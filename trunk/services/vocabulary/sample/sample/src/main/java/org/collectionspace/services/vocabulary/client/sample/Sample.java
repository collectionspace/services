/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c)) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.collectionspace.services.vocabulary.client.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.BasicConfigurator;
//import org.collectionspace.services.VocabularyItemJAXBSchema; 
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.client.VocabularyClientUtils;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.vocabulary.VocabulariesCommon;
import org.collectionspace.services.vocabulary.VocabulariesCommonList;
import org.collectionspace.services.vocabulary.VocabularyitemsCommon;
import org.collectionspace.services.vocabulary.VocabularyitemsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VocabularyServiceTest, carries out tests against a
 * deployed and running Vocabulary Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class Sample {
    private static final Logger logger =
        LoggerFactory.getLogger(Sample.class);

    // Instance variables specific to this test.
    private VocabularyClient client = new VocabularyClient();
    final String SERVICE_PATH_COMPONENT = "vocabularies";
    final String ITEM_SERVICE_PATH_COMPONENT = "items";


    // ---------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------

    public void createEnumeration(String vocabName, List<String> enumValues ) {

    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create vocabulary: \"" + vocabName +"\"");
    	}

        String displaySuffix = "displayName-" + System.currentTimeMillis(); //TODO: Laramie20100728 temp fix, made-up displaySuffix.
        String baseVocabRefName = VocabularyClientUtils.createVocabularyRefName(vocabName, displaySuffix);   //TODO: Laramie20100728 temp fix  was vocabName, false
    	String fullVocabRefName = VocabularyClientUtils.createVocabularyRefName(vocabName, displaySuffix);   //TODO: Laramie20100728 temp fix  was vocabName, true
    	PoxPayloadOut multipart = VocabularyClientUtils.createEnumerationInstance(
    			vocabName, fullVocabRefName, client.getCommonPartName());
    	ClientResponse<Response> res = client.create(multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create enumeration: \""+vocabName
    				+"\" "+ VocabularyClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating enumeration: \""
    				+vocabName +"\", Status:"+ statusCode);
    	}

    	// Store the ID returned from this create operation
    	// for additional tests below.
    	String newVocabId = VocabularyClientUtils.extractId(res);
    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Created vocabulary: \"" + vocabName +"\" ID:"
    				+newVocabId );
    	}
    	for(String itemName : enumValues){
            HashMap<String, String> itemInfo = new HashMap<String, String>();
            itemInfo.put(AuthorityItemJAXBSchema.DISPLAY_NAME, itemName);
    		VocabularyClientUtils.createItemInVocabulary(newVocabId, 
    				baseVocabRefName, itemInfo, client);
    	}
    }
    
   // ---------------------------------------------------------------
   // Read
   // ---------------------------------------------------------------

   private VocabulariesCommonList readVocabularies() {

        // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;

        // Submit the request to the service and store the response.
        ClientResponse<VocabulariesCommonList> res = client.readList();
        VocabulariesCommonList list = res.getEntity();

        int statusCode = res.getStatus();
    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not read list of vocabularies: "
                + VocabularyClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when reading " +
                "list of vocabularies, Status:"+ statusCode);
    	}

        return list;
   }

    private List<String> readVocabularyIds(VocabulariesCommonList list) {

        List<String> ids = new ArrayList<String>();
        List<VocabulariesCommonList.VocabularyListItem> vocabularies =
            list.getVocabularyListItem();
        for (VocabulariesCommonList.VocabularyListItem vocabulary : vocabularies) {
            ids.add(vocabulary.getCsid());
        }
        return ids;
   }
    
   private VocabulariesCommon readVocabulary(String vocabId) {

        // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;

        // Submit the request to the service and store the response.
        VocabulariesCommon vocabulary = null;
        try {
            ClientResponse<String> res = client.read(vocabId);
            int statusCode = res.getStatus();
            if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
                throw new RuntimeException("Could not read vocabulary"
                    + VocabularyClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            }
            if(statusCode != EXPECTED_STATUS_CODE) {
                throw new RuntimeException("Unexpected Status when reading " +
                    "vocabulary, Status:"+ statusCode);
            }
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            vocabulary = (VocabulariesCommon) extractPart(input,
                    client.getCommonPartName(), VocabulariesCommon.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not read vocabulary: ", e);
        }

        return vocabulary;
    }

    private VocabularyitemsCommonList readItemsInVocab(String vocabId) {

        // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;

        // Submit the request to the service and store the response.
                          //  readItemList(String inAuthority, String partialTerm, String keywords)
        ClientResponse<VocabularyitemsCommonList> res =  client.readItemList(vocabId, "", ""); //TODO: figure out these params.  I just put in empty string to make it recompile after refactoring.  Laramie20100728
        VocabularyitemsCommonList list = res.getEntity();

        int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not read items in vocabulary: "
                + VocabularyClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when reading " +
                "items in vocabulary, Status:"+ statusCode);
    	}

        return list;
    }

    private List<String> readVocabularyItemIds(VocabularyitemsCommonList list) {

        List<String> ids = new ArrayList<String>();
        List<VocabularyitemsCommonList.VocabularyitemListItem> items =
            list.getVocabularyitemListItem();
        for (VocabularyitemsCommonList.VocabularyitemListItem item : items) {
            ids.add(item.getCsid());
        }
        return ids;
   }

    // ---------------------------------------------------------------
    // Delete
    // ---------------------------------------------------------------

    private void deleteVocabulary(String vcsid) {
         // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.DELETE;

        ClientResponse<Response> res = client.delete(vcsid);
        int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not delete vocabulary: "
                + VocabularyClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when deleting " +
                "vocabulary, Status:"+ statusCode);
    	}
    }

    private void deleteAllVocabularies() {
        List<String> ids = readVocabularyIds(readVocabularies());
        for (String id : ids) {
            deleteVocabulary(id);
        }
    }

        private void deleteVocabularyItem(String vcsid, String itemcsid) {
         // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.DELETE;

        ClientResponse<Response> res = client.deleteItem(vcsid, itemcsid);
        int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not delete vocabulary item: "
                + VocabularyClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when deleting " +
                "vocabulary item, Status:"+ statusCode);
    	}
    }

    private void deleteAllItemsForVocab(String vocabId) {
        List<String> itemIds = readVocabularyItemIds(readItemsInVocab(vocabId));
        for (String itemId : itemIds) {
            deleteVocabularyItem(vocabId, itemId);
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------

    // Retrieve individual fields of vocabulary records.

    private String displayAllVocabularies(VocabulariesCommonList list) {
        StringBuffer sb = new StringBuffer();
            List<VocabulariesCommonList.VocabularyListItem> vocabularies =
                    list.getVocabularyListItem();
            int i = 0;
        for (VocabulariesCommonList.VocabularyListItem vocabulary : vocabularies) {
            sb.append("vocabulary [" + i + "]" + "\n");
            sb.append(displayVocabularyDetails(vocabulary));
            i++;
        }
        return sb.toString();
    }

    private String displayVocabularyDetails(
        VocabulariesCommonList.VocabularyListItem vocabulary) {
            StringBuffer sb = new StringBuffer();
            sb.append("displayName=" + vocabulary.getDisplayName() + "\n");
            sb.append("vocabType=" + vocabulary.getVocabType() + "\n");
            // sb.append("csid=" + vocabulary.getCsid() + "\n");
            sb.append("URI=" + vocabulary.getUri() + "\n");
        return sb.toString();
    }

    // Retrieve individual fields of vocabulary item records.

    private String displayAllVocabularyItems(VocabularyitemsCommonList list) {
        StringBuffer sb = new StringBuffer();
        List<VocabularyitemsCommonList.VocabularyitemListItem> items =
                list.getVocabularyitemListItem();
        int i = 0;
        for (VocabularyitemsCommonList.VocabularyitemListItem item : items) {
            sb.append("vocabulary item [" + i + "]" + "\n");
            sb.append(displayVocabularyItemDetails(item));
            i++;
        }
        return sb.toString();
    }

    private String displayVocabularyItemDetails(
        VocabularyitemsCommonList.VocabularyitemListItem item) {
            StringBuffer sb = new StringBuffer();
            sb.append("csid=" + item.getCsid() + "\n");
            sb.append("displayName=" + item.getDisplayName() + "\n");
            // sb.append("URI=" + item.getUri() + "\n");
        return sb.toString();
    }

    // TODO this should be moved to a common utils class
    private Object extractPart(PoxPayloadIn input, String label,
        Class clazz) throws Exception {
        Object obj = null;
        obj = input.getPart(label);
        /*
        for(PayloadInputPart part : input.getParts()){
            String partLabel = part.getHeaders().getFirst("label");
            if(label.equalsIgnoreCase(partLabel)){
                String partStr = part.getBodyAsString();
                if(logger.isDebugEnabled()){
                    logger.debug("extracted part str=\n" + partStr);
                }
                obj = part.getBody(clazz, null);
                if(logger.isDebugEnabled()){
                    logger.debug("extracted part obj=\n", obj, clazz);
                }
                break;
            }
        }
        */
        return obj;
    }

	public static void main(String[] args) {

        // Configure logging.
		BasicConfigurator.configure();

        logger.info("VocabularyBaseImport starting...");

		Sample vbi = new Sample();
        VocabulariesCommonList vocabularies;
        List<String> vocabIds;
        String details = "";

        // Optionally delete all vocabularies and vocabulary items.

        boolean ENABLE_DELETE_ALL = false;
        if (ENABLE_DELETE_ALL) {

            logger.info("Deleting all vocabulary items and vocabularies ...");

            // For each vocabulary ...
            vocabularies = vbi.readVocabularies();
            vocabIds = vbi.readVocabularyIds(vocabularies);
            for (String vocabId : vocabIds) {
                logger.info("Deleting all vocabulary items for vocabulary ...");
                vbi.deleteAllItemsForVocab(vocabId);
                logger.info("Deleting vocabulary ...");
                vbi.deleteVocabulary(vocabId);
            }

            logger.info("Reading vocabularies after deletion ...");
            vocabularies = vbi.readVocabularies();
            details = vbi.displayAllVocabularies(vocabularies);
            logger.info(details);

            logger.info("Reading items in each vocabulary after deletion ...");
            vocabIds = vbi.readVocabularyIds(vocabularies);
            for (String vocabId : vocabIds) {
                VocabularyitemsCommonList items = vbi.readItemsInVocab(vocabId);
                details = vbi.displayAllVocabularyItems(items);
                logger.info(details);
            }

        }

        // Create new vocabularies, each populated with vocabulary items.

		final String acquisitionMethodsVocabName = "Acquisition Methods";
		final String entryMethodsVocabName = "Entry Methods";
		final String entryReasonsVocabName = "Entry Reasons";
		final String responsibleDeptsVocabName = "Responsible Departments";

		List<String> acquisitionMethodsEnumValues = 
			Arrays.asList("Gift","Purchase","Exchange","Transfer","Treasure");
		List<String> entryMethodsEnumValues = 
			Arrays.asList("In person","Post","Found on doorstep");
		List<String> entryReasonsEnumValues = 
			Arrays.asList("Enquiry","Commission","Loan");
		List<String> respDeptNamesEnumValues = 
			Arrays.asList("Antiquities","Architecture and Design","Decorative Arts",
									"Ethnography","Herpetology","Media and Performance Art",
									"Paintings and Sculpture","Paleobotany","Photographs",
									"Prints and Drawings");
        
		vbi.createEnumeration(acquisitionMethodsVocabName, acquisitionMethodsEnumValues);
		vbi.createEnumeration(entryMethodsVocabName, entryMethodsEnumValues);
		vbi.createEnumeration(entryReasonsVocabName, entryReasonsEnumValues);
		vbi.createEnumeration(responsibleDeptsVocabName, respDeptNamesEnumValues);

		logger.info("VocabularyBaseImport complete.");

        logger.info("Reading vocabularies and items ...");
        // Get a list of vocabularies.
        vocabularies = vbi.readVocabularies();
        // For each vocabulary ...
        for (VocabulariesCommonList.VocabularyListItem
            vocabulary : vocabularies.getVocabularyListItem()) {
            // Get its display name.
            logger.info(vocabulary.getDisplayName());
            // Get a list of the vocabulary items in this vocabulary.
            VocabularyitemsCommonList items =
                vbi.readItemsInVocab(vocabulary.getCsid());
            // For each vocabulary item ...
            for (VocabularyitemsCommonList.VocabularyitemListItem
                item : items.getVocabularyitemListItem()) {
                // Get its display name.
                logger.info(" " + item.getDisplayName());
            }
        }

        // Sample alternate methods of reading all vocabularies and
        // vocabulary items separately.
        boolean RUN_ADDITIONAL_SAMPLES = false;
        if (RUN_ADDITIONAL_SAMPLES) {

            logger.info("Reading all vocabularies ...");
            details = vbi.displayAllVocabularies(vocabularies);
            logger.info(details);

            logger.info("Reading all vocabulary items ...");
            vocabIds = vbi.readVocabularyIds(vocabularies);
            for (String vocabId : vocabIds) {
                VocabularyitemsCommonList items = vbi.readItemsInVocab(vocabId);
                details = vbi.displayAllVocabularyItems(items);
                logger.info(details);
            }

        }

	}

}
