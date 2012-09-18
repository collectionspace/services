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
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.collectionspace.services.common.IFragmentHandler;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.StoredValuesUriTemplate;
import org.collectionspace.services.common.UriTemplateFactory;
import org.collectionspace.services.common.UriTemplateRegistry;
import org.collectionspace.services.common.UriTemplateRegistryKey;
import org.collectionspace.services.common.XmlSaxFragmenter;
import org.collectionspace.services.common.XmlTools;
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/** This class expands variables in templates specifically for the imports service.
 *
 *  To see capability to create workspaces, see svn revision 4346 on branch
 *  https://source.collectionspace.org/collection-space/src/services/branches/CSPACE-3178/services
 *  This capability was removed, as it was necessary for testing only.
 *
 * @author Laramie Crocker
 */
public class TemplateExpander {
    
    private final static Logger logger = LoggerFactory.getLogger(TemplateExpander.class);
    
    private static final String DEFAULT_WRAPPER_TEMPLATE_FILENAME = "service-document.xml";
    private static XPath xpath = XPathFactory.newInstance().newXPath();
    // XPath expressions to match the value of the inAuthority field in authority item records.
    // The first expression matches namespace-qualified elements, while the second matches
    // non-namespace-qualified elements.
    private static final String IN_AUTHORITY_NAMESPACE_XPATH = "//*[local-name()='inAuthority']";
    private static final String IN_AUTHORITY_NO_NAMESPACE_XPATH = "//inAuthority";
    
    private static final String SERVICE_ATTRIBUTE = "service";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String CREATED_AT_ATTRIBUTE = "createdAt";
    private static final String CREATED_BY_ATTRIBUTE = "createdBy";
    private static final String UPDATED_AT_ATTRIBUTE = "updatedAt";
    private static final String UPDATED_BY_ATTRIBUTE = "updatedBy";
    

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

    /**
     * <p>Creates a single document, representing a single record or resource, ready
     * for import into a Nuxeo workspace.</p>
     * 
     * <p>Expands macro variables within this document, with values supplied during
     * import, or obtained from the CollectionSpace system or its environment.</p>
     * 
     * @param tenantId a tenant ID.
     * @param outDir an output directory name.
     * @param partTmpl a template file containing the content to be imported.
     *   This consists of content within one or more <schema> tags - one such tag
     *   for each part of the record being imported - and may contain macro variables
     *   such as ${docID} to be expanded.
     * @param wrapperTmpl a wrapper template into which the content to be imported 
     *   (in the partTmpl) will be inserted.  This template contains additional
     *   metadata fields required by CollectionSpace and Nuxeo, some of whose values
     *   are set in this method, via expansion of additional macro variables.
     * @param SERVICE_TYPE the service document type.
     * @param SERVICE_NAME the service name.
     * @param CSID an optional CollectionSpace ID (CSID) for the document. If no
     *   CSID was provided, it will be generated.
     * @return the ID of the just-created document.
     * @throws Exception  
     */
    public static String doOneService(String tenantId, String outDir, String partTmpl, String wrapperTmpl,
                                      String SERVICE_TYPE, String SERVICE_NAME, Map<String,String> perRecordAttributes,
                                      String CSID) throws Exception {
        String docID;
        // Generate a CSID if one was not provided with the import record.
        if (Tools.notBlank(CSID)){
            docID = CSID;
        } else {
            docID = UUID.randomUUID().toString();
        }
        
        // Expand macro variables within the content to be imported.
        String part = searchAndReplaceVar(partTmpl, "docID", docID);
        
        // Insert the content to be imported into the wrapper template.
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "Schema", part);
        
        // Expand macro variables within the wrapper template.
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "docID", docID);
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "tenantID", tenantId);
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "ServiceType", SERVICE_TYPE);
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "ServiceName", SERVICE_NAME);
        String nowTime = GregorianCalendarDateTimeUtils.timestampUTC();
        String createdAtTime = getAttributeValue(perRecordAttributes, CREATED_AT_ATTRIBUTE);
        if (Tools.notBlank(createdAtTime)) {
            wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "createdAt", createdAtTime);
        } else {
            wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "createdAt", nowTime);
        }
        String updatedAtTime = getAttributeValue(perRecordAttributes, UPDATED_AT_ATTRIBUTE);
        if (Tools.notBlank(updatedAtTime)) {
            wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "updatedAt", updatedAtTime);
        } else {
            wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "updatedAt", nowTime);
        }
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "createdBy",
                getAttributeValue(perRecordAttributes, CREATED_BY_ATTRIBUTE));
        wrapperTmpl = searchAndReplaceVar(wrapperTmpl, "updatedBy", 
                getAttributeValue(perRecordAttributes, UPDATED_BY_ATTRIBUTE));
        wrapperTmpl = Tools.searchAndReplace(wrapperTmpl, var("uri"),
                getDocUri(tenantId, SERVICE_TYPE, docID, partTmpl));

        String serviceDir = outDir+'/'+docID;
        FileTools.saveFile(serviceDir, "document.xml", wrapperTmpl, FileTools.FORCE_CREATE_PARENT_DIRS);
        return docID;
    }


    /** Once you have called createWorkspace() to create a home for documents of a service, you can call this method to add documents for that service.
     *
     *  Internally, this method also gets called by the XmlSaxFragmenter callback via the public inner class FragmentHandlerImpl.
     *
     * @param partTmpl  A template file that contains the schema part for the service, and which has macros such as ${docID} to be expanded.
     * @param SERVICE_NAME The name of the service, such as "CollectionObjects" or "Personauthorities".
     * @param SERVICE_TYPE The Nuxeo document type, such as "CollectionObject" or "Personauthority".
     * @param perRecordAttributes a property bag of additional per-record attributes.
     * @param TEMPLATE_DIR The local filesystem location of all the standard templates that wrap up workspace documents;
     *                     once expanded, these spit out Nuxeo import format.
     * @param CSID an optional CollectionSpace ID (CSID) for the document. If no CSID was provided, it will be generated.
     * @throws Exception
     */
    public static void createDocInWorkspace(
    		String tenantId,
            String partTmpl,
            String SERVICE_NAME,
            String SERVICE_TYPE,
            Map<String,String> perRecordAttributes,
            String TEMPLATE_DIR,
            String OUTPUT_DIR,
            String CSID) throws Exception {
        String wrapperTmpl = FileTools.readFile(TEMPLATE_DIR, DEFAULT_WRAPPER_TEMPLATE_FILENAME);
        String outputDir = OUTPUT_DIR+'/'+SERVICE_NAME;
        doOneService(tenantId, outputDir, partTmpl, wrapperTmpl, SERVICE_TYPE, SERVICE_NAME, perRecordAttributes, CSID);
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
        String uri = "";
        UriTemplateRegistry registry = ServiceMain.getInstance().getUriTemplateRegistry();
        UriTemplateRegistryKey key = new UriTemplateRegistryKey(tenantId, docType);
        StoredValuesUriTemplate template = registry.get(key);
        if (template != null) {
            Map<String, String> additionalValues = new HashMap<String, String>();
            if (template.getUriTemplateType() == UriTemplateFactory.RESOURCE) {
                additionalValues.put(UriTemplateFactory.IDENTIFIER_VAR, docID);
                uri = template.buildUri(additionalValues);
            } else if (template.getUriTemplateType() == UriTemplateFactory.ITEM) {
                try {
                    String inAuthorityCsid = getInAuthorityValue(partTmpl);
                    additionalValues.put(UriTemplateFactory.IDENTIFIER_VAR, inAuthorityCsid);
                    additionalValues.put(UriTemplateFactory.ITEM_IDENTIFIER_VAR, docID);
                    uri = template.buildUri(additionalValues);
                } catch (Exception e) {
                    logger.warn("Could not extract inAuthority property from authority item record: " + e.getMessage());
                    // Returns the default (empty string) value for uri
                }
            } else if (template.getUriTemplateType() == UriTemplateFactory.CONTACT) {
                // FIXME: Generating contact sub-resource URIs requires additional work,
                // as a follow-on to CSPACE-5271 - ADR 2012-08-16
                // Returns the default (empty string) value for uri, for now
            } else {
                logger.warn("Unrecognized URI template type = " + template.getUriTemplateType());
                // Returns the default (empty string) value for uri
            }
        } else { // (if template == null)
            logger.warn("Could not retrieve URI template from registry via tenant ID "
                    + tenantId + " and docType " + docType);
            // Returns the default (empty string) value for uri
        }
        return uri;
    }
    
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
        inAuthorityValue = extractValueFromXmlFragment(IN_AUTHORITY_NAMESPACE_XPATH, xmlFragment);
        if (Tools.isBlank(inAuthorityValue)) {
            inAuthorityValue = extractValueFromXmlFragment(IN_AUTHORITY_NO_NAMESPACE_XPATH, xmlFragment);
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
            logger.error(e.getMessage());
        }
        return value;

    }
    
    private static String getAttributeValue(Map<String,String> attributes, String attributeName){
        String attributeVal = "";
        if (attributes.containsKey(attributeName.toLowerCase())) {
            attributeVal = (String) attributes.get(attributeName.toLowerCase());
        }
        return attributeVal;
    }
    
    /** This inner class is the callback target for calls to XmlSaxFragmenter, for example:
     *     FragmentHandlerImpl callback = new FragmentHandlerImpl();
     *     XmlSaxFragmenter.parse(filename, "/imports/import", callback, false);
     *  It will be called for every /imports/import in the file:
     *      &lt;import ID="1" service="Personauthorities" type="Personauthority">
     */
    public static class FragmentHandlerImpl implements IFragmentHandler {
        public String DEFAULT_SERVICE_NAME = "";   //You can provide a default.
        public String DEFAULT_SERVICE_TYPE = "";   //You can provide a default.
        public String TEMPLATE_DIR = "";   //You MUST provide a default via constructor.
        public String OUPUT_DIR = "";      //You MUST provide a default via constructor.
        public String TENANT_ID = "";

        //============IFragmentHandler===========================================================
        public void onFragmentReady(Document context, Element fragmentParent, String currentPath, int fragmentIndex, String fragment){
            try {
                dump(context, currentPath, fragmentIndex, fragment);
                String serviceName = checkAttribute(fragmentParent, SERVICE_ATTRIBUTE, DEFAULT_SERVICE_NAME);
                String serviceType = checkAttribute(fragmentParent, TYPE_ATTRIBUTE, DEFAULT_SERVICE_TYPE);
                Map<String,String> perRecordAttributes = getPerRecordAttributes(fragmentParent);
                serviceType = NuxeoUtils.getTenantQualifiedDocType(TENANT_ID, serviceType); //REM - Ensure a tenant qualified Nuxeo doctype
                String CSID  = fragmentParent.attributeValue("CSID");
                TemplateExpander.createDocInWorkspace(TENANT_ID, fragment, serviceName, serviceType,
                        perRecordAttributes, TEMPLATE_DIR, OUPUT_DIR, CSID);
            } catch (Exception e){
                logger.error("ERROR calling expandXmlPayloadToDir"+e);
                e.printStackTrace();
            }
        }
        public void onEndDocument(Document document, int fragmentCount){
            if (logger.isTraceEnabled()) {
                logger.trace("====TemplateExpander DONE============\r\n"+ XmlTools.prettyPrint(document)+"================");
            }
        }
        
        //============helper methods==============================================================
        public FragmentHandlerImpl(String tenantId, String templateDir, String outputDir){
            TEMPLATE_DIR = templateDir;
            OUPUT_DIR = outputDir;
            TENANT_ID = tenantId;
        }
        
        private Map<String,String> getPerRecordAttributes(Element fragmentParent){
            Map<String,String> perRecordAttributes = new HashMap<String,String>();
            for ( Iterator<Attribute> attributesIterator = fragmentParent.attributeIterator(); attributesIterator.hasNext(); ) {
                Attribute attr = attributesIterator.next();
                perRecordAttributes.put(attr.getName().toLowerCase(), attr.getValue());
            }
            return perRecordAttributes;
        }
        
        private String checkAttribute(Element fragmentParent, String attName, String defaultVal){
            String val = fragmentParent.attributeValue(attName);
            if (Tools.notEmpty(val)){
                return val;
            }
            return defaultVal;
        }
        
        private void dump(Document context, String currentPath, int fragmentIndex, String fragment){
            if (logger.isTraceEnabled()) {
                logger.trace("====Path============\r\n"+currentPath+'['+fragmentIndex+']');
                logger.trace("====Context=========\r\n"+ XmlTools.prettyPrint(context));
                logger.trace("====Fragment========\r\n"+fragment+"\r\n===================\r\n");
            }
        }
    }

}
