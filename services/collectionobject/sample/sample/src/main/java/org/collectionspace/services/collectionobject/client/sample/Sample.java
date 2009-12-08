/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
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

package org.collectionspace.services.collectionobject.client.sample;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;
import org.collectionspace.services.collectionobject.OtherNumberList;
import org.collectionspace.services.collectionobject.domain.naturalhistory.CollectionobjectsNaturalhistory;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample, sample client code for creating and accessing 
 * CollectionObject records.
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class Sample {

    private static final Logger logger =
        LoggerFactory.getLogger(Sample.class);

    // Instance variables specific to this test.
    private CollectionObjectClient client = new CollectionObjectClient();
    final String SERVICE_PATH_COMPONENT = "collectionobjects";


    // ---------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------

    public String createCollectionObject() {

       // Create a CollectionObject record to submit to the service.
       MultipartOutput multipart = createCollectionObjectInstance();

       // Submit a 'create' request to the service, sending the new
       // CollectionObject record to be created, and store the response.
       ClientResponse<Response> res = client.create(multipart);

       // Get the status code from the response and check it against
       // the expected status code.
       if (res.getStatus() != Response.Status.CREATED.getStatusCode()) {
           logger.error("Error creating new CollectionObject. Status code = " +
               res.getStatus());
           return "";
       } else {
           logger.info("CollectionObject created successfully.");
       }

       // Return the new record number for the newly-created record.
       return extractId(res);

   }

    // ---------------------------------------------------------------
    // Read
    // ---------------------------------------------------------------

    public MultipartInput readCollectionObject(String resourceId) throws Exception {

        if (resourceId == null || resourceId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Resource ID must not be null or empty.");
        }

        // Submit the read ("get") request to the service and store the response.
        // The resourceId is a unique identifier for the CollectionObject
        // record we're reading.
        ClientResponse<MultipartInput> res = client.read(resourceId);

        // Get the status code from the response and check it against
        // the expected status code.
        if (res.getStatus() != Response.Status.OK.getStatusCode()) {
            logger.error("Error reading CollectionObject with" +
               "resource ID " + resourceId + ". Status code = " +
               res.getStatus());
            return null;
        }

        // Get the entity body of the response from the service.
        MultipartInput input = (MultipartInput) res.getEntity();

        return input;

   }

   private CollectionobjectsCommonList readCollectionObjectList()
       throws Exception {

        // Submit the read list request to the service and store the response.
        ClientResponse<CollectionobjectsCommonList> res = client.readList();

        // Get the status code from the response and check it against
        // the expected status code.
        if (res.getStatus() != Response.Status.OK.getStatusCode()) {
            logger.error("Error reading list of CollectionObjects. Status code = " +
               res.getStatus());
            return null;
        }

        CollectionobjectsCommonList list = res.getEntity();
        return list;


   }

   // ---------------------------------------------------------------
   // Delete
   // ---------------------------------------------------------------

   private void deleteCollectionObject(String resourceId) {

        if (resourceId == null || resourceId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Resource ID must not be null or empty.");
        }
        
        ClientResponse res = client.delete(resourceId);

        // Get the status code from the response and check it against
        // the expected status code.
        if (res.getStatus() != Response.Status.OK.getStatusCode()) {
            logger.error("Error deleting CollectionObject with" +
               "resource ID " + resourceId + ". Status code = " +
               res.getStatus());
            return;
        }

   }

   private void deleteAllCollectionObjects() throws Exception {

        int recordsDeleted = 0;
        for (String resourceId : getAllResourceIds()) {

            ClientResponse res = client.delete(resourceId);

            // Get the status code from the response and check it against
            // the expected status code.
            if (res.getStatus() != Response.Status.OK.getStatusCode()) {
            logger.error("Error deleting CollectionObject with" +
               "resource ID " + resourceId + ". Status code = " +
               res.getStatus());
            } else {
                recordsDeleted++;
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Deleted " + recordsDeleted + " CollectionObject record(s).");
        }

   }

   // ---------------------------------------------------------------
   // Utility methods
   // ---------------------------------------------------------------

   private MultipartOutput createCollectionObjectInstance() {

       // Create the Common part of a CollectionObject and add data to its fields.

       CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();
       collectionObject.setObjectNumber("some object number here");
       collectionObject.setObjectName("some object name here");
       // The 'other number' field in the common part of a CollectionObject record
       // is multi-valued.
       OtherNumberList onList = new OtherNumberList();
       List<String> ons = onList.getOtherNumber();
       // Note: the structured (URN) format of the values below is for
       // illustrative purposes only.
       ons.add("urn:org.collectionspace.id:24082390");
       ons.add("urn:org.walkerart.id:123");
       collectionObject.setOtherNumbers(onList);
       collectionObject.setAge(""); // Test using an empty String.
       collectionObject.setBriefDescription("Papier mache bird mask with horns, " +
               "painted red with black and yellow spots. " +
               "Puerto Rico. ca. 8&quot; high, 6&quot; wide, projects 10&quot; (with horns).");

       MultipartOutput multipart = new MultipartOutput();
       OutputPart commonPart = multipart.addPart(collectionObject,
               MediaType.APPLICATION_XML_TYPE);
       commonPart.getHeaders().add("label", getCommonPartName());

       if (logger.isInfoEnabled()) {
           logger.info("CollectionObject Common part to be created:");
           logger.info(objectAsXmlString(collectionObject,
               CollectionobjectsCommon.class));
       }

       // Create a "domain" part of a CollectionObject and add data to its fields.
       // This part might come from a community or consortia in a particular domain.
       // There could potentially be multiple "domain" parts in a CollectionObject record.

       // This example adds data to fields for a hypothetical Natural History domain,
       // as in the case of the test fields below ...

       CollectionobjectsNaturalhistory conh = new CollectionobjectsNaturalhistory();
       conh.setNhString("test-string");
       conh.setNhInt(999);
       conh.setNhLong(9999);

       OutputPart nhPart = multipart.addPart(conh, MediaType.APPLICATION_XML_TYPE);
       nhPart.getHeaders().add("label", getNHPartName());

       if (logger.isInfoEnabled()) {
           logger.info("CollectionObject Natural History part to be created:");
           logger.info(objectAsXmlString(conh,
               CollectionobjectsNaturalhistory.class));
       }

       // Return the multipart entity body that will be submitted in the
       // 'create' request, above.
       return multipart;
   }

    private Object extractPart(MultipartInput input, String label,
        Class clazz) throws Exception {
        Object obj = null;
        String partLabel = "";
        List<InputPart> parts = input.getParts();
        if (parts.size() == 0) {
            logger.warn("No parts found in multipart body.");
        }
        if(logger.isInfoEnabled()){
            logger.info("Parts:");
            for(InputPart part : parts){
               partLabel = part.getHeaders().getFirst("label");
               logger.info("part = " + partLabel);
            }
        }
        boolean partLabelMatched = false;
        for(InputPart part : parts){
            partLabel = part.getHeaders().getFirst("label");
            if(label.equalsIgnoreCase(partLabel)){
                partLabelMatched = true;
                if(logger.isInfoEnabled()){
                    logger.info("found part" + partLabel);
                }
                String partStr = part.getBodyAsString();
                if (partStr == null || partStr.trim().isEmpty()) {
                    logger.warn("Part '" + label + "' in multipart body is empty.");
                } else {
                    if (logger.isInfoEnabled()){
                        logger.info("extracted part as str=\n" + partStr);
                    }
                    obj = part.getBody(clazz, null);
                    if(logger.isInfoEnabled()){
                        logger.info("extracted part as obj=\n",
                            objectAsXmlString(obj, clazz));
                    }
                }
                break;
            }
        }
        if (! partLabelMatched) {
            logger.warn("Could not find part '" + label + "' in multipart body.");
        // Handle a potential condition where getBodyAsString() or getBody(),
        // above, do *not* throw an IOException, but getBody() nonetheless
        // retrieves a null object.
        //
        // This *may* be effectively unreachable, but is here as a precaution.
        } else if (obj == null) {
            logger.warn("Could not extract part '" + label +
                "' in multipart body as an object.");
        }
        return obj;
    }

    public void displayCollectionObject(MultipartInput input)
        throws Exception {

        if (input == null) {
            throw new IllegalArgumentException(
                "Could not display null CollectionObject record.");
        }

        // Extract each part of the record, and convert it from
        // its XML representation to its associated Java object.

        // Read the Common part of the record.
        CollectionobjectsCommon collectionObject =
                (CollectionobjectsCommon) extractPart(input,
                client.getCommonPartName(), CollectionobjectsCommon.class);

       if (logger.isInfoEnabled()) {
           logger.info("CollectionObject Common part read:");
           logger.info(objectAsXmlString(collectionObject,
               CollectionobjectsCommon.class));
       }

       // Read the Natural History part of the record.
       CollectionobjectsNaturalhistory conh =
           (CollectionobjectsNaturalhistory) extractPart(input,
               getNHPartName(), CollectionobjectsNaturalhistory.class);

        if (logger.isInfoEnabled()) {
           logger.info("CollectionObject Natural History part read:");
           logger.info(objectAsXmlString(conh,
               CollectionobjectsNaturalhistory.class));
       }

    }


    private String getCommonPartName() {
       return client.getCommonPartName();
    }

    private String getNHPartName() {
       return "collectionobjects_naturalhistory";
    }

    private List<String> getAllResourceIds() throws Exception {
        
        CollectionobjectsCommonList list = readCollectionObjectList();
        List<String> resourceIds = new ArrayList();
        List<CollectionobjectsCommonList.CollectionObjectListItem> items =
                list.getCollectionObjectListItem();

        for (CollectionobjectsCommonList.CollectionObjectListItem item : items) {
            resourceIds.add(item.getCsid());
        }

        return resourceIds;
    }

    private String extractId(ClientResponse<Response> res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        String uri = (String) ((ArrayList<Object>) mvm.get("Location")).get(0);
        if(logger.isInfoEnabled()){
        	logger.info("extractId:uri=" + uri);
        }
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        if(logger.isInfoEnabled()){
        	logger.info("id=" + id);
        }
        return id;
    }

    private String objectAsXmlString(Object o, Class clazz) {
        StringWriter sw = new StringWriter();
        try{
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, sw);
        }catch(Exception e){
            e.printStackTrace();
        }
        return sw.toString();
    }
    
    public static void main(String[] args) throws Exception {

        Sample sample = new Sample();

        // Optionally first delete all existing collection object records.
        boolean ENABLE_DELETE_ALL = false;
        if (ENABLE_DELETE_ALL) {
            logger.info("Deleting all CollectionObject records ...");
            sample.deleteAllCollectionObjects();
        }

        logger.info("Creating a new CollectionObject record ...");
        String newRecordId = sample.createCollectionObject();
        
        if (newRecordId == null || newRecordId.trim().isEmpty()) {
            logger.error("Could not create new record.");
            return;
        }

        logger.info("Reading the new CollectionObject record ...");
        MultipartInput corecord = sample.readCollectionObject(newRecordId);
        sample.displayCollectionObject(corecord);

        logger.info("Deleting the new CollectionObject record ...");
        sample.deleteCollectionObject(newRecordId);
		
    }

}
