/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 Regents of the University of California

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.note;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;

import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class NoteResource.
 */
@Path("/notes")
@Consumes("application/xml")
@Produces("application/xml;charset=UTF-8")
public class NoteResource extends 
		AbstractMultiPartCollectionSpaceResourceImpl {

    /** The Constant serviceName. */
    private final static String serviceName = "notes";
    
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(NoteResource.class);
    //FIXME retrieve client type from configuration
    /** The Constant CLIENT_TYPE. */
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    /**
     * Instantiates a new note resource.
     */
    public NoteResource() {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getVersionString()
     */
    @Override
    protected String getVersionString() {
    	/** The last change revision. */
    	final String lastChangeRevision = "$LastChangedRevision: 1982 $";
    	return lastChangeRevision;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return serviceName;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getCommonPartClass()
     */
    @Override
    public Class<NotesCommon> getCommonPartClass() {
    	return NotesCommon.class;
    }
    

    /**
		 * Creates the note.
		 * 
		 * @param input the input
		 * 
		 * @return the response
		 */
		@POST
    public Response createNote(String xmlText) {
        try {
            PoxPayloadIn input = new PoxPayloadIn(xmlText);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(NoteResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createNote", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the note.
     * 
     * @param csid the csid
     * 
     * @return the note
     */
    @GET
    @Path("{csid}")
    public String getNote(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getNote with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getNote: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "Get failed on Note csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        PoxPayloadOut result = null;
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getNote", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Note csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getNote", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Note CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result.toXML();
    }

    /**
     * Gets the note list.
     * 
     * @param ui the ui
     * 
     * @return the note list
     */
    @GET
    @Produces("application/xml")
    public NotesCommonList getNoteList(@Context UriInfo ui) {
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        NotesCommonList noteObjectList = new NotesCommonList();
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            noteObjectList = (NotesCommonList) handler.getCommonPartList();
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getNoteList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return noteObjectList;
    }

    /**
     * Update note.
     * 
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}")
    public String updateNote(
            @PathParam("csid") String csid,
            String xmlText) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateNote with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateNote: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Note csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        PoxPayloadOut result = null;
        try {
            PoxPayloadIn update = new PoxPayloadIn(xmlText);
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(update);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = ctx.getOutput();
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in updateNote", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Note csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result.toXML();
    }

    /**
     * Delete note.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteNote(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteNote with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteNote: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "Delete failed on Note csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in deleteNote", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Note csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }
}
