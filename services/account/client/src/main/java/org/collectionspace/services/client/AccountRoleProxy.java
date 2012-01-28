/**
 * AccountRoleProxy.java
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
 * Copyright (C) 2009 {Contributing Institution}
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.authorization.AccountRole;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/accounts")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface AccountRoleProxy extends CollectionSpaceProxy<AbstractCommonList> {

    //(C)reate
    @POST
    @Path("/{csid}/accountroles")
    ClientResponse<Response> create(@PathParam("csid") String csid, AccountRole accRole);

    //(R)ead
    @GET
    @Path("/{csid}/accountroles")
    ClientResponse<AccountRole> read(@PathParam("csid") String csid);

    //(R)ead
    @GET
    @Path("/{csid}/accountroles/{arcsid}")
    ClientResponse<AccountRole> read(@PathParam("csid") String csid,
            @PathParam("arcsid") String arcsid);

    //(D)elete
    @POST
    @Path("/{csid}/accountroles")
    ClientResponse<Response> delete(@PathParam("csid") String csid,
            @QueryParam("_method") String method,
            AccountRole accRole);

    //(U)pdate
    @PUT
    @Path("/{csid}/accountroles")
    ClientResponse<AccountRole> update(@PathParam("csid") String csid, AccountRole role);
    
    //(D)elete
    @Override
	@DELETE
    @Path("/{csid}/accountroles")
    ClientResponse<Response> delete(@PathParam("csid") String csid);
}
