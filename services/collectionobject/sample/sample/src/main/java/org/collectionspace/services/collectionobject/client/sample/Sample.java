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

package org.collectionspace.services.collectionobject.client.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.BasicConfigurator;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.test.ServiceRequestType;
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


    // ---------------------------------------------------------------
    // Read
    // ---------------------------------------------------------------

    // ---------------------------------------------------------------
    // Delete
    // ---------------------------------------------------------------

    // ---------------------------------------------------------------
    // Utility methods
    // ---------------------------------------------------------------

    private Object extractPart(MultipartInput input, String label,
        Class clazz) throws Exception {
        Object obj = null;
        for(InputPart part : input.getParts()){
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
        return obj;
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
    protected String invalidStatusCodeMessage(ServiceRequestType requestType, int statusCode) {
        return "Status code '" + statusCode + "' in response is NOT within the expected set: " +
                requestType.validStatusCodesAsString();
    }

    protected String extractId(ClientResponse<Response> res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        String uri = (String) ((ArrayList<Object>) mvm.get("Location")).get(0);
        if(logger.isDebugEnabled()){
        	logger.info("extractId:uri=" + uri);
        }
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        if(logger.isDebugEnabled()){
        	logger.debug("id=" + id);
        }
        return id;
    }
    
    public static void main(String[] args) {
		
    }

}
