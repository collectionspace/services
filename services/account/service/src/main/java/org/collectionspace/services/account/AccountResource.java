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

import org.collectionspace.services.account.storage.AccountStorageClient;
import org.collectionspace.services.authorization.AccountPermission;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.AccountRoleRel;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.client.AccountClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.common.SecurityResourceBase;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;


/** AccountResource provides RESTful interface to the account service  */
@Path(AccountClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AccountResource extends SecurityResourceBase {

    final Logger logger = LoggerFactory.getLogger(AccountResource.class);
    final StorageClient storageClient = new AccountStorageClient();

    @Override
    protected String getVersionString() {
        return "$LastChangedRevision: 1165 $";
    }

    @Override
    public String getServiceName() {
        return AccountClient.SERVICE_NAME;
    }

    @Override
    public Class<AccountsCommon> getCommonPartClass() {
        return AccountsCommon.class;
    }

    @Override
    public ServiceContextFactory<AccountsCommon, AccountsCommon> getServiceContextFactory() {
        return (ServiceContextFactory<AccountsCommon, AccountsCommon>) RemoteServiceContextFactory.get();
    }

    @Override
    public StorageClient getStorageClient(ServiceContext ctx) {
        //FIXME use ctx to identify storage client
        return storageClient;
    }

    @POST
    public Response createAccount(AccountsCommon input) {
        return create(input);
    }

    @GET
    @Path("{csid}")
    public AccountsCommon getAccount(@PathParam("csid") String csid) {
        return (AccountsCommon)get(csid, AccountsCommon.class);
    }

    @GET
    @Produces("application/xml")
    public AccountsCommonList getAccountList(@Context UriInfo ui) {
    	AccountsCommonList result = (AccountsCommonList)getList(ui, AccountsCommon.class);
    	if(logger.isTraceEnabled()) {
        	PayloadOutputPart ppo = new PayloadOutputPart(AccountsCommonList.class.getSimpleName(),
        			result);
    		System.out.println(ppo.asXML());
    	}
    	return result;
    }

    @PUT
    @Path("{csid}")
    public AccountsCommon updateAccount(@PathParam("csid") String csid,AccountsCommon theUpdate) {
        return (AccountsCommon)update(csid, theUpdate, AccountsCommon.class);
    }


    @DELETE
    @Path("{csid}")
    public Response deleteAccount(@Context UriInfo uriInfo, @PathParam("csid") String csid) {
        logger.debug("deleteAccount with csid=" + csid);
        ensureCSID(csid, ServiceMessages.DELETE_FAILED);
        try {
        	AccountsCommon account = (AccountsCommon)get(csid, AccountsCommon.class);
            // If marked as metadata immutable, do not delete
            if(AccountClient.IMMUTABLE.equals(account.getMetadataProtection())) {
                Response response = 
                	Response.status(Response.Status.FORBIDDEN).entity("Account: "+csid+" is immutable.").type("text/plain").build();
                return response;
            }
            //FIXME ideally the following two ops should be in the same tx CSPACE-658
            //delete all relationships
            AccountRoleSubResource subResource = new AccountRoleSubResource("accounts/accountroles");
            subResource.deleteAccountRole(csid, SubjectType.ROLE);
            ServiceContext<AccountsCommon, AccountsCommon> ctx = createServiceContext((AccountsCommon) null,
                    AccountsCommon.class, uriInfo);
            getStorageClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        }

    }

    @POST
    @Path("{csid}/accountroles")
    public Response createAccountRole(@QueryParam("_method") String method,
            @PathParam("csid") String accCsid,
            AccountRole input) {
        if (method != null) {
            if ("delete".equalsIgnoreCase(method)) {
                return deleteAccountRole(accCsid, input);
            }
        }
        logger.debug("createAccountRole with accCsid=" + accCsid);
        ensureCSID(accCsid, ServiceMessages.POST_FAILED+ "accountroles account ");
        try {
        	AccountsCommon account = (AccountsCommon)get(accCsid, AccountsCommon.class);
            // If marked as roles immutable, do not create
            if(AccountClient.IMMUTABLE.equals(account.getRolesProtection())) {
                Response response = 
                	Response.status(Response.Status.FORBIDDEN).entity("Roles for Account: "+accCsid+" are immutable.").type("text/plain").build();
                return response;
            }
            AccountRoleSubResource subResource =
                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
            String accrolecsid = subResource.createAccountRole(input, SubjectType.ROLE);
            UriBuilder path = UriBuilder.fromResource(AccountResource.class);
            path.path(accCsid + "/accountroles/" + accrolecsid);
            Response response = Response.created(path.build()).build();
            return response;
         } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED, accCsid);
        }
    }

    @GET
    @Path("{csid}/accountroles/{id}")
    public AccountRoleRel getAccountRole(
            @PathParam("csid") String accCsid,
            @PathParam("id") String accrolecsid) {
        logger.debug("getAccountRole with accCsid=" + accCsid);
        ensureCSID(accCsid, ServiceMessages.GET_FAILED+ "accountroles account ");
        AccountRoleRel result = null;
        try {
            AccountRoleSubResource subResource =
                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
            //get relationships for an account
            result = subResource.getAccountRoleRel(accCsid, SubjectType.ROLE, accrolecsid);
         } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, accCsid);
        }
        checkResult(result, accCsid, ServiceMessages.GET_FAILED);
        return result;
    }
    
    @GET
    @Path("{csid}/accountroles")
    public AccountRole getAccountRole(
            @PathParam("csid") String accCsid) {
        logger.debug("getAccountRole with accCsid=" + accCsid);
        ensureCSID(accCsid, ServiceMessages.GET_FAILED+ "accountroles account ");
        AccountRole result = null;
        try {
            AccountRoleSubResource subResource =
                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
            //get relationships for an account
            result = subResource.getAccountRole(accCsid, SubjectType.ROLE);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, accCsid);
        }
        checkResult(result, accCsid, ServiceMessages.GET_FAILED);
        return result;
    }

    @GET
    @Path("{csid}/accountperms")
    public AccountPermission getAccountPerm(@PathParam("csid") String accCsid) {
        logger.debug("getAccountPerm with accCsid=" + accCsid);
        ensureCSID(accCsid, ServiceMessages.GET_FAILED+ "getAccountPerm account ");
        AccountPermission result = null;
        String userId = "undefined";
        try {
            result = JpaStorageUtils.getAccountPermissions(accCsid);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, accCsid);
        }
        checkResult(result, accCsid, ServiceMessages.GET_FAILED);
        return result;
    }
    
    public Response deleteAccountRole(String accCsid, AccountRole input) {
        logger.debug("deleteAccountRole with accCsid=" + accCsid);
        ensureCSID(accCsid, ServiceMessages.DELETE_FAILED+ "accountroles account ");
        try {
        	AccountsCommon account = (AccountsCommon)get(accCsid, AccountsCommon.class);
            // If marked as roles immutable, do not delete
            if(AccountClient.IMMUTABLE.equals(account.getRolesProtection())) {
                Response response = 
                	Response.status(Response.Status.FORBIDDEN).entity("Roles for Account: "+accCsid+" are immutable.").type("text/plain").build();
                return response;
            }
            AccountRoleSubResource subResource =
                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
            //delete all relationships for an account
            subResource.deleteAccountRole(accCsid, SubjectType.ROLE, input);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, accCsid);
        }
    }
    
    @DELETE
    @Path("{csid}/accountroles")
    public Response deleteAccountRole(@PathParam("csid") String accCsid) {
        logger.debug("deleteAccountRole: All roles related to account with accCsid=" + accCsid);
        ensureCSID(accCsid, ServiceMessages.DELETE_FAILED+ "accountroles account ");
        try {
            // If marked as roles immutable, do not delete
        	AccountsCommon account = (AccountsCommon)get(accCsid, AccountsCommon.class);
            if(AccountClient.IMMUTABLE.equals(account.getRolesProtection())) {
                Response response = 
                	Response.status(Response.Status.FORBIDDEN).entity("Roles for Account: "+accCsid+" are immutable.").type("text/plain").build();
                return response;
            }
            AccountRoleSubResource subResource =
                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
            //delete all relationships for an account
            subResource.deleteAccountRole(accCsid, SubjectType.ROLE);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, accCsid);
        }
    }
    
}
