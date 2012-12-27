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

package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * ClaimProxy.java
 *
 * $LastChangedRevision: 5284 $
 * $LastChangedDate: 2011-07-22 12:44:36 -0700 (Fri, 22 Jul 2011) $
 */
@Path("/claims/")
@Produces("application/xml")
@Consumes("application/xml")
public interface ClaimProxy extends CollectionSpaceCommonListPoxProxy {

    // Sorted list
    @GET
    @Produces({"application/xml"})
    ClientResponse<AbstractCommonList> readListSortedBy(
        @QueryParam(IClientQueryParams.ORDER_BY_PARAM) String sortFieldName);
    
    @Override
	@GET
    @Produces({"application/xml"})
    ClientResponse<AbstractCommonList> readIncludeDeleted(
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

    @Override
    @GET
    @Produces({"application/xml"})
    ClientResponse<AbstractCommonList> keywordSearchIncludeDeleted(
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

    @GET
    @Produces({"application/xml"})
    ClientResponse<AbstractCommonList> keywordSearchSortedBy(
        @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
        @QueryParam(IClientQueryParams.ORDER_BY_PARAM) String sortFieldName);
}
