/**	
 * ReportProxy.java
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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.report.ReportsCommonList;
import org.collectionspace.services.person.PersonsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;

/**
 * @version $Revision:$
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1684
 */
@Path("/reports/")
@Produces({"multipart/mixed"})
@Consumes({"multipart/mixed"})
public interface ReportProxy extends CollectionSpaceProxy {

    /**
     * Read list.
     *
     * @return the client response
     */
    @GET
    @Produces({"application/xml"})
    ClientResponse<ReportsCommonList> readList();

    //(C)reate
    /**
     * Creates the.
     *
     * @param multipart the multipart
     * @return the client response
     */
    @POST
    ClientResponse<Response> create(MultipartOutput multipart);

    //(R)ead
    /**
     * Read.
     *
     * @param csid the csid
     * @return the client response
     */
    @GET
    @Path("/{csid}")
    ClientResponse<MultipartInput> read(@PathParam("csid") String csid);

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
    ClientResponse<MultipartInput> update(@PathParam("csid") String csid, MultipartOutput multipart);

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
    
    // List Items
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
    
}
