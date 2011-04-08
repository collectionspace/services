/**
 * PermissionRoleProxy.java
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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;


import org.collectionspace.services.authorization.PermissionRole;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/authorization/permissions")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface PermissionRoleProxy extends CollectionSpaceProxy<PermissionRole> {
    //(C)reate
    @POST
    @Path("/{csid}/permroles")
    ClientResponse<Response> create(@PathParam("csid") String csid, PermissionRole permRole);

    //(R)ead
    @GET
    @Path("/{csid}/permroles")
    ClientResponse<PermissionRole> read(@PathParam("csid") String csid);

    //(R)ead
    @GET
    @Path("/{csid}/permroles/{prcsid}")
    ClientResponse<PermissionRole> read(@PathParam("csid") String csid,
            @PathParam("prcsid") String prcsid);

    //(D)elete
    @DELETE
    @Path("/{csid}/permroles")
    ClientResponse<Response> delete(@PathParam("csid") String csid);
    
    //(D)elete - with a payload
    @POST
    @Path("/{csid}/permroles")
    ClientResponse<Response> delete(@PathParam("csid") String csid,
            @QueryParam("_method") String method,
            PermissionRole permRole);    
}
