package org.collectionspace.services.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;

import org.collectionspace.services.WorkJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.work.WorkTermGroup;
import org.collectionspace.services.work.WorkTermGroupList;
import org.collectionspace.services.work.WorkauthoritiesCommon;
import org.collectionspace.services.work.WorksCommon;

import org.dom4j.DocumentException;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkAuthorityClientUtils {
    private static final Logger logger =
        LoggerFactory.getLogger(WorkAuthorityClientUtils.class);

    /**
     * Creates a new Work Authority
     * @param displayName   The displayName used in UI, etc.
     * @param refName       The proper refName for this authority
     * @param headerLabel   The common part label
     * @return  The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createWorkAuthorityInstance(
            String displayName, String shortIdentifier, String headerLabel ) {
        WorkauthoritiesCommon workAuthority = new WorkauthoritiesCommon();
        workAuthority.setDisplayName(displayName);
        workAuthority.setShortIdentifier(shortIdentifier);
        String refName = createWorkAuthRefName(shortIdentifier, displayName);
        workAuthority.setRefName(refName);
        workAuthority.setVocabType("WorkAuthority"); //FIXME: REM - Should this really be hard-coded?
        PoxPayloadOut multipart = new PoxPayloadOut(WorkAuthorityClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(workAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
            logger.debug("to be created, workAuthority common ", 
                        workAuthority, WorkauthoritiesCommon.class);
        }

        return multipart;
    }

    /**
     * @param workRefName  The proper refName for this authority
     * @param workInfo the properties for the new Work. Can pass in one condition
     *                      note and date string.
     * @param headerLabel   The common part label
     * @return  The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createWorkInstance( 
            String workAuthRefName, Map<String, String> workInfo, 
        List<WorkTermGroup> terms, String headerLabel){
        WorksCommon work = new WorksCommon();
        String shortId = workInfo.get(WorkJAXBSchema.SHORT_IDENTIFIER);
        work.setShortIdentifier(shortId);

        // Set values in the Term Information Group
        WorkTermGroupList termList = new WorkTermGroupList();
        if (terms == null || terms.isEmpty()) {
            terms = getTermGroupInstance(getGeneratedIdentifier());
        }
        termList.getWorkTermGroup().addAll(terms); 
        work.setWorkTermGroupList(termList);
        
        PoxPayloadOut multipart = new PoxPayloadOut(WorkAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(work,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
            logger.debug("to be created, work common ", work, WorksCommon.class);
        }

        return multipart;
    }
    
    /**
     * @param vcsid CSID of the authority to create a new work
     * @param workAuthorityRefName The refName for the authority
     * @param workMap the properties for the new Work
     * @param client the service client
     * @return the CSID of the new item
     */
    public static String createItemInAuthority(String vcsid, 
            String workAuthorityRefName, Map<String,String> workMap,
            List<WorkTermGroup> terms, WorkAuthorityClient client ) {
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
            createWorkInstance( workAuthorityRefName,
                workMap, terms, client.getItemCommonPartName() );
        String newID = null;
        Response res = client.createItem(vcsid, multipart);
        try {
            int statusCode = res.getStatus();
    
            if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
                throw new RuntimeException("Could not create Item: \""
                        +workMap.get(WorkJAXBSchema.SHORT_IDENTIFIER)
                        +"\" in workAuthority: \"" + workAuthorityRefName
                        +"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            }
            if(statusCode != EXPECTED_STATUS_CODE) {
                throw new RuntimeException("Unexpected Status when creating Item: \""
                        +workMap.get(WorkJAXBSchema.SHORT_IDENTIFIER)
                        +"\" in workAuthority: \"" + workAuthorityRefName +"\", Status:"+ statusCode);
            }
            newID = extractId(res);
        } finally {
            res.close();
        }

        return newID;
    }

    public static PoxPayloadOut createWorkInstance(
            String commonPartXML, String headerLabel)  throws DocumentException {
        PoxPayloadOut multipart = new PoxPayloadOut(WorkAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(commonPartXML,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
            logger.debug("to be created, work common ", commonPartXML);
        }

        return multipart;
    }
    
    public static String createItemInAuthority(String vcsid,
            String commonPartXML,
            WorkAuthorityClient client ) throws DocumentException {
        // Expected status code: 201 Created
        int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
        // Type of service request being tested
        ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
        
        PoxPayloadOut multipart = 
            createWorkInstance(commonPartXML, client.getItemCommonPartName());
        String newID = null;
        Response res = client.createItem(vcsid, multipart);
        try {
            int statusCode = res.getStatus();
    
            if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
                throw new RuntimeException("Could not create Item: \""+commonPartXML
                        +"\" in workAuthority: \"" + vcsid
                        +"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            }
            if(statusCode != EXPECTED_STATUS_CODE) {
                throw new RuntimeException("Unexpected Status when creating Item: \""+commonPartXML
                        +"\" in workAuthority: \"" + vcsid +"\", Status:"+ statusCode);
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
            WorkAuthorityClient client) throws Exception {
        byte[] b = FileUtils.readFileToByteArray(new File(commonPartFileName));
        String commonPartXML = new String(b);
        return createItemInAuthority(vcsid, commonPartXML, client );
    }    

    /**
     * Creates the workAuthority ref name.
     *
     * @param shortId the workAuthority shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createWorkAuthRefName(String shortId, String displaySuffix) {
        String refName = "urn:cspace:org.collectionspace.demo:workauthority:name("
            +shortId+")";
        if(displaySuffix!=null&&!displaySuffix.isEmpty())
            refName += "'"+displaySuffix+"'";
        return refName;
    }

    /**
     * Creates the work ref name.
     *
     * @param workAuthRefName the workAuthority ref name
     * @param shortId the work shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createWorkRefName(
                            String workAuthRefName, String shortId, String displaySuffix) {
        String refName = workAuthRefName+":work:name("+shortId+")";
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
     * @see WorkAuthorityDocumentModelHandler.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param workName  
     * @return a display name
     */
    public static String prepareDefaultDisplayName(
            String workName ) {
        StringBuilder newStr = new StringBuilder();
            newStr.append(workName);
        return newStr.toString();
    }
    
    public static List<WorkTermGroup> getTermGroupInstance(String identifier) {
        if (Tools.isBlank(identifier)) {
            identifier = getGeneratedIdentifier();
        }
        List<WorkTermGroup> terms = new ArrayList<WorkTermGroup>();
        WorkTermGroup term = new WorkTermGroup();
        term.setTermDisplayName(identifier);
        term.setTermName(identifier);
        terms.add(term);
        return terms;
    }
    
    private static List<WorkTermGroup> getTermGroupInstance(String shortIdentifier, String displayName) {
        if (Tools.isBlank(shortIdentifier)) {
            shortIdentifier = getGeneratedIdentifier();
        }
        if (Tools.isBlank(shortIdentifier)) {
            displayName = shortIdentifier;
        }
        
        List<WorkTermGroup> terms = new ArrayList<WorkTermGroup>();
        WorkTermGroup term = new WorkTermGroup();
        term.setTermDisplayName(displayName);
        term.setTermName(shortIdentifier);
        terms.add(term);
        return terms;
    }
    
    private static String getGeneratedIdentifier() {
        return "id" + new Date().getTime(); 
   }
    
    public static PoxPayloadOut createWorkInstance(String shortIdentifier, String displayName,
            String serviceItemCommonPartName) {
        List<WorkTermGroup> terms = getTermGroupInstance(shortIdentifier, displayName);
        
        Map<String, String> workInfo = new HashMap<String, String>();
        workInfo.put(WorkJAXBSchema.SHORT_IDENTIFIER, shortIdentifier);
        
        final Map<String, List<String>> EMPTY_WORK_REPEATABLES_INFO = new HashMap<String, List<String>>();

        return createWorkInstance(null, workInfo, terms, EMPTY_WORK_REPEATABLES_INFO, serviceItemCommonPartName);
    }

    private static PoxPayloadOut createWorkInstance(Object object, Map<String, String> orgInfo,
            List<WorkTermGroup> terms, Map<String, List<String>> workRepeatablesInfo,
            String serviceItemCommonPartName) {
        
        WorksCommon work = new WorksCommon();
        String shortId = orgInfo.get(WorkJAXBSchema.SHORT_IDENTIFIER);
        if (shortId == null || shortId.isEmpty()) {
            throw new IllegalArgumentException("shortIdentifier cannot be null or empty");
        }       
        work.setShortIdentifier(shortId);
        
        // Set values in the Term Information Group
        WorkTermGroupList termList = new WorkTermGroupList();
        if (terms == null || terms.isEmpty()) {
            terms = getTermGroupInstance(getGeneratedIdentifier());
        }
        termList.getWorkTermGroup().addAll(terms); 
        work.setWorkTermGroupList(termList);
        
        PoxPayloadOut multipart = new PoxPayloadOut(WorkAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(work, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(serviceItemCommonPartName);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, organization common ", work, WorksCommon.class);
        }

        return multipart;
    }
    
}
