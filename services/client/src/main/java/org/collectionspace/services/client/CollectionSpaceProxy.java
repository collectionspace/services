/**	
 * CollectionSpaceProxy.java
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
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.workflow.WorkflowClient;

/**
 * The Interface CollectionSpaceProxy.
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1684
 */
public interface CollectionSpaceProxy<CLT> {

    //(D)elete
    @DELETE
    @Path("/{csid}")
    Response delete(@PathParam("csid") String csid);
	
    // List Authority References
    @GET
    @Produces({"application/xml"})
    @Path("/{csid}/authorityrefs/")
    Response getAuthorityRefs(@PathParam("csid") String csid); //ClientResponse<AuthorityRefList>
    
    @GET
    @Produces({"application/xml"})
    @Consumes({"application/xml"})    
    @Path("{csid}" + WorkflowClient.SERVICE_PATH)
    Response getWorkflow(@PathParam("csid") String csid);
    
    @PUT
    @Produces({"application/xml"})
    @Consumes({"application/xml"})    
    @Path("{csid}" + WorkflowClient.SERVICE_PATH + "/" + "{transition}")
    Response updateWorkflowWithTransition(@PathParam("csid") String csid, @PathParam("transition") String transition);

    /**
     * Return a payload of meta info about the service
     * @return
     */
    @GET
    @Produces({"application/xml"})
    @Path("/" + CollectionSpaceClient.SERVICE_DESCRIPTION_PATH)
	public Response getServiceDescription();

    /*
     * (R)read List operations
     */
    
    @GET
    @Produces({"application/xml"})
    Response readList();
    
    /**
     * Read list.
     *
     * @param pageSize the page size
     * @param pageNumber the page number
     * @return the client response
     */
    @GET
    @Produces({"application/xml"})
    Response readList(
            @QueryParam(IClientQueryParams.PAGE_SIZE_PARAM) Long pageSize,
    	    @QueryParam(IClientQueryParams.START_PAGE_PARAM) Long pageNumber);
        
    /**
     * Read list.
     * @param sortBy 
     *
     * @param pageSize the page size
     * @param pageNumber the page number
     * @return the client response
     */
    @GET
    @Produces({"application/xml"})
    Response readList(
            @QueryParam(IClientQueryParams.ORDER_BY_PARAM) String sortBy,
            @QueryParam(IClientQueryParams.PAGE_SIZE_PARAM) Long pageSize,
    	    @QueryParam(IClientQueryParams.START_PAGE_PARAM) Long pageNumber);    
}
