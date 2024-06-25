/*
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:
 *
 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org
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
package org.collectionspace.services.restrictedmedia;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.collectionspace.services.blob.BlobResource;
import org.collectionspace.services.client.BlobClient;
import org.collectionspace.services.client.CollectionSpaceClientUtils;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RestrictedMediaClient;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobUtil;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(RestrictedMediaClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class RestrictedMediaResource extends NuxeoBasedResource {

    private static final Logger logger = LoggerFactory.getLogger(RestrictedMediaResource.class);

    static final RestrictedMediaClient mediaClient = createMediaClient();

    @Override
    protected String getVersionString() {
        return "$LastChangedRevision$";
    }

    @Override
    public String getServiceName() {
        return RestrictedMediaClient.SERVICE_NAME;
    }

    @Override
    public Class<RestrictedMediaCommon> getCommonPartClass() {
        return RestrictedMediaCommon.class;
    }

    public String getCommonPartName() {
        return mediaClient.getCommonPartName();
    }

    private static RestrictedMediaClient createMediaClient() {
        RestrictedMediaClient result;

        try {
            result = new RestrictedMediaClient();
        } catch (Exception e) {
            String errMsg = "Could not create a new restricted media client for the RestrictedMedia resource.";
            logger.error(errMsg, e);
            throw new RuntimeException();
        }

        return result;
    }

    private String getBlobCsid(String mediaCsid) throws Exception {
        ServiceContext<PoxPayloadIn, PoxPayloadOut> mediaContext = createServiceContext();

        BlobInput blobInput = BlobUtil.getBlobInput(mediaContext);
        blobInput.setSchemaRequested(true);

        // set the blobInput blobCsid
        get(mediaCsid, mediaContext);

        String result = blobInput.getBlobCsid();

        ensureCSID(result, READ);

        return result;
    }

    /*
     * Creates a new media record/resource AND creates a new blob (using a URL pointing to a media file/resource) and
     * associates it with the new media record/resource.
     */
    protected Response createBlobWithUri(UriInfo uriInfo, String blobUri) {
        Response response;

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(BlobClient.SERVICE_NAME, uriInfo);
            BlobInput blobInput = BlobUtil.getBlobInput(ctx);
            blobInput.createBlobFile(blobUri);

            // By now the binary bits have been created, and we just need to create the metadata blob
            // record -this info is in the blobInput var
            response = create(null, ctx);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
        }

        return response;
    }

    /*
     * Looks for a blobUri query param from a POST.  If it finds one then it creates a blob AND a media resource and
     * associates them.
     *
     * @see org.collectionspace.services.common.ResourceBase#create(org.collectionspace.services.common.context.ServiceContext, org.collectionspace.services.common.ResourceMap, javax.ws.rs.core.UriInfo, java.lang.String)
     */
    @Override
    public Response create(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
                           @Context ResourceMap resourceMap,
                           @Context UriInfo uriInfo,
                           String xmlPayload) {
        uriInfo = new UriInfoWrapper(uriInfo);

        // create a blob resource/record first and then the media resource/record
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String blobUri = queryParams.getFirst(BlobClient.BLOB_URI_PARAM);
        if (blobUri != null && !blobUri.isEmpty()) {
            // uses the blob resource and doc handler to create the blob
            Response result = createBlobWithUri(uriInfo, blobUri);
            String blobCsid = CollectionSpaceClientUtils.extractId(result);

            // Add the new blob's csid as an artificial query param -the media doc handler will look for this
            queryParams.add(BlobClient.BLOB_CSID_PARAM, blobCsid);
        }

        return super.create(parentCtx, resourceMap, uriInfo, xmlPayload);
    }

    @Override
    public byte[] update(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
                         @Context ResourceMap resourceMap,
                         @Context UriInfo uriInfo,
                         @PathParam("csid") String csid,
                         String xmlPayload) {
        uriInfo = new UriInfoWrapper(uriInfo);
        // If we find a "blobUri" query param, then we need to create a blob resource/record first and then the media
        // resource/record
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String blobUri = queryParams.getFirst(BlobClient.BLOB_URI_PARAM);
        if (blobUri != null && !blobUri.isEmpty()) {
            // uses the blob resource and doc handler to create the blob
            Response blobResponse = createBlobWithUri(uriInfo, blobUri);
            String blobCsid = CollectionSpaceClientUtils.extractId(blobResponse);
            // Add the new blob's csid as an artificial query param -the media doc handler will look
            queryParams.add(BlobClient.BLOB_CSID_PARAM, blobCsid);
        }

        // finish the media resource PUT request
        return super.update(parentCtx, resourceMap, uriInfo, csid, xmlPayload);
    }

    /*
     * Creates a new blob (using a URL pointing to a media file/resource) and associate it with
     * an existing media record/resource.
     */
    @POST
    @Path("{csid}")
    @Consumes("application/xml")
    @Produces("application/xml")
    public Response createBlobWithUriAndUpdateMedia(@PathParam("csid") String csid,
                                                    @QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri) {
        Response response;

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(BlobClient.SERVICE_NAME);
            BlobInput blobInput = BlobUtil.getBlobInput(ctx);
            blobInput.createBlobFile(blobUri);
            response = create(null, ctx);

            // Next, update the RestrictedMedia record to be linked to the blob
            // and put the blobInput into the RestrictedMedia context
            ServiceContext<PoxPayloadIn, PoxPayloadOut> mediaContext = createServiceContext();
            BlobUtil.setBlobInput(mediaContext, blobInput);
            update(csid, null, mediaContext);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
        }

        return response;
    }

    @PUT
    @Path("{csid}/blob")
    @Consumes("multipart/form-data")
    @Produces("application/xml")
    public Response updateMediaByCreatingBlob(
            @Context HttpServletRequest req,
            @PathParam("csid") String csid,
            @QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri) {
        return createBlob(req, csid, blobUri);
    }

    /*
     * Creates a new blob (using the incoming multipart form data) and associates it with an existing media
     * record/resource. If a URL query param is passed in as well, we use the URL to create the new blob instead of
     * the multipart form data.
     *
     * todo: Why is this deprecated?
     */
    @POST
    @Path("{csid}")
    @Consumes("multipart/form-data")
    @Produces("application/xml")
    @Deprecated
    public Response createBlob(
            @Context HttpServletRequest req,
            @PathParam("csid") String csid,
            @QueryParam(BlobClient.BLOB_URI_PARAM) String blobUri) {
        Response response;
        try {
            if (blobUri == null) {
                ServiceContext<PoxPayloadIn, PoxPayloadOut> blobContext =
                        createServiceContext(BlobClient.SERVICE_NAME, (PoxPayloadIn) null);
                BlobInput blobInput = BlobUtil.getBlobInput(blobContext);
                blobInput.createBlobFile(req, null);
                response = create(null, blobContext);

                // Next, update the Media record to be linked to the blob and put the blobInput into the Media context
                ServiceContext<PoxPayloadIn, PoxPayloadOut> mediaContext = createServiceContext();
                BlobUtil.setBlobInput(mediaContext, blobInput);
                update(csid, null, mediaContext);
            } else {
                // A URI query param overrides the incoming multipart/form-data payload in the request
                response = createBlobWithUriAndUpdateMedia(csid, blobUri);
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
        }

        return response;
    }

    @GET
    @Path("{csid}/blob")
    public byte[] getBlobInfo(@PathParam("csid") String csid) {
        PoxPayloadOut result;

        try {
            String blobCsid = getBlobCsid(csid);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> blobContext = createServiceContext(BlobClient.SERVICE_NAME);
            result = get(blobCsid, blobContext);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }

        return result.getBytes();
    }

    @GET
    @Path("{csid}/blob/content")
    public Response getBlobContent(
            @PathParam("csid") String csid, @Context Request requestInfo, @Context UriInfo uriInfo) {
        Response result;
        BlobResource blobResource = new BlobResource();

        try {
            ensureCSID(csid, READ);
            String blobCsid = getBlobCsid(csid);
            result = blobResource.getBlobContent(blobCsid, requestInfo, uriInfo);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }

        return result;
    }

    @GET
    @Path("{csid}/blob/derivatives/{derivativeTerm}/content")
    public Response getDerivativeContent(
            @PathParam("csid") String csid,
            @PathParam("derivativeTerm") String derivativeTerm,
            @Context Request requestInfo,
            @Context UriInfo uriInfo) {
        Response result;
        BlobResource blobResource = new BlobResource();

        try {
            ensureCSID(csid, READ);
            String blobCsid = getBlobCsid(csid);
            result = blobResource.getDerivativeContent(blobCsid, derivativeTerm, requestInfo, uriInfo);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }

        return result;
    }

    @GET
    @Path("{csid}/blob/derivatives/{derivativeTerm}")
    public byte[] getDerivative(@PathParam("csid") String csid, @PathParam("derivativeTerm") String derivativeTerm) {
        PoxPayloadOut result;
        BlobResource blobResource = new BlobResource();

        try {
            ensureCSID(csid, READ);
            String blobCsid = getBlobCsid(csid);
            String xmlPayload = blobResource.getDerivative(blobCsid, derivativeTerm);
            result = new PoxPayloadOut(xmlPayload.getBytes());
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }

        return result.getBytes();
    }

    @GET
    @Path("{csid}/blob/derivatives")
    @Produces("application/xml")
    public CommonList getDerivatives(@PathParam("csid") String csid) {
        CommonList result = null;
        BlobResource blobResource = new BlobResource();

        try {
            ensureCSID(csid, READ);
            String blobCsid = getBlobCsid(csid);
            result = blobResource.getDerivatives(blobCsid);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
        }

        return result;
    }

    @DELETE
    @Path("{csid}")
    @Override
    public Response delete(@PathParam("csid") String csid) {
        BlobResource blob = new BlobResource();

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            PoxPayloadOut mediaPayload = get(csid, ctx);
            PayloadOutputPart mediaPayloadPart = mediaPayload.getPart(getCommonPartName());
            RestrictedMediaCommon mediaCommon = (RestrictedMediaCommon) mediaPayloadPart.getBody();
            String blobCsid = mediaCommon.getBlobCsid();

            // Delete the blob if it exists.
            if (blobCsid != null && !blobCsid.isEmpty()) {
                Response response = blob.delete(blobCsid);
                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    logger.debug("Problem deleting related blob record of RestrictedMedia record: " +
                                 "RestrictedMedia CSID={} Blob CSID={}", csid, blobCsid);
                }
            }

            return super.delete(ctx, csid);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        }
    }
}
