package org.collectionspace.services.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;

import org.collectionspace.services.MaterialJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.material.MaterialTermGroup;
import org.collectionspace.services.material.MaterialTermGroupList;
import org.collectionspace.services.material.MaterialauthoritiesCommon;
import org.collectionspace.services.material.MaterialsCommon;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaterialAuthorityClientUtils {
    private static final Logger logger =
        LoggerFactory.getLogger(MaterialAuthorityClientUtils.class);

    /**
     * Creates a new Material Authority
     * @param displayName   The displayName used in UI, etc.
     * @param refName       The proper refName for this authority
     * @param headerLabel   The common part label
     * @return  The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createMaterialAuthorityInstance(
            String displayName, String shortIdentifier, String headerLabel ) {
        MaterialauthoritiesCommon materialAuthority = new MaterialauthoritiesCommon();
        materialAuthority.setDisplayName(displayName);
        materialAuthority.setShortIdentifier(shortIdentifier);
        String refName = createMaterialAuthRefName(shortIdentifier, displayName);
        materialAuthority.setRefName(refName);
        materialAuthority.setVocabType("MaterialAuthority"); //FIXME: REM - Should this really be hard-coded?
        PoxPayloadOut multipart = new PoxPayloadOut(MaterialAuthorityClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(materialAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
            logger.debug("to be created, materialAuthority common ", 
                        materialAuthority, MaterialauthoritiesCommon.class);
        }

        return multipart;
    }

    /**
     * @param materialRefName  The proper refName for this authority
     * @param materialInfo the properties for the new Material. Can pass in one condition
     *                      note and date string.
     * @param headerLabel   The common part label
     * @return  The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createMaterialInstance( 
            String materialAuthRefName, Map<String, String> materialInfo, 
        List<MaterialTermGroup> terms, String headerLabel){
        MaterialsCommon material = new MaterialsCommon();
        String shortId = materialInfo.get(MaterialJAXBSchema.SHORT_IDENTIFIER);
        material.setShortIdentifier(shortId);

        // Set values in the Term Information Group
        MaterialTermGroupList termList = new MaterialTermGroupList();
        if (terms == null || terms.isEmpty()) {
            terms = getTermGroupInstance(getGeneratedIdentifier());
        }
        termList.getMaterialTermGroup().addAll(terms); 
        material.setMaterialTermGroupList(termList);
        
        PoxPayloadOut multipart = new PoxPayloadOut(MaterialAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(material,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
            logger.debug("to be created, material common ", material, MaterialsCommon.class);
        }

        return multipart;
    }
    
    /**
     * @param vcsid CSID of the authority to create a new material
     * @param materialAuthorityRefName The refName for the authority
     * @param materialMap the properties for the new Material
     * @param client the service client
     * @return the CSID of the new item
     */
    public static String createItemInAuthority(String vcsid, 
            String materialAuthorityRefName, Map<String,String> materialMap,
            List<MaterialTermGroup> terms, MaterialAuthorityClient client ) {
        // Expected status code: 201 Created
        int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
        // Type of service request being tested
        ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
        
        String displayName = "";
        if ((terms !=null) && (! terms.isEmpty())) {
            displayName = terms.get(0).getTermDisplayName();
        }
        if(logger.isDebugEnabled()){
            logger.debug("Creating item with display name: \"" + displayName
                    +"\" in locationAuthority: \"" + vcsid +"\"");
        }
        PoxPayloadOut multipart = 
            createMaterialInstance(materialAuthorityRefName, materialMap, terms, client.getItemCommonPartName() );
        String newID = null;
        Response res = client.createItem(vcsid, multipart);
        try {
            int statusCode = res.getStatus();
    
            if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
                throw new RuntimeException("Could not create Item: \""
                        +materialMap.get(MaterialJAXBSchema.SHORT_IDENTIFIER)
                        +"\" in materialAuthority: \"" + materialAuthorityRefName
                        +"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            }
            if(statusCode != EXPECTED_STATUS_CODE) {
                throw new RuntimeException("Unexpected Status when creating Item: \""
                        +materialMap.get(MaterialJAXBSchema.SHORT_IDENTIFIER)
                        +"\" in materialAuthority: \"" + materialAuthorityRefName +"\", Status:"+ statusCode);
            }
            newID = extractId(res);
        } finally {
            res.close();
        }

        return newID;
    }

    public static PoxPayloadOut createMaterialInstance(
            String commonPartXML, String headerLabel)  throws DocumentException {
        PoxPayloadOut multipart = new PoxPayloadOut(MaterialAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(commonPartXML,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
            logger.debug("to be created, material common ", commonPartXML);
        }

        return multipart;
    }
    
    public static String createItemInAuthority(String vcsid,
            String commonPartXML,
            MaterialAuthorityClient client ) throws DocumentException {
        // Expected status code: 201 Created
        int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
        // Type of service request being tested
        ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
        
        PoxPayloadOut multipart = 
            createMaterialInstance(commonPartXML, client.getItemCommonPartName());
        String newID = null;
        Response res = client.createItem(vcsid, multipart);
        try {
            int statusCode = res.getStatus();
    
            if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
                throw new RuntimeException("Could not create Item: \""+commonPartXML
                        +"\" in materialAuthority: \"" + vcsid
                        +"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            }
            if(statusCode != EXPECTED_STATUS_CODE) {
                throw new RuntimeException("Unexpected Status when creating Item: \""+commonPartXML
                        +"\" in materialAuthority: \"" + vcsid +"\", Status:"+ statusCode);
            }
            newID = extractId(res);
        } finally {
            res.close();
        }

        return newID;
    }
    
    /**
     * Creates the from xml file.
     *
     * @param fileName the file name
     * @return new CSID as string
     * @throws Exception the exception
     */
    private String createItemInAuthorityFromXmlFile(String vcsid, String commonPartFileName, 
            MaterialAuthorityClient client) throws Exception {
        byte[] b = FileUtils.readFileToByteArray(new File(commonPartFileName));
        String commonPartXML = new String(b);
        return createItemInAuthority(vcsid, commonPartXML, client );
    }    

    /**
     * Creates the materialAuthority ref name.
     *
     * @param shortId the materialAuthority shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createMaterialAuthRefName(String shortId, String displaySuffix) {
        String refName = "urn:cspace:org.collectionspace.demo:materialauthority:name("
            +shortId+")";
        if(displaySuffix!=null&&!displaySuffix.isEmpty())
            refName += "'"+displaySuffix+"'";
        return refName;
    }

    /**
     * Creates the material ref name.
     *
     * @param materialAuthRefName the materialAuthority ref name
     * @param shortId the material shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createMaterialRefName(
                            String materialAuthRefName, String shortId, String displaySuffix) {
        String refName = materialAuthRefName+":material:name("+shortId+")";
        if(displaySuffix!=null&&!displaySuffix.isEmpty())
            refName += "'"+displaySuffix+"'";
        return refName;
    }

    public static String extractId(Response res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        String uri = (String) ((ArrayList<Object>) mvm.get("Location")).get(0);
        if(logger.isDebugEnabled()){
            logger.debug("extractId:uri=" + uri);
        }
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        if(logger.isDebugEnabled()){
            logger.debug("id=" + id);
        }
        return id;
    }
    
    /**
     * Returns an error message indicating that the status code returned by a
     * specific call to a service does not fall within a set of valid status
     * codes for that service.
     *
     * @param serviceRequestType  A type of service request (e.g. CREATE, DELETE).
     *
     * @param statusCode  The invalid status code that was returned in the response,
     *                    from submitting that type of request to the service.
     *
     * @return An error message.
     */
    public static String invalidStatusCodeMessage(ServiceRequestType requestType, int statusCode) {
        return "Status code '" + statusCode + "' in response is NOT within the expected set: " +
                requestType.validStatusCodesAsString();
    }


    
    /**
     * Produces a default displayName from one or more supplied field(s).
     * @see MaterialAuthorityDocumentModelHandler.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param materialName  
     * @return a display name
     */
    public static String prepareDefaultDisplayName(
            String materialName ) {
        StringBuilder newStr = new StringBuilder();
            newStr.append(materialName);
        return newStr.toString();
    }
    
    public static List<MaterialTermGroup> getTermGroupInstance(String identifier) {
        if (Tools.isBlank(identifier)) {
            identifier = getGeneratedIdentifier();
        }
        List<MaterialTermGroup> terms = new ArrayList<MaterialTermGroup>();
        MaterialTermGroup term = new MaterialTermGroup();
        term.setTermDisplayName(identifier);
        term.setTermName(identifier);
        terms.add(term);
        return terms;
    }
    
    private static String getGeneratedIdentifier() {
        return "id" + new Date().getTime(); 
   }
    
}
