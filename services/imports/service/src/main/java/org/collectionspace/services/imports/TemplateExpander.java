/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2011 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.collectionspace.services.imports;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.common.IFragmentHandler;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.XmlSaxFragmenter;
import org.collectionspace.services.common.XmlTools;
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.datetime.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.InputSource;

/** This class expands templates specifically for the imports service.
 *
 *  To see capability to create workspaces, see svn revision 4346 on branch
 *  https://source.collectionspace.org/collection-space/src/services/branches/CSPACE-3178/services
 *  This capability was removed, as it was necessary for testing only.
 *
 * @author Laramie Crocker
 */
public class TemplateExpander {
    
    private static Map<String,String> docTypeSvcNameRegistry = new HashMap<String,String>();
    private static XPath xpath = XPathFactory.newInstance().newXPath();
    private static final String IN_AUTHORITY_NAMESPACE_XPATH = "//*[local-name()='inAuthority']";
    private static final String IN_AUTHORITY_NO_NAMESPACE_XPATH = "//inAuthority";

    protected static String var(String theVar){
        return "\\$\\{"+theVar+"\\}";
    }

    /**
     * @param source the template, which contains variables wrapped in a dollar sign and curly braces, e.g. source="my template with ID ${docID} yada yada."
     * @param theVar a variable name, without the dollar sign or curly braces or internal quotes, e.g. searchAndReplaceVar(source, "docID", "1234-5678")
     * @param replace the value the variable will be replaced with.
     * @return the expanded template.
     */
    public static String searchAndReplaceVar(String source, String theVar, String replace){
        return Tools.searchAndReplaceWithQuoteReplacement(source, var(theVar), replace);
    }

    public static String doOneService(String tenantId, String outDir, String partTmpl, String wrapperTmpl,
                                      String SERVICE_TYPE, String SERVICE_NAME, String CSID) throws Exception {
        String docID;
        if (Tools.notBlank(CSID)){
            docID = CSID;
        } else {
            docID = UUID.randomUUID().toString();
        }
        String part = searchAndReplaceVar(partTmpl, "docID", docID);

        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "Schema", part);
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "docID", docID);
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "tenantID", tenantId);
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "ServiceType", SERVICE_TYPE);
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "ServiceName", SERVICE_NAME);
        //TODO: set timestamp via creating a ${created} variable.
        String nowTime = GregorianCalendarDateTimeUtils.timestampUTC();
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "createdDate", nowTime);
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "updatedDate", nowTime);
        
        wrapperTmpl = Tools.searchAndReplace(wrapperTmpl, var("uri"),
                getDocUri(tenantId, SERVICE_TYPE, docID, partTmpl));

        String serviceDir = outDir+'/'+docID;
        FileTools.saveFile(serviceDir, "document.xml", wrapperTmpl, true/*true=create parent dirs*/);
        return docID;
    }


    /** Once you have called createWorkspace() to create a home for documents of a service, you can call this method to add documents for that service.
     *
     *  Internally, this method also gets called by the XmlSaxFragmenter callback via the public inner class FragmentHandlerImpl.
     *
     * @param partTmpl  A template file that contains the schema part for the service, and which has macros such as ${docID} to be expanded.
     * @param SERVICE_NAME The name of the service, such as "Personauthorities"
     * @param SERVICE_TYPE The Nuxeo document type, such as "Personauthority"
     * @param TEMPLATE_DIR The local filesystem location of all the standard templates that wrap up workspace documents;
     *                     once expanded, these spit out Nuxeo import format.
     * @param CSID An optional parameter which forces the document CSID, otherwise the CSID is set to a random UUID.
     * @throws Exception
     */
    public static void createDocInWorkspace(
    		String tenantId,
            String partTmpl,
            String SERVICE_NAME,
            String SERVICE_TYPE,
            String TEMPLATE_DIR,
            String OUTPUT_DIR,
            String CSID) throws Exception {
        String wrapperTmpl = FileTools.readFile(TEMPLATE_DIR,"service-document.xml");
        String outputDir = OUTPUT_DIR+'/'+SERVICE_NAME;
        doOneService(tenantId, outputDir, partTmpl, wrapperTmpl, SERVICE_TYPE, SERVICE_NAME, CSID);
    }

    public static void expand(String tenantId, String TEMPLATE_DIR, String outputDir, String requestFilename, String chopPath){
        FragmentHandlerImpl callback = new FragmentHandlerImpl(tenantId, TEMPLATE_DIR, outputDir);
        XmlSaxFragmenter.parse(requestFilename, chopPath, callback, false);
    }

    public static void expandInputSource(String tenantId, String TEMPLATE_DIR, String outputDir, InputSource requestSource, String chopPath){
        FragmentHandlerImpl callback = new FragmentHandlerImpl(tenantId, TEMPLATE_DIR, outputDir);
        XmlSaxFragmenter.parse(requestSource, chopPath, callback, false);
    }

    // The docType parameter here is matched to the SERVICE_TYPE argument in
    // the calling method doOneService(), above; both refer to a per-service
    // document type name
    private static String getDocUri(String tenantId, String docType, String docID,
            String partTmpl) throws Exception {
        
        // FIXME: This is a quick hack, which assumes that URI construction
        // behaviors are bound to categories of services.  Those behaviors
        // should instead be specified on a per-service basis via a registry,
        // the mechanism we are intending to use in v2.5.  (See comments below
        // for more details.) - ADR 2012-05-24
        final String AUTHORITY_SERVICE_CATEGORY = "authority";
        final String OBJECT_SERVICE_CATEGORY = "object";
        final String PROCEDURE_SERVICE_CATEGORY = "procedure";

        TenantBindingConfigReaderImpl tReader = ServiceMain.getInstance().getTenantBindingConfigReader();
        // We may have been supplied with the tenant-qualified name
        // of an extension to a document type, and thus need to
        // get the base document type name.
        docType = ServiceBindingUtils.getUnqualifiedTenantDocType(docType);
        ServiceBindingType sb =
            tReader.getServiceBindingForDocType(tenantId, docType);        

        String serviceCategory = sb.getType();
        String uri = "";
        if (serviceCategory.equalsIgnoreCase(AUTHORITY_SERVICE_CATEGORY)) {
            String authoritySvcName = getAuthoritySvcName(docType);
            if (authoritySvcName == null) {
                return uri;
            }
            String inAuthorityID = getInAuthorityValue(partTmpl);
            uri = getAuthorityItemUri(authoritySvcName, inAuthorityID, docID);
       } else if (serviceCategory.equalsIgnoreCase(OBJECT_SERVICE_CATEGORY) ||
               serviceCategory.equalsIgnoreCase(PROCEDURE_SERVICE_CATEGORY) ) {
            String serviceName = sb.getName().toLowerCase();
            uri = getUri(serviceName, docID);
       } else {
           // Currently returns a blank URI for any other cases,
           // including sub-resources like contacts
         }
        return uri;
    }
    
    // FIXME: This is a quick hack; a stub / mock of a registry of
    // authority document types and their associated parent authority
    // service names. This MUST be replaced by a more general mechanism
    // in v2.5. 
    // 
    // Per Patrick, this registry needs to be available system-wide, not
    // just here in the Imports service; extend to all relevant record types;
    // and be automatically built in some manner, such as via per-resource
    // registration, from configuration, etc. - ADR 2012-05-24
    private static Map<String,String> getDocTypeSvcNameRegistry() {
        if (docTypeSvcNameRegistry.isEmpty()) {
            docTypeSvcNameRegistry.put("Conceptitem", "Conceptauthorities");
            docTypeSvcNameRegistry.put("Locationitem", "Locationauthorities");
            docTypeSvcNameRegistry.put("Person", "Personauthorities");
            docTypeSvcNameRegistry.put("Placeitem", "Placeauthorities");
            docTypeSvcNameRegistry.put("Organization", "Orgauthorities");
            docTypeSvcNameRegistry.put("Taxon", "Taxonomyauthority");
        }
        return docTypeSvcNameRegistry;
    }
    
    /**
     * Return the parent authority service name, based on the item document type.
     */
    private static String getAuthoritySvcName(String docType) {
        return getDocTypeSvcNameRegistry().get(docType);
    }
    
    // FIXME: The following URI construction methods are also intended to be
    // made generally available and associated to individual services, via the
    // registry mechanism described above. - ADR, 2012-05-24
    private static String getUri(String serviceName, String docID) {
        return "/" + serviceName
                + "/" + docID;
    }

    private static String getAuthorityItemUri(String authorityServiceName, String inAuthorityID, String docID) {
        return "/" + authorityServiceName.toLowerCase()
                + '/' + inAuthorityID
                + '/' + AuthorityClient.ITEMS
                + '/' + docID;
    }
    
    // FIXME: Create equivalent getUri-type method(s) for sub-resources,
    // such as contacts - ADR, 2012-05-24
    
    // FIXME: It may also be desirable to explicitly validate the format of
    // CSID values provided in fields such as inAuthority, and perhaps later on,
    // their uniqueness against those already present in a running system.
    // - ADR 2012-05-24
    private static String getInAuthorityValue(String xmlFragment) {
        String inAuthorityValue = "";
        // Check in two ways for the inAuthority value: one intended for records with
        // namespace-qualified elements, the second for unqualified elements.
        // (There may be a more elegant way to do this with a single XPath expression,
        // via an OR operator or the like.)
        System.out.println("before setting inAuthority value");
        inAuthorityValue = extractValueFromXmlFragment(IN_AUTHORITY_NAMESPACE_XPATH, xmlFragment);
        System.out.println("after setting namespaced inAuthority value: " + inAuthorityValue);
        if (Tools.isBlank(inAuthorityValue)) {
            System.out.println("in if block ...");
            inAuthorityValue = extractValueFromXmlFragment(IN_AUTHORITY_NO_NAMESPACE_XPATH, xmlFragment);
            System.out.println("after setting non-namespaced inAuthority value: " + inAuthorityValue);
        } else {
          System.out.println("bypassed if block ...");
        }
        return inAuthorityValue;
    }
    
    // FIXME: Need to handle cases here where the xmlFragment may contain more
    // than one matching expression. (Simply matching on instance [0] within
    // the XPath expression might not be reasonable, as it won't always be
    // evident if that specific value is pertinent.) - ADR 2012-05-24
    private static String extractValueFromXmlFragment(String xpathExpr, String xmlFragment) {
        String value = "";
        try {
            // FIXME: Cruelly ugly hack; at this point for imported records
            // with more than one <schema> child, we have a non-well-formed fragment.
            String xmlFragmentWrapped = "<root>" + xmlFragment + "</root>";
            InputSource input = new InputSource(new StringReader(xmlFragmentWrapped));
            value = xpath.evaluate(xpathExpr, input);
        } catch (XPathExpressionException e) {
            System.out.println(e.getMessage());
        }
        return value;

    }
    
    /** This inner class is the callback target for calls to XmlSaxFragmenter, for example:
     *     FragmentHandlerImpl callback = new FragmentHandlerImpl();
     *     XmlSaxFragmenter.parse(filename, "/imports/import", callback, false);
     *  It will be called for every /imports/import in the file:
     *      &lt;import ID="1" service="Personauthorities" type="Personauthority">
     */
    public static class FragmentHandlerImpl implements IFragmentHandler {
        public String SERVICE_NAME = "";   //You can provide a default.
        public String SERVICE_TYPE = "";   //You can provide a default.
        public String TEMPLATE_DIR = "";   //You MUST provide a default via constructor.
        public String OUPUT_DIR = "";      //You MUST provide a default via constructor.
        public String TENANT_ID = "";

        //============IFragmentHandler===========================================================
        public void onFragmentReady(Document context, Element fragmentParent, String currentPath, int fragmentIndex, String fragment){
            try {
                dump(context, currentPath, fragmentIndex, fragment);
                String serviceName = checkAttribute(fragmentParent, "service", SERVICE_NAME);
                String serviceType = checkAttribute(fragmentParent, "type", SERVICE_TYPE);
                serviceType = NuxeoUtils.getTenantQualifiedDocType(TENANT_ID, serviceType); //REM - Ensure a tenant qualified Nuxeo doctype
                String CSID  = fragmentParent.attributeValue("CSID");
                TemplateExpander.createDocInWorkspace(TENANT_ID, fragment, serviceName, serviceType, TEMPLATE_DIR, OUPUT_DIR, CSID);
            } catch (Exception e){
                System.err.println("ERROR calling expandXmlPayloadToDir"+e);
                e.printStackTrace();
            }
        }
        public void onEndDocument(Document document, int fragmentCount){
            System.out.println("====TemplateExpander DONE============\r\n"+ XmlTools.prettyPrint(document)+"================");
        }
        //============helper methods==============================================================
        public FragmentHandlerImpl(String tenantId, String templateDir, String outputDir){
            TEMPLATE_DIR = templateDir;
            OUPUT_DIR = outputDir;
            TENANT_ID = tenantId;
        }
        private String checkAttribute(Element fragmentParent, String attName, String defaultVal){
            String val = fragmentParent.attributeValue(attName);
            if (Tools.notEmpty(val)){
                return val;
            }
            return defaultVal;
        }
        private void dump(Document context, String currentPath, int fragmentIndex, String fragment){
            System.out.println("====Path============\r\n"+currentPath+'['+fragmentIndex+']');
            System.out.println("====Context=========\r\n"+ XmlTools.prettyPrint(context));
            System.out.println("====Fragment========\r\n"+fragment+"\r\n===================\r\n");
        }
    }

}
