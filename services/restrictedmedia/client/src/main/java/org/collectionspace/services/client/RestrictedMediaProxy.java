/*
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

/**
 * RestrictedMediaProxy.java
 */
@Path(RestrictedMediaClient.SERVICE_PATH_PROXY)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface RestrictedMediaProxy extends CollectionSpaceCommonListPoxProxy {

    @POST
    @Path("{csid}")
    @Consumes("multipart/form-data")
    Response createBlobFromFormData(@PathParam("csid") String csid, MultipartFormDataOutput formDataOutput);

    /**
     *
     * @param csid
     * @param blobUri
     * @param emptyXML param to force RESTEasy to produce a Content-Type header
     * @return
     */
    @POST
    @Path("{csid}")
    @Produces("application/xml")
    @Consumes("application/xml")
    Response createBlobFromUri(
            @PathParam("csid") String csid, @QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri, String emptyXML);

    @POST
    @Produces("application/xml")
    @Consumes("application/xml")
    Response createMediaAndBlobWithUri(
            byte[] xmlPayload,
            @QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri,
            @QueryParam(BlobClient.BLOB_PURGE_ORIGINAL) boolean purgeOriginal);
}
