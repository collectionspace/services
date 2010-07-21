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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.movement.MovementsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;

/**
 * MovementProxy.java
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
@Path("/movements/")
@Produces({"multipart/mixed"})
@Consumes({"multipart/mixed"})
public interface MovementProxy extends CollectionSpaceProxy {

    //(C)reate
    @POST
    ClientResponse<Response> create(MultipartOutput multipart);

    //(R)ead
    @GET
    @Path("/{csid}")
    ClientResponse<MultipartInput> read(@PathParam("csid") String csid);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    ClientResponse<MultipartInput> update(@PathParam("csid") String csid, MultipartOutput multipart);

    //(D)elete
    @DELETE
    @Path("/{csid}")
    ClientResponse<Response> delete(@PathParam("csid") String csid);
    
    // List
    @GET
    @Produces({"application/xml"})
    ClientResponse<MovementsCommonList> readList();
    
    // Sorted list
    @GET
    @Produces({"application/xml"})
    ClientResponse<MovementsCommonList> readListSortedBy(
        @QueryParam(IClientQueryParams.SORT_BY_PARAM) String sortFieldName);

    @GET
    @Produces({"application/xml"})
    ClientResponse<MovementsCommonList> keywordSearchSortedBy(
        @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
        @QueryParam(IClientQueryParams.SORT_BY_PARAM) String sortFieldName);

    // List Authority References
    @GET
    @Produces({"application/xml"})
    @Path("/{csid}/authorityrefs/")
    ClientResponse<AuthorityRefList> getAuthorityRefs(@PathParam("csid") String csid);
    
}
