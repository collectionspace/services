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
package org.collectionspace.services.person;

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

import org.collectionspace.services.PersonAuthorityJAXBSchema;
import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.contact.ContactResource;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.contact.ContactJAXBSchema;
import org.collectionspace.services.contact.nuxeo.ContactDocumentModelHandler;
import org.collectionspace.services.person.nuxeo.PersonDocumentModelHandler;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;

import org.nuxeo.ecm.core.api.DocumentModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PersonAuthorityResource.
 */
@Path("/personauthorities")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class PersonAuthorityResource extends
		AbstractMultiPartCollectionSpaceResourceImpl {

    /** The Constant personAuthorityServiceName. */
    private final static String personAuthorityServiceName = "personauthorities";
    
    /** The Constant personServiceName. */
    private final static String personServiceName = "persons";
    
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(PersonAuthorityResource.class);
    //FIXME retrieve client type from configuration
    /** The Constant CLIENT_TYPE. */
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();
    
    /** The contact resource. */
    private ContactResource contactResource = new ContactResource();

    /**
     * Instantiates a new person authority resource.
     */
    public PersonAuthorityResource() {
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
        return personAuthorityServiceName;
    }

    @Override
    public Class<PersonauthoritiesCommon> getCommonPartClass() {
    	return PersonauthoritiesCommon.class;
    }
    
    /**
     * Gets the item service name.
     * 
     * @return the item service name
     */
    public String getItemServiceName() {
        return personServiceName;
    }

    /**
     * Gets the contact service name.
     * 
     * @return the contact service name
     */
    public String getContactServiceName() {
        return contactResource.getServiceName();
    }

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
    private DocumentHandler createItemDocumentHandler(ServiceContext<MultipartInput, MultipartOutput> ctx,
            String inAuthority) throws Exception {    
        PersonDocumentModelHandler docHandler = (PersonDocumentModelHandler)createDocumentHandler(ctx,
    			ctx.getCommonPartLabel(getItemServiceName()),
    			PersonsCommon.class);        	
        docHandler.setInAuthority(inAuthority);
        
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
    	ContactDocumentModelHandler docHandler = (ContactDocumentModelHandler)createDocumentHandler(ctx,
    			ctx.getCommonPartLabel(getContactServiceName()),
    			ContactsCommon.class);        	
        docHandler.setInAuthority(inAuthority);
        docHandler.setInItem(inItem);
    	
        return docHandler;
    }

    /**
     * Creates the person authority.
     * 
     * @param input the input
     * 
     * @return the response
     */
    @POST
    public Response createPersonAuthority(MultipartInput input) {
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(input);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getRepositoryClient(ctx).create(ctx, handler);
            //personAuthorityObject.setCsid(csid);
            UriBuilder path = UriBuilder.fromResource(PersonAuthorityResource.class);
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
                logger.debug("Caught exception in createPersonAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the person authority by name.
     * 
     * @param specifier the specifier
     * 
     * @return the person authority by name
     */
    @GET
    @Path("urn:cspace:name({specifier})")
    public MultipartOutput getPersonAuthorityByName(@PathParam("specifier") String specifier) {
        if (specifier == null) {
            logger.error("getPersonAuthority: missing name!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on PersonAuthority (missing specifier)").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        String whereClause =
        	PersonAuthorityJAXBSchema.PERSONAUTHORITIES_COMMON+
        	":"+PersonAuthorityJAXBSchema.DISPLAY_NAME+
        	"='"+specifier+"'";
        // We only get a single doc - if there are multiple,
        // it is an error in use.

        if (logger.isDebugEnabled()) {
            logger.debug("getPersonAuthority with name=" + specifier);
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
                logger.debug("getPersonAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on PersonAuthority spec=" + specifier).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPersonAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested PersonAuthority spec:" + specifier + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the entities referencing this Person instance. The service type
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
    @Produces("application/xml") //FIXME: REM do this for CSPACE-1079 in Org authority.
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
    		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    		// Note that we have to create the service context for the Items, not the main service
    		ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(),
    				queryParams);
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
    		String refName = (String)docModel.getPropertyValue(PersonJAXBSchema.REF_NAME);

    		authRefDocList = RefNameServiceUtils.getAuthorityRefDocs(ctx,
    				repoClient, 
    				serviceType,
    				refName,
    				myFilter.getPageSize(), myFilter.getStartPage(), true /*computeTotal*/ );
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

    @GET
    @Path("{csid}")
    public MultipartOutput getPersonAuthority(@PathParam("csid") String csid) {
        if (csid == null) {
            logger.error("getPersonAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on PersonAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getPersonAuthority with path(id)=" + csid);
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
                logger.debug("getPersonAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on PersonAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPersonAuthority", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested PersonAuthority CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the person authority list.
     * 
     * @param ui the ui
     * 
     * @return the person authority list
     */
    @GET
    @Produces("application/xml")
    public PersonauthoritiesCommonList getPersonAuthorityList(@Context UriInfo ui) {
        PersonauthoritiesCommonList personAuthorityObjectList = new PersonauthoritiesCommonList();
        try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);
            String nameQ = queryParams.getFirst("refName");
            if (nameQ != null) {
                handler.getDocumentFilter().setWhereClause("personauthorities_common:refName='" + nameQ + "'");
            }
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            personAuthorityObjectList = (PersonauthoritiesCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getPersonAuthorityList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return personAuthorityObjectList;
    }

    /**
     * Update person authority.
     * 
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}")
    public MultipartOutput updatePersonAuthority(
            @PathParam("csid") String csid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updatePersonAuthority with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updatePersonAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on PersonAuthority csid=" + csid).type(
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
                logger.debug("caugth exception in updatePersonAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on PersonAuthority csid=" + csid).type(
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
     * Delete person authority.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deletePersonAuthority(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deletePersonAuthority with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deletePersonAuthority: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on PersonAuthority csid=" + csid).type(
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
                logger.debug("caught exception in deletePersonAuthority", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on PersonAuthority csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    /**
     * ***********************************************************************
     * Person parts - this is a sub-resource of PersonAuthority
     * ***********************************************************************.
     *
     * @param parentcsid the parentcsid
     * @param input the input
     * @return the response
     */    
    @POST
    @Path("{csid}/items")
    public Response createPerson(@PathParam("csid") String parentcsid, MultipartInput input) {
        try {
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(), input);
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            String itemcsid = getRepositoryClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(PersonAuthorityResource.class);
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
                logger.debug("Caught exception in createPerson", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the person.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * 
     * @return the person
     */
    @GET
    @Path("{csid}/items/{itemcsid}")
    public MultipartOutput getPerson(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getPerson with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
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
        MultipartOutput result = null;
        try {
            // Note that we have to create the service context for the Items, not the main service
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName());
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            getRepositoryClient(ctx).get(ctx, itemcsid, handler);
            // TODO should we assert that the item is in the passed personAuthority?
            result = (MultipartOutput) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Get failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPerson", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on Person csid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPerson", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested Person CSID:" + itemcsid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the person list.
     * 
     * @param parentcsid the parentcsid
     * @param partialTerm the partial term
     * @param ui the ui
     * 
     * @return the person list
     */
    @GET
    @Path("{csid}/items")
    @Produces("application/xml")
    public PersonsCommonList getPersonList(
            @PathParam("csid") String parentcsid,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,            
            @Context UriInfo ui) {
        PersonsCommonList personObjectList = new PersonsCommonList();
        try {
            // Note that docType defaults to the ServiceName, so we're fine with that.
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getItemServiceName(),
        			queryParams);
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);
            // Add the where clause "persons_common:inAuthority='" + parentcsid + "'"
            handler.getDocumentFilter().setWhereClause(PersonJAXBSchema.PERSONS_COMMON + ":" +
            		PersonJAXBSchema.IN_AUTHORITY + "='" + parentcsid + "'");
            
            // AND persons_common:displayName LIKE '%partialTerm%'
            if (partialTerm != null && !partialTerm.isEmpty()) {
            	String ptClause = "AND " +
            	PersonJAXBSchema.PERSONS_COMMON + ":" +
            		PersonJAXBSchema.DISPLAY_NAME +
            		" LIKE " +
            		"'%" + partialTerm + "%'";
            	// handler.getDocumentFilter().appendWhereClause(ptClause, IQueryManager.SEARCH_QUALIFIER_AND);
                handler.getDocumentFilter().appendWhereClause(ptClause, "");

            }            
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            personObjectList = (PersonsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getPersonList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return personObjectList;
    }
    

    /**
     * Gets the person list by auth name.
     * 
     * @param parentSpecifier the parent specifier
     * @param partialTerm the partial term
     * @param ui the ui
     * 
     * @return the person list by auth name
     */
    @GET
    @Path("urn:cspace:name({specifier})/items")
    @Produces("application/xml")
    public PersonsCommonList getPersonListByAuthName(
    		@PathParam("specifier") String parentSpecifier,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,            
            @Context UriInfo ui) {
        PersonsCommonList personObjectList = new PersonsCommonList();
        try {
            String whereClause =
            	PersonAuthorityJAXBSchema.PERSONAUTHORITIES_COMMON+
            	":" + PersonAuthorityJAXBSchema.DISPLAY_NAME+
            	"='" + parentSpecifier+"'";
            // Need to get an Authority by name
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(queryParams);
            String parentcsid = 
            	getRepositoryClient(ctx).findDocCSID(ctx, whereClause);
            ctx = createServiceContext(getItemServiceName(), queryParams);
            DocumentHandler handler = createItemDocumentHandler(ctx, parentcsid);

            // Add the where clause "persons_common:inAuthority='" + parentcsid + "'"
            handler.getDocumentFilter().setWhereClause(PersonJAXBSchema.PERSONS_COMMON + ":" +
            		PersonJAXBSchema.IN_AUTHORITY + "='" + parentcsid + "'");
            
            // AND persons_common:displayName LIKE '%partialTerm%'
            if (partialTerm != null && !partialTerm.isEmpty()) {
            	String ptClause = "AND " +
            	PersonJAXBSchema.PERSONS_COMMON + ":" +
            		PersonJAXBSchema.DISPLAY_NAME +
            		" LIKE " +
            		"'%" + partialTerm + "%'";
            	// handler.getDocumentFilter().appendWhereClause(ptClause, IQueryManager.SEARCH_QUALIFIER_AND);
                handler.getDocumentFilter().appendWhereClause(ptClause, "");

            }            
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            personObjectList = (PersonsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity("Index failed reason " + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getPersonList", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Index failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return personObjectList;
    }

    /**
     * Update person.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param theUpdate the the update
     * 
     * @return the multipart output
     */
    @PUT
    @Path("{csid}/items/{itemcsid}")
    public MultipartOutput updatePerson(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            MultipartInput theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updatePerson with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)) {
            logger.error("updatePerson: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Person parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || "".equals(itemcsid)) {
            logger.error("updatePerson: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "update failed on Person=" + itemcsid).type(
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
                logger.debug("caught exception in updatePerson", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed on Person csid=" + itemcsid).type(
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
     * Delete person.
     * 
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}/items/{itemcsid}")
    public Response deletePerson(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("deletePerson with parentcsid=" + parentcsid + " and itemcsid=" + itemcsid);
        }
        if (parentcsid == null || "".equals(parentcsid)) {
            logger.error("deletePerson: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Person parentcsid=" + parentcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        if (itemcsid == null || "".equals(itemcsid)) {
            logger.error("deletePerson: missing itemcsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "delete failed on Person=" + itemcsid).type(
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
                logger.debug("caught exception in deletePerson", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed on Person itemcsid=" + itemcsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Delete failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    /**
     * ***********************************************************************
     * Contact parts - this is a sub-resource of Person (or "item")
     * ***********************************************************************.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param input the input
     * @return the response
     */
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
            UriBuilder path = UriBuilder.fromResource(PersonAuthorityResource.class);
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
            handler.getDocumentFilter().setWhereClause(ContactJAXBSchema.CONTACTS_COMMON + ":" +
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
        	ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(getContactServiceName(),
        			theUpdate);
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
