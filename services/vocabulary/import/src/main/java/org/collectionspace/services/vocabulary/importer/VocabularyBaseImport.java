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
package org.collectionspace.services.vocabulary.importer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;

import org.collectionspace.services.VocabularyItemJAXBSchema;
import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.client.VocabularyClientUtils;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VocabularyServiceTest, carries out tests against a
 * deployed and running Vocabulary Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class VocabularyBaseImport {

    private static final Logger logger =
            LoggerFactory.getLogger(VocabularyBaseImport.class);
    // Instance variables specific to this test.
    private VocabularyClient client = new VocabularyClient();
    final String SERVICE_PATH_COMPONENT = "vocabularies";
    final String ITEM_SERVICE_PATH_COMPONENT = "items";

    public void createEnumeration(String vocabName, List<String> enumValues) {

        // Expected status code: 201 Created
        int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
        // Type of service request being tested
        ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

        if (logger.isDebugEnabled()) {
            logger.debug("Import: Create vocabulary: \"" + vocabName + "\"");
        }
        String baseVocabRefName = VocabularyClientUtils.createVocabularyRefName(vocabName, false);
        String fullVocabRefName = baseVocabRefName + "'" + vocabName + "'";
        MultipartOutput multipart = VocabularyClientUtils.createEnumerationInstance(
                vocabName, fullVocabRefName, client.getCommonPartName());
        ClientResponse<Response> res = client.create(multipart);

        int statusCode = res.getStatus();

        if (!REQUEST_TYPE.isValidStatusCode(statusCode)) {
            throw new RuntimeException("Could not create enumeration: \"" + vocabName
                    + "\" " + VocabularyClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        }
        if (statusCode != EXPECTED_STATUS_CODE) {
            throw new RuntimeException("Unexpected Status when creating enumeration: \""
                    + vocabName + "\", Status:" + statusCode);
        }

        // Store the ID returned from this create operation
        // for additional tests below.
        String newVocabId = VocabularyClientUtils.extractId(res);
        if (logger.isDebugEnabled()) {
            logger.debug("Import: Created vocabulary: \"" + vocabName + "\" ID:"
                    + newVocabId);
        }
        for (String itemName : enumValues) {
            HashMap<String, String> itemInfo = new HashMap<String, String>();
            itemInfo.put(VocabularyItemJAXBSchema.DISPLAY_NAME, itemName);
            VocabularyClientUtils.createItemInVocabulary(newVocabId,
                    baseVocabRefName, itemInfo, client);
        }
    }

    public static void main(String[] args) {

        logger.info("VocabularyBaseImport starting...");

        VocabularyBaseImport vbi = new VocabularyBaseImport();
        final String acquisitionMethodsVocabName = "Acquisition Methods";
        final String entryMethodsVocabName = "Entry Methods";
        final String entryReasonsVocabName = "Entry Reasons";
        final String responsibleDeptsVocabName = "Responsible Departments";

        List<String> acquisitionMethodsEnumValues =
                Arrays.asList("Gift", "Purchase", "Exchange", "Transfer", "Treasure");
        List<String> entryMethodsEnumValues =
                Arrays.asList("In person", "Post", "Found on doorstep");
        List<String> entryReasonsEnumValues =
                Arrays.asList("Enquiry", "Commission", "Loan");
        List<String> respDeptNamesEnumValues =
                Arrays.asList("Antiquities", "Architecture and Design", "Decorative Arts",
                "Ethnography", "Herpetology", "Media and Performance Art",
                "Paintings and Sculpture", "Paleobotany", "Photographs",
                "Prints and Drawings");

        vbi.createEnumeration(acquisitionMethodsVocabName, acquisitionMethodsEnumValues);
        vbi.createEnumeration(entryMethodsVocabName, entryMethodsEnumValues);
        vbi.createEnumeration(entryReasonsVocabName, entryReasonsEnumValues);
        vbi.createEnumeration(responsibleDeptsVocabName, respDeptNamesEnumValues);

        logger.info("VocabularyBaseImport complete.");
    }
}
