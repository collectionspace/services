/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:
 *
 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org
 *
 *  Copyright © 2009 Regents of the University of California
 *
 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.
 *
 *  You may obtain a copy of the ECL 2.0 License at
 *
 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.movement;

import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

/**
 * MovementResource.java
 *
 * Handles requests to the Movement service, orchestrates the retrieval
 * of relevant resources, and returns responses to the client.
 */
@Path(MovementClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class MovementResource extends ResourceBase {

    final Logger logger = LoggerFactory.getLogger(MovementResource.class);

    @Override
    protected String getVersionString() {
        final String lastChangeRevision = "$LastChangedRevision$";
        return lastChangeRevision;
    }

    @Override
    public String getServiceName() {
        return MovementClient.SERVICE_NAME;
    }

    @Override
    public Class<MovementsCommon> getCommonPartClass() {
        return MovementsCommon.class;
    }
    
    private boolean isSet(String key, MultivaluedMap<String, String> queryParams) {
    	boolean result = false;
    	
    	String value = queryParams.getFirst(key);
    	result = value != null && !value.isEmpty();
    	
    	return result;
    }

//    @Override
//    protected AbstractCommonList getList(MultivaluedMap<String, String> queryParams) {
//        if (isSet(IQueryManager.SEARCH_RELATED_TO_CSID_SUBJECT, queryParams) == false) {
//        	//
//        	// It's not a "related to" query so we can use our normal call to getList and not our CMIS query
//        	//
//        	return super.getList(queryParams);
//        } else {
//        	//
//        	// We need to use CMIS query method since we'll be doing a join with the Relation table
//        	//
//	        try {
//	            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
//	            DocumentHandler handler = createDocumentHandler(ctx);
//	        	String relationToCsid = queryParams.getFirst(IQueryManager.SEARCH_RELATED_TO_CSID_SUBJECT);
//	        		            
//	        	getRepositoryClient(ctx).getFilteredCMIS(ctx, handler);
//
//	            AbstractCommonList list = (AbstractCommonList) handler.getCommonPartList();
//	            return list;
//	        } catch (Exception e) {
//	            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
//	        }
//        }
//    }
    
}
