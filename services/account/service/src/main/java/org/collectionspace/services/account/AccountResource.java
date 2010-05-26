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
package org.collectionspace.services.account;

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

import org.collectionspace.services.account.storage.AccountStorageClient;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.storage.StorageClient;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AccountResource provides RESTful interface to the account service
 */
@Path("/accounts")
@Consumes("application/xml")
@Produces("application/xml")
public class AccountResource
        extends AbstractCollectionSpaceResourceImpl<AccountsCommon, AccountsCommon> {

    /** The service name. */
    final private String serviceName = "accounts";
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(AccountResource.class);
    /** The storage client. */
    final StorageClient storageClient = new AccountStorageClient();

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getVersionString()
     */
    @Override
    protected String getVersionString() {
        /** The last change revision. */
        final String lastChangeRevision = "$LastChangedRevision: 1165 $";
        return lastChangeRevision;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public Class<AccountsCommon> getCommonPartClass() {
        return AccountsCommon.class;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getServiceContextFactory()
     */
    @Override
    public ServiceContextFactory<AccountsCommon, AccountsCommon> getServiceContextFactory() {
        return (ServiceContextFactory<AccountsCommon, AccountsCommon>) RemoteServiceContextFactory.get();
    }

//    private <T> ServiceContext createServiceContext(T obj) throws Exception {
//        ServiceContext ctx = new RemoteServiceContextImpl<T, T>(getServiceName());
//        ctx.setInput(obj);
//        ctx.setDocumentType(AccountsCommon.class.getPackage().getName()); //persistence unit
//        ctx.setProperty("entity-name", AccountsCommon.class.getName());
//        return ctx;
//    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getStorageClient(org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    public StorageClient getStorageClient(ServiceContext<AccountsCommon, AccountsCommon> ctx) {
        //FIXME use ctx to identify storage client
        return storageClient;
    }

//    @Override
//    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
//        DocumentHandler docHandler = ctx.getDocumentHandler();
//        docHandler.setCommonPart(ctx.getInput());
//        return docHandler;
//    }
    /**
     * Creates the account.
     *
     * @param input the input
     *
     * @return the response
     */
    @POST
    public Response createAccount(AccountsCommon input) {
        try {
            ServiceContext<AccountsCommon, AccountsCommon> ctx = createServiceContext(input, AccountsCommon.class);
            DocumentHandler handler = createDocumentHandler(ctx);
            String csid = getStorageClient(ctx).create(ctx, handler);
            UriBuilder path = UriBuilder.fromResource(AccountResource.class);
            path.path("" + csid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity(ServiceMessages.POST_FAILED
                    + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.POST_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createAccount", e);
            }
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.POST_FAILED + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    /**
     * Gets the account.
     * 
     * @param csid the csid
     * 
     * @return the account
     */
    @GET
    @Path("{csid}")
    public AccountsCommon getAccount(
            @PathParam("csid") String csid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getAccount with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("getAccount: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.GET_FAILED + ServiceMessages.MISSING_INVALID_CSID + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        AccountsCommon result = null;
        try {
            ServiceContext<AccountsCommon, AccountsCommon> ctx = createServiceContext((AccountsCommon) null, AccountsCommon.class);
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).get(ctx, csid, handler);
            result = (AccountsCommon) ctx.getOutput();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.GET_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAccount", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.GET_FAILED + "csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAccount", e);
            }
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceMessages.GET_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.GET_FAILED + "csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Gets the account list.
     * 
     * @param ui the ui
     * 
     * @return the account list
     */
    @GET
    @Produces("application/xml")
    public AccountsCommonList getAccountList(
            @Context UriInfo ui) {
        AccountsCommonList accountList = new AccountsCommonList();
        try {
            ServiceContext<AccountsCommon, AccountsCommon> ctx = createServiceContext((AccountsCommon) null, AccountsCommon.class);
            DocumentHandler handler = createDocumentHandler(ctx);
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            DocumentFilter myFilter = handler.createDocumentFilter();
            myFilter.setPagination(queryParams);
            myFilter.setQueryParams(queryParams);
            handler.setDocumentFilter(myFilter);
            getStorageClient(ctx).getFiltered(ctx, handler);
            accountList = (AccountsCommonList) handler.getCommonPartList();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.LIST_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAccountList", e);
            }
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceMessages.LIST_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return accountList;
    }

    /**
     * Update account.
     * 
     * @param csid the csid
     * @param theUpdate the the update
     * 
     * @return the accounts common
     */
    @PUT
    @Path("{csid}")
    public AccountsCommon updateAccount(
            @PathParam("csid") String csid,
            AccountsCommon theUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateAccount with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("updateAccount: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.PUT_FAILED + ServiceMessages.MISSING_INVALID_CSID + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        AccountsCommon result = null;
        try {
            ServiceContext<AccountsCommon, AccountsCommon> ctx = createServiceContext(theUpdate, AccountsCommon.class);
            DocumentHandler handler = createDocumentHandler(ctx);
            getStorageClient(ctx).update(ctx, csid, handler);
            result = (AccountsCommon) ctx.getOutput();
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity(ServiceMessages.PUT_FAILED
                    + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.PUT_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caugth exception in updateAccount", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.PUT_FAILED + "csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceMessages.PUT_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    /**
     * Delete account.
     * 
     * @param csid the csid
     * 
     * @return the response
     */
    @DELETE
    @Path("{csid}")
    public Response deleteAccount(@PathParam("csid") String csid) {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteAccount with csid=" + csid);
        }
        if (csid == null || "".equals(csid)) {
            logger.error("deleteAccount: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.DELETE_FAILED + ServiceMessages.MISSING_INVALID_CSID + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            //FIXME ideally the following two ops shoudl be in the same tx CSPACE-658
            //delete all relationships
            AccountRoleSubResource subResource = new AccountRoleSubResource("accounts/accountroles");
            subResource.deleteAccountRole(csid, SubjectType.ROLE);
            ServiceContext<AccountsCommon, AccountsCommon> ctx = createServiceContext((AccountsCommon) null,
                    AccountsCommon.class);
            getStorageClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.DELETE_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);

        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteAccount", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.DELETE_FAILED + "csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceMessages.DELETE_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    @POST
    @Path("{csid}/accountroles")
    public Response createAccountRole(@PathParam("csid") String accCsid,
            AccountRole input) {
        if (logger.isDebugEnabled()) {
            logger.debug("createAccountRole with accCsid=" + accCsid);
        }
        if (accCsid == null || "".equals(accCsid)) {
            logger.error("createAccountRole: missing accCsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.POST_FAILED + "accountroles account "
                    + ServiceMessages.MISSING_INVALID_CSID + accCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            AccountRoleSubResource subResource =
                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
            String accrolecsid = subResource.createAccountRole(input, SubjectType.ROLE);
            UriBuilder path = UriBuilder.fromResource(AccountResource.class);
            path.path(accCsid + "/accountroles/" + accrolecsid);
            Response response = Response.created(path.build()).build();
            return response;
        } catch (BadRequestException bre) {
            Response response = Response.status(
                    Response.Status.BAD_REQUEST).entity(ServiceMessages.POST_FAILED
                    + bre.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.POST_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in createAccountRole", e);
            }
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.POST_FAILED
                    + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    @GET
    @Path("{csid}/accountroles/{accrolecsid}")
    public AccountRole getAccountRole(
            @PathParam("csid") String accCsid,
            @PathParam("accrolecsid") String accrolecsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("getAccountRole with accCsid=" + accCsid);
        }
        if (accCsid == null || "".equals(accCsid)) {
            logger.error("getAccountRole: missing accCsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.GET_FAILED + "accountroles account "
                    + ServiceMessages.MISSING_INVALID_CSID + accCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        AccountRole result = null;
        try {
            AccountRoleSubResource subResource =
                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
            //get relationships for an account
            result = subResource.getAccountRole(accCsid, SubjectType.ROLE);
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.GET_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAccountRole", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.GET_FAILED + "account csid=" + accrolecsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getAccountRole", e);
            }
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.GET_FAILED + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.GET_FAILED + "account csid=" + accCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        return result;
    }

    @DELETE
    @Path("{csid}/accountroles/{accrolecsid}")
    public Response deleteAccountRole(
            @PathParam("csid") String accCsid,
            @PathParam("accrolecsid") String accrolecsid) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteAccountRole with accCsid=" + accCsid);
        }
        if (accCsid == null || "".equals(accCsid)) {
            logger.error("deleteAccountRole: missing accCsid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    ServiceMessages.DELETE_FAILED + "accountroles account "
                    + ServiceMessages.MISSING_INVALID_CSID + accCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
        try {
            AccountRoleSubResource subResource =
                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
            //delete all relationships for an account
            subResource.deleteAccountRole(accCsid, SubjectType.ROLE);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (UnauthorizedException ue) {
            Response response = Response.status(
                    Response.Status.UNAUTHORIZED).entity(ServiceMessages.DELETE_FAILED
                    + ue.getErrorReason()).type("text/plain").build();
            throw new WebApplicationException(response);
        } catch (DocumentNotFoundException dnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("caught exception in deleteAccountRole", dnfe);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    ServiceMessages.DELETE_FAILED + "account csid=" + accCsid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        } catch (Exception e) {
            logger.error(ServiceMessages.UNKNOWN_ERROR_MSG, e);
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ServiceMessages.DELETE_FAILED + ServiceMessages.UNKNOWN_ERROR_MSG).type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }
}
