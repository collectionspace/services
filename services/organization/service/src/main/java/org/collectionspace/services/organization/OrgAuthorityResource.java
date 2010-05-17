/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

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
package org.collectionspace.services.organization;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.OrgAuthorityJAXBSchema;
import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
//import org.collectionspace.services.common.context.MultipartServiceContext;
//import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
//import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.contact.ContactResource;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.contact.ContactJAXBSchema;
import org.collectionspace.services.contact.nuxeo.ContactDocumentModelHandler;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.organization.nuxeo.OrganizationDocumentModelHandler;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class OrgAuthorityResource.
 */
@Path("/orgauthorities")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class OrgAuthorityResource extends
		AbstractMultiPartCollectionSpaceResourceImpl {

    /** The Constant orgAuthorityServiceName. */
    private final static String orgAuthorityServiceName = "orgauthorities";
    
    /** The Constant organizationServiceName. */
    private final static String organizationServiceName = "organizations";
    
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(OrgAuthorityResource.class);
    //FIXME retrieve client type from configuration
    /** The Constant CLIENT_TYPE. */
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();
    
    /** The contact resource. */
    private ContactResource contactResource = new ContactResource();

    /**
     * Instantiates a new org authority resource.
     */
    public OrgAuthorityResource() {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getVersionString()
     */
    @Override
    protected String getVersionString() {
    	/** The last change revision. */
    	final String lastChangeRevision = "$LastChangedRevision$";
    	return lastChangeRevision;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return orgAuthorityServiceName;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getCommonPartClass()
     */
    @Override
    public Class<OrgauthoritiesCommon> getCommonPartClass() {
    	return OrgauthoritiesCommon.class;
    }    

    /**
     * Gets the item service name.
     * 
     * @return the item service name
     */
    public String getItemServiceName() {
        return organizationServiceName;
    }

    /**
     * Gets the contact service name.
     * 
     * @return the contact service name
     */
    public String getContactServiceName() {
        return contactResource.getServiceName();
    }

    /*
    public RemoteServiceContext createItemServiceContext(MultipartInput input) throws Exception {
    RemoteServiceContext ctx = new RemoteServiceContextImpl(getItemServiceName());
    ctx.setInput(input);
    return ctx;
    }
     */
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#createDocumentHandler(org.collectionspace.services.common.context.ServiceContext)
     */
//    @Override
//    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
//        DocumentHandler docHandler =ctx.getDocumentHandler();
//        if (ctx.getInput() != null) {
//            Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx.getCommonPartLabel(), OrgauthoritiesCommon.class);
//            if (obj != null) {
//                docHandler.setCommonPart((OrgauthoritiesCommon) obj);
//            }
//        }
//        return docHandler;
//    }

    /**
     * Creates the item document handler.
     * 
     * @param ctx the ctx
     * @param inAuthority the in authority
     * 
     * @return the document handler
     * 
     * @throws Exception the exception
     */
    private DocumentHandler createItemDocumentHandler(
    		ServiceContext<MultipartInput, MultipartOutput> ctx,
            String inAuthority) throws Exception {    	
    	OrganizationDocumentModelHandler docHandler = (OrganizationDocumentModelHandler)createDocumentHandler(
    			ctx,
    			ctx.getCommonPartLabel(getItemServiceName()),
    			OrganizationsCommon.class);        	
        docHandler.setInAuthority(inAuthority);
        
        
//        ((OrganizationDocumentModelHandler) docHandler).setInAuthority(inAuthority);
//        if (ctx.getInput() != null) {
//            Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx.getCommonPartLabel(getItemServiceName()),
//                    OrganizationsCommon.class);
//            if (obj != null) {
//                docHandler.setCommonPart((OrganizationsCommon) obj);
//            }
//        }

        return docHandler;
    }

    /**
     * Creates the contact document handler.
     * 
     * @param ctx the ctx
     * @param inAuthority the in authority
     * @param inItem the in item
     * 
     * @return the document handler
     * 
     * @throws Exception the exception
     */
    private DocumentHandler createContactDocumentHandler(
    		ServiceContext<MultipartInput, MultipartOutput> ctx, String inAuthority,
            String inItem) throws Exception {
    	
    	ContactDocumentModelHandler docHandler = (ContactDocumentModelHandler)createDocumentHandler(
    			ctx,
    			ctx.getCommonPartLabel(getContactServiceName()),
    			ContactsCommon.class);        	
        docHandler.setInAuthority(inAuthority);
        docHandler.setInItem(inItem);
    	
//        DocumentHandler docHandler = ctx.getDocumentHandler();
//        // Set the inAuthority and inItem values, which specify the
//        // parent authority (e.g. PersonAuthority, OrgAuthority) and the item
//        // (e.g. Person, Organization) with which the Contact is associated.
//        ((ContactDocumentModelHandler) docHandler).setInAuthority(inAuthority);
//        ((ContactDocumentModelHandler) docHandler).setInItem(inItem);
//        if (ctx.getInput() != null) {
//            Object obj = ((MultipartServiceContext) ctx)
//                .getInputPart(ctx.getCommonPartLabel(getContactServiceName()),
//                ContactsCommon.class);
//            if (obj != null) {
//                docHandler.setCommonPart((ContactsCommon) obj);
//            }
//        }
        return docHandler;
    }

    /**
     * Creates the org authority.
     * 
     * @param input the input
     * 
     * @return the response
     */
    @POST
    public Response createOrgAuthority(MultipartInput input) {
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            //orgAuthorityObject.setCsid(csid);
            UriBuilder path = UriBuilder.fromResource(OrgAuthorityResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createOrgAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the org authority by name.
     * 
     * @param specifier the specifier
     * 
     * @return the org authority by name
     */
    @GET
    @Path("urn:cspace:name({specifier})")
    public MultipartOutput getOrgAuthorityByName(@PathParam("specifier") String specifier) {
        String idValue = null;
        if (specifier == null) {
            logger.error("getOrgAuthority: missing name!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on OrgAuthority (missing specifier)").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        String whereClause =
        	OrgAuthorityJAXBSchema.ORGAUTHORITIES_COMMON+
        	":"+OrgAuthorityJAXBSchema.DISPLAY_NAME+
        	"='"+specifier+"'";
        // We only get a single doc - if there are multiple,
        // it is an error in use.

        if (logger.isDebugEnabled()) {
            logger.debug("getOrgAuthority with name=" + specifier);
        } 
        MultipartOutput result = null;
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            DocumentFilter myFilter = new DocumentFilter(whereClause, 0, 1);
            handler.setDocumentFilter(myFilter);
            getRepositoryClient(ctx).get(ctx, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getOrgAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on OrgAuthority spec=" + specifier).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getOrgAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested OrgAuthority spec:" + specifier + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the entities referencing this Organization instance. The service type
     * can be passed as a query param "type", and must match a configured type
     * for the service bindings. If not set, the type defaults to
     * ServiceBindingUtils.SERVICE_TYPE_PROCEDURE.
     * @param parentcsid 
     * 
     * @param csid the parent csid
     * @param itemcsid the person csid
     * @param ui the ui
     * 
     * @return the info for the referencing objects
     */
    @GET
    @Path("{csid}/items/{itemcsid}/refObjs")
    @Produces("application/xml")
    public AuthorityRefDocList getReferencingObjects(
    		@PathParam("csid") String parentcsid,
    		@PathParam("itemcsid") String itemcsid,
    		@Context UriInfo ui) {
    	AuthorityRefDocList authRefDocList = null;
    	if (logger.isDebugEnabled()) {
    		logger.debug("getReferencingObjects with parentcsid=" 
    				+ parentcsid + " and itemcsid=" + itemcsid);
    	}
    	if (parentcsid == null || "".equals(parentcsid)
    			|| itemcsid == null || "".equals(itemcsid)) {
    		logger.error("getPerson: missing parentcsid or itemcsid!");
    		Response response = Response.status(Response.Status.BAD_REQUEST).entity(
    				"get failed on Person with parentcsid=" 
    				+ parentcsid + " and itemcsid=" + itemcsid).type(
    				"text/plain").build();
    		throw new WebApplicationException(response);
    	}
    	try {
    		// Note that we have to create the service context for the Items, not the main service
    		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    		ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(), queryParams);
    		DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
    		RepositoryClient repoClient = getRepositoryClient(ctx); 
    		DocumentFilter myFilter = handler.getDocumentFilter();
    		String serviceType = ServiceBindingUtils.SERVICE_TYPE_PROCEDURE;
    		List<String> list = queryParams.remove(ServiceBindingUtils.SERVICE_TYPE_PROP);
    		if (list != null) {
    			serviceType = list.get(0);
    		}
    		DocumentWrapper<DocumentModel> docWrapper = repoClient.getDoc(ctx, itemcsid);
    		DocumentModel docModel = docWrapper.getWrappedObject();
    		String refName = (String)docModel.getPropertyValue(OrganizationJAXBSchema.REF_NAME);

    		authRefDocList = RefNameServiceUtils.getAuthorityRefDocs(ctx,
    				repoClient, 
    				serviceType,
    				refName,
    				myFilter.getPageSize(), myFilter.getStartPage(), true );
    	} catch (UnauthorizedException ue) {
    		Response response = Response.status(
    				Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
    		throw new WebApplicationException(response);
    	} catch (DocumentNotFoundException dnfe) {
    		if (logger.isDebugEnabled()) {
    			logger.debug("getReferencingObjects", dnfe);
    		}
    		Response response = Response.status(Response.Status.NOT_FOUND).entity(
    				"GetReferencingObjects failed with parentcsid=" 
    				+ parentcsid + " and itemcsid=" + itemcsid).type(
    				"text/plain").build();
    		throw new WebApplicationException(response);
    	} catch (Exception e) {	// Includes DocumentException
    		if (logger.isDebugEnabled()) {
    			logger.debug("GetReferencingObjects", e);
    		}
    		Response response = Response.status(
    				Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
    		throw new WebApplicationException(response);
    	}
    	if (authRefDocList == null) {
    		Response response = Response.status(Response.Status.NOT_FOUND).entity(
    				"Get failed, the requested Person CSID:" + itemcsid + ": was not found.").type(
    				"text/plain").build();
    		throw new WebApplicationException(response);
    	}
    	return authRefDocList;
    }

    
    /**
     * Gets the org authority.
     * 
     * @param csid the csid
     * 
     * @return the org authority
     */
    @GET
    @Path("{csid}")
    public MultipartOutput getOrgAuthority(@PathParam("csid") String csid) {
        String idValue = null;
        if (csid == null) {
            logger.error("getOrgAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getOrgAuthority with path(id)=" + csid);
        }
        MultipartOutput result = null;
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getOrgAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getOrgAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested OrgAuthority CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the org authority list.
     * 
     * @param ui the ui
     * 
     * @return the org authority list
     */
    @GET
    @Produces("application/xml")
    public OrgauthoritiesCommonList getOrgAuthorityList(@Context UriInfo ui) {
        OrgauthoritiesCommonList orgAuthorityObjectList = new OrgauthoritiesCommonList();
        try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            DocumentFilter myFilter = handler.getDocumentFilter();
            String nameQ = queryParams.getFirst("refName");
            if (nameQ != null) {
                myFilter.setWhereClause("orgauthorities_common:refName='" + nameQ + "'");
            }
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            orgAuthorityObjectList = (OrgauthoritiesCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getOrgAuthorityList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return orgAuthorityObjectList;
    }

    /**
     * Update org authority.
     * 
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}")
    public MultipartOutput updateOrgAuthority(
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateOrgAuthority with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateOrgAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(theUpdate);
            DocumentHandler handler = createDocumentHandler(ctx);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in updateOrgAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Delete org authority.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteOrgAuthority(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteOrgAuthority with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteOrgAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteOrgAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on OrgAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    /*************************************************************************
     * Organization parts - this is a sub-resource of OrgAuthority
     *************************************************************************/
    @POST
    @Path("{csid}/items")
    public Response createOrganization(@PathParam("csid") String parentcsid, MultipartInput input) {
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(),
        			input);
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            String itemcsid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(OrgAuthorityResource.class);
            path.path(parentcsid + "/items/" + itemcsid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createOrganization", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the organization.
     * 
     * @param csid The organization authority (parent) CSID.
     * @param itemcsid The organization item CSID.
     * 
     * @return the organization.
     */
    @GET
    @Path("{csid}/items/{itemcsid}")
    public MultipartOutput getOrganization(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getOrganization with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)) {
            logger.error("getOrganization: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Organization csid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || "".equals(itemcsid)) {
            logger.error("getOrganization: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on Organization itemcsid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            // Note that we have to create the service context for the Items, not the main service
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName());
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            getRepositoryClient(ctx).get(ctx, itemcsid, handler);
            // TODO should we assert that the item is in the passed orgAuthority?
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getOrganization", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Organization csid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getOrganization", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Organization CSID:" + itemcsid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the authority refs for an Organization item.
     *
     * @param csid The organization authority (parent) CSID.
     * @param itemcsid The organization item CSID.
     *
     * @return the authority refs for the Organization item.
     */
    @GET
    @Path("{csid}/items/{itemcsid}/authorityrefs")
    @Produces("application/xml")
    public AuthorityRefList getOrganizationAuthorityRefs(
    		@PathParam("csid") String parentcsid,
                @PathParam("itemcsid") String itemcsid,
    		@Context UriInfo ui) {
    	AuthorityRefList authRefList = null;
        try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            ServiceContext<MultipartInput, MultipartOutput> ctx =
                createServiceContext(getItemServiceName(), queryParams);
            RemoteDocumentModelHandlerImpl handler =
                (RemoteDocumentModelHandlerImpl) createItemDocumentHandler(ctx, parentcsid);
            DocumentWrapper<DocumentModel> docWrapper =
               getRepositoryClient(ctx).getDoc(ctx, itemcsid);
            List<String> authRefFields =
            	((MultipartServiceContextImpl)ctx).getCommonPartPropertyValues(
            	ServiceBindingUtils.AUTH_REF_PROP, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
            authRefList = handler.getAuthorityRefs(docWrapper, authRefFields);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Failed to retrieve authority references: reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAuthorityRefs", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve authority references").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return authRefList;
    }

    /**
     * Gets the organization list.
     * 
     * @param parentcsid the parentcsid
     * @param partialTerm the partial term
     * @param ui the ui
     * 
     * @return the organization list
     */
    @GET
    @Path("{csid}/items")
    @Produces("application/xml")
    public OrganizationsCommonList getOrganizationList(
            @PathParam("csid") String parentcsid,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @Context UriInfo ui) {
        OrganizationsCommonList organizationObjectList = new OrganizationsCommonList();
        try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            // Note that docType defaults to the ServiceName, so we're fine with that.
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(),
        			queryParams);
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            DocumentFilter myFilter = handler.getDocumentFilter(); //new DocumentFilter();
            myFilter.setWhereClause(OrganizationJAXBSchema.ORGANIZATIONS_COMMON +
            		":" + OrganizationJAXBSchema.IN_AUTHORITY + "=" +
            		"'" + parentcsid + "'");
            
            // AND organizations_common:displayName LIKE '%partialTerm%'
            if (partialTerm != null && !partialTerm.isEmpty()) {
            	String ptClause = "AND " + OrganizationJAXBSchema.ORGANIZATIONS_COMMON +
            		":" + OrganizationJAXBSchema.DISPLAY_NAME +
            		" LIKE " + "'%" + partialTerm + "%'";
            	myFilter.appendWhereClause(ptClause);
            }            
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            organizationObjectList = (OrganizationsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getOrganizationList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return organizationObjectList;
    }

    /**
     * Gets the organization list by auth name.
     * 
     * @param parentSpecifier the parent specifier
     * @param partialTerm the partial term
     * @param ui the ui
     * 
     * @return the organization list by auth name
     */
    @GET
    @Path("urn:cspace:name({specifier})/items")
    @Produces("application/xml")
    public OrganizationsCommonList getOrganizationListByAuthName(
    		@PathParam("specifier") String parentSpecifier,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,            
            @Context UriInfo ui) {
    	OrganizationsCommonList personObjectList = new OrganizationsCommonList();
        try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            String whereClause =
            	OrgAuthorityJAXBSchema.ORGAUTHORITIES_COMMON+
            	":" + OrgAuthorityJAXBSchema.DISPLAY_NAME+
            	"='" + parentSpecifier+"'";
            // Need to get an Authority by name
            ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
            String parentcsid = getRepositoryClient(ctx).findDocCSID(ctx, whereClause);

            ctx = createServiceContext(getItemServiceName(), queryParams);
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            DocumentFilter myFilter = handler.getDocumentFilter();// new DocumentFilter();

            // Add the where clause "organizations_common:inAuthority='" + parentcsid + "'"
            myFilter.setWhereClause(OrganizationJAXBSchema.ORGANIZATIONS_COMMON + ":" +
            		OrganizationJAXBSchema.IN_AUTHORITY + "='" + parentcsid + "'");
            
            // AND organizations_common:displayName LIKE '%partialTerm%'
            if (partialTerm != null && !partialTerm.isEmpty()) {
            	String ptClause = "AND " +
            	OrganizationJAXBSchema.ORGANIZATIONS_COMMON + ":" +
            	OrganizationJAXBSchema.DISPLAY_NAME +
            		" LIKE " +
            		"'%" + partialTerm + "%'";
            	myFilter.appendWhereClause(ptClause);
            }            
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            personObjectList = (OrganizationsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getOrganizationListByAuthName", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return personObjectList;
    }

    /**
     * Update organization.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}/items/{itemcsid}")
    public MultipartOutput updateOrganization(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateOrganization with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)) {
            logger.error("updateOrganization: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Organization parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || "".equals(itemcsid)) {
            logger.error("updateOrganization: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Organization=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            // Note that we have to create the service context for the Items, not the main service
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(),
        			theUpdate);
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            getRepositoryClient(ctx).update(ctx, itemcsid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in updateOrganization", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Organization csid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Delete organization.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}/items/{itemcsid}")
    public Response deleteOrganization(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteOrganization with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)) {
            logger.error("deleteOrganization: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Organization parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || "".equals(itemcsid)) {
            logger.error("deleteOrganization: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Organization=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            // Note that we have to create the service context for the Items, not the main service
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName());
            getRepositoryClient(ctx).delete(ctx, itemcsid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteOrganization", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Organization itemcsid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    /*************************************************************************
     * Contact parts - this is a sub-resource of Organization (or "item")
     *************************************************************************/
    @POST
    @Path("{parentcsid}/items/{itemcsid}/contacts")
    public Response createContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            MultipartInput input) {
        try {
            // Note that we have to create the service context and document
            // handler for the Contact service, not the main service.
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getContactServiceName(), input);
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(OrgAuthorityResource.class);
            path.path("" + parentcsid + "/items/" + itemcsid + "/contacts/" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Create failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createContact", e);
            }
            Response response = Response.status(
                Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Attempt to create Contact failed.")
                .type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    /**
     * Gets the contact list.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param ui the ui
     * 
     * @return the contact list
     */
    @GET
    @Produces({"application/xml"})
    @Path("{parentcsid}/items/{itemcsid}/contacts/")
    public ContactsCommonList getContactList(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @Context UriInfo ui) {
        ContactsCommonList contactObjectList = new ContactsCommonList();
        try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getContactServiceName(),
        			queryParams);
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            DocumentFilter myFilter = handler.getDocumentFilter(); //new DocumentFilter();
            myFilter.setWhereClause(ContactJAXBSchema.CONTACTS_COMMON + ":" +
                ContactJAXBSchema.IN_AUTHORITY +
                "='" + parentcsid + "'" +
                " AND " +
                ContactJAXBSchema.CONTACTS_COMMON + ":" +
                ContactJAXBSchema.IN_ITEM +
                "='" + itemcsid + "'" +
                " AND ecm:isProxy = 0");
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            contactObjectList = (ContactsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getContactsList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return contactObjectList;
    }

    /**
     * Gets the contact.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * 
     * @return the contact
     */
    @GET
    @Path("{parentcsid}/items/{itemcsid}/contacts/{csid}")
    public MultipartOutput getContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid) {
        MultipartOutput result = null;
       if (logger.isDebugEnabled()) {
            logger.debug("getContact with parentCsid=" + parentcsid +
            " itemcsid=" + itemcsid + " csid=" + csid);
        }
        try {
            // Note that we have to create the service context and document
            // handler for the Contact service, not the main service.
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getContactServiceName());
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            getRepositoryClient(ctx).get(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getContact", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND)
                .entity("Get failed, the requested Contact CSID:" + csid + ": was not found.")
                .type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getContact", e);
            }
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Get contact failed")
                .type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND)
                .entity("Get failed, the requested Contact CSID:" + csid + ": was not found.")
                .type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;

    }

    /**
     * Update contact.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{parentcsid}/items/{itemcsid}/contacts/{csid}")
    public MultipartOutput updateContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
       if (logger.isDebugEnabled()) {
            logger.debug("updateContact with parentcsid=" + parentcsid +
            " itemcsid=" + itemcsid + " csid=" + csid);
        }
       if (parentcsid == null || parentcsid.trim().isEmpty()) {
            logger.error("updateContact: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Contact parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || itemcsid.trim().isEmpty()) {
            logger.error("updateContact: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Contact=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (csid == null || csid.trim().isEmpty()) {
            logger.error("updateContact: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Contact=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        MultipartOutput result = null;
        try {
            // Note that we have to create the service context and document
            // handler for the Contact service, not the main service.
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getContactServiceName(), theUpdate);
            DocumentHandler handler = createContactDocumentHandler(ctx, parentcsid, itemcsid);
            getRepositoryClient(ctx).update(ctx, csid, handler);
            result = (MultipartOutput) ctx.getOutput();
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity("Create failed reason " + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Update failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in updateContact", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Contact csid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Delete contact.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{parentcsid}/items/{itemcsid}/contacts/{csid}")
    public Response deleteContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteContact with parentCsid=" + parentcsid +
            " itemcsid=" + itemcsid + " csid=" + csid);
        }
        if (parentcsid == null || parentcsid.trim().isEmpty()) {
            logger.error("deleteContact: missing parentcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete contact failed on parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || itemcsid.trim().isEmpty()) {
            logger.error("deleteContact: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete contact failed on itemcsid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (csid == null || csid.trim().isEmpty()) {
            logger.error("deleteContact: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete contact failed on csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            // Note that we have to create the service context for the
            // Contact service, not the main service.
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getContactServiceName());
            getRepositoryClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
         } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Delete failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
         } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in deleteContact", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND)
                .entity("Delete failed, the requested Contact CSID:" + csid + ": was not found.")
                .type("text/plain").build();
            throw new WebApplicationException(response);
       } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

}
