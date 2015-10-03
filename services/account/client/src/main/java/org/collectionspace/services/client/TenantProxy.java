/**
 * TenantProxy.java
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.account.Tenant;
import org.collectionspace.services.account.TenantsList;

/**
 * @version $Revision:$
 */
@Path("/tenants/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface TenantProxy extends CollectionSpaceProxy<TenantsList> {

    @GET
    @Produces({"application/xml"})
    Response readList();

    @GET
    @Produces({"application/xml"})
    Response readSearchList(
				@QueryParam("name") String name, 
				@QueryParam("disabled") String disabled);

    //(C)reate
    @POST
    Response create(Tenant multipart);

    //(R)ead
    @GET
    @Path("/{csid}")
    Response read(@PathParam("id") String id);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    Response update(@PathParam("id") String id, Tenant multipart);    
}
