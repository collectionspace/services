/**	
 * CollectionObjectProxy.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
//import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
//import javax.ws.rs.core.UriInfo;

//import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.client.CollectionSpaceProxy;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
//import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;

import org.jboss.resteasy.client.ClientResponse;

//import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;

/**
 * @version $Revision:$
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1684
 */
@Path("/" + CollectionObjectClient.SERVICE_PATH_COMPONENT + "/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface CollectionObjectProxy extends CollectionSpaceProxy {

    /**
     * Read list.
     *
     * @return the client response
     */
    @GET
    @Produces({"application/xml"})
    ClientResponse<CollectionobjectsCommonList> readList();
    
//    /**
//     * Read list.  //FIXME: REM - Remove this dead code please.
//     *
//     * @param pageSize the page size
//     * @param pageNumber the page number
//     * @return the client response
//     */
//    @GET
//    @Produces({"application/xml"})
//    @Override
//    ClientResponse<CollectionobjectsCommonList> readList(
//    		@QueryParam(IClientQueryParams.PAGE_SIZE_PARAM) String pageSize,
//    		@QueryParam(IClientQueryParams.START_PAGE_PARAM) String pageNumber);
    

    /**
     * Roundtrip.
     * @param ms 
     *
     * @return the client response
     */
    @GET
    @Path("/{ms}/roundtrip")
    @Produces({"application/xml"})
    ClientResponse<Response> roundtrip(@PathParam("ms") int ms);

    /**
     * Keyword search.
     *
     * @param keywords the keywords
     * @return the client response
     */
    @GET
    @Produces({"application/xml"})
    ClientResponse<CollectionobjectsCommonList> keywordSearch(
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords);

    //(C)reate
    /**
     * Creates the.
     *
     * @param payload the payload
     * @return the client response
     */
    @POST
    ClientResponse<Response> create(String payload);

    //(R)ead
    /**
     * Read.
     *
     * @param csid the csid
     * @return the client response
     */
    @GET
    @Path("/{csid}")
    ClientResponse<String> read(@PathParam("csid") String csid);
    
    @GET
    @Produces({"application/xml"})
    @Path("/{csid}/picture")
    ClientResponse<Response> createPicture(@PathParam("csid") String csid);
    
    // List Authority references
    /**
     * Gets the authority refs.
     *
     * @param csid the csid
     * @return the authority refs
     */
    @GET
    @Produces({"application/xml"})
    @Path("/{csid}/authorityrefs/")
    ClientResponse<AuthorityRefList> getAuthorityRefs(@PathParam("csid") String csid);
    

    //(U)pdate
    /**
     * Update.
     *
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    @PUT
    @Path("/{csid}")
    ClientResponse<String> update(@PathParam("csid") String csid, String payload);

    //(D)elete
    /**
     * Delete.
     *
     * @param csid the csid
     * @return the client response
     */
    @DELETE
    @Path("/{csid}")
    ClientResponse<Response> delete(@PathParam("csid") String csid);
}
