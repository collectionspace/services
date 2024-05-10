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

import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.account.storage.AccountStorageClient;
import org.collectionspace.services.account.storage.csidp.TokenStorageClient;
import org.collectionspace.services.authentication.Passwordreset;
import org.collectionspace.services.authentication.Token;
import org.collectionspace.services.authorization.AccountPermission;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.AccountRoleRel;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.client.AccountClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.EmailUtil;
import org.collectionspace.services.common.SecurityResourceBase;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.authorization_mgt.AuthorizationCommon;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.query.UriInfoImpl;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.TransactionContext;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.collectionspace.services.config.ServiceConfig;
import org.collectionspace.services.config.tenant.EmailConfig;
import org.collectionspace.services.config.tenant.TenantBindingType;

import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.persistence.NoResultException;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.DatatypeConverter;


/** AccountResource provides RESTful interface to the account service  */
@Path(AccountClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AccountResource extends SecurityResourceBase<AccountsCommon, AccountsCommon> {

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

    @SuppressWarnings("unchecked")
	@Override
    public ServiceContextFactory<AccountsCommon, AccountsCommon> getServiceContextFactory() {
        return (ServiceContextFactory<AccountsCommon, AccountsCommon>) RemoteServiceContextFactory.get();
    }

    @SuppressWarnings("rawtypes")
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
    public AccountsCommon getAccount(@Context UriInfo ui, @PathParam("csid") String csid) {
    	AccountsCommon result = null;

        result = (AccountsCommon)get(ui, csid, AccountsCommon.class);

    	return result;
    }

    @GET
    @Produces("application/xml")
    public AccountsCommonList getAccountList(@Context UriInfo ui) {
    	UriInfoWrapper uriInfoWrapper = new UriInfoWrapper(ui);
    	AccountsCommonList result = (AccountsCommonList)getList(uriInfoWrapper, AccountsCommon.class);
    	if(logger.isTraceEnabled()) {
        	PayloadOutputPart ppo = new PayloadOutputPart(AccountsCommonList.class.getSimpleName(),
        			result);
    		System.out.println(ppo.asXML());
    	}
    	return result;
    }

    protected UriInfo createUriInfo() throws URISyntaxException {
        return createUriInfo("");
    }

    private UriInfo createUriInfo(String queryString) throws URISyntaxException {
        URI absolutePath = new URI("");
        URI baseUri = new URI("");
        return new UriInfoImpl(absolutePath, baseUri, "", queryString, Collections.<PathSegment>emptyList());
    }

    /**
     * Perform a search off the accounts for using a user ID
     * @param userId
     * @return
     */
    private String getAccountCsid(String userId) {
    	String result = null;

    	try {
			UriInfo uriInfo = createUriInfo(String.format("uid=%s", userId));
			AccountsCommonList accountsCommonList = getAccountList(uriInfo);
			if (accountsCommonList != null && accountsCommonList.getAccountListItem() != null) {
				for (AccountListItem accountListItem: accountsCommonList.getAccountListItem()) {
					if (accountListItem.getUserid().equalsIgnoreCase(userId)) {
						result = accountListItem.getCsid();
						break;
					}
				}
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return result;
    }

    /**
     * Return the list of roles (display name) for a user id.
     *
     * @param userId
     * @return
     */
    public List<String> getAccountRoleDisplayNames(String userId, String tenantId) {
    	List<String> result = null;

    	String accountCsid = getAccountCsid(userId);
    	if (accountCsid != null) {
    		AccountRole accountRole = getAccountRole(accountCsid);
    		if (accountRole != null && accountRole.getRole() != null) {
    			List<RoleValue> roleValueList = accountRole.getRole();
    			if (roleValueList.isEmpty() == false) {
    				result = new ArrayList<String>();
    				for (RoleValue roleValue: roleValueList) {
    					if (roleValue != null) {
	    					String displayName = roleValue.getDisplayName();
	    					if (displayName == null) {
	    						displayName = RoleClient.inferDisplayName(roleValue.getRoleName(), tenantId);
	    					}
	    					result.add(displayName);
    					}
    				}
    			}
    		}
    	}

    	return result;
    }

    @PUT
    @Path("{csid}")
    public AccountsCommon updateAccount(@Context UriInfo ui, @PathParam("csid") String csid, AccountsCommon theUpdate) {
        return (AccountsCommon)update(ui, csid, theUpdate, AccountsCommon.class);
    }

    /*
     * Use this when you have an existing and active ServiceContext. //FIXME: Use this only for password reset
     */
    private AccountsCommon updateAccountPassword(ServiceContext<AccountsCommon, AccountsCommon> parentContext, UriInfo ui, String csid, AccountsCommon theUpdate) {
        return (AccountsCommon)update(parentContext, ui, csid, theUpdate, AccountsCommon.class, false);
    }

    @GET
    @Path(AccountClient.PROCESS_PASSWORD_RESET_PATH_COMPONENT)
    @Produces(MediaType.TEXT_HTML)
    public String processPasswordResetForm(@Context HttpServletRequest request) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
        String tokenId = request.getParameter(AccountClient.PASSWORD_RESET_TOKEN_QP);
        Token token = null;

        try {
            token = TokenStorageClient.get(tokenId);
        } catch (DocumentNotFoundException e) {
        }

        if (token == null || !token.isEnabled()) {
            return String.format("<html><body>The token %s is not valid.</body></html>", tokenId);
        }

        Map<String, Object> uiConfig = new HashMap<>();

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            Map<String, Object> csrfConfig = new HashMap<>();

            csrfConfig.put("parameterName", csrfToken.getParameterName());
            csrfConfig.put("token", csrfToken.getToken());

            uiConfig.put("csrf", csrfConfig);
        }

        String tenantId = token.getTenantId();
        TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
        TenantBindingType tenantBinding = tenantBindingConfigReader.getTenantBinding(tenantId);

        uiConfig.put("token", tokenId);
        uiConfig.put("tenantId", tenantId);
        uiConfig.put("tenantLoginUrl", ConfigUtils.getUILoginSuccessUrl(tenantBinding));

        String uiConfigJS;

        try {
            uiConfigJS = new ObjectMapper().writeValueAsString(uiConfig);
        } catch (JsonProcessingException e) {
            logger.error("Error generating login page UI configuration", e);

            uiConfigJS = "";
        }

        Map<String, String> dataModel = new HashMap<>();

        dataModel.put("uiConfig", uiConfigJS);

        Configuration freeMarkerConfig = ServiceMain.getInstance().getFreeMarkerConfig();
        Template template = freeMarkerConfig.getTemplate("service-ui.ftlh");
        Writer out = new StringWriter();

        template.process(dataModel, out);

        out.close();

        return out.toString();
    }

    /**
     * Resets an accounts password.
     *
     * Requires three query params:
     * 		id = CSID of the account
     * 		token = the password reset token generated by the system
     * 		password = the new password
     *
     * @param ui
     * @return
     * @throws
     * @throws IOException
     */
    @POST
    @Path(AccountClient.PROCESS_PASSWORD_RESET_PATH_COMPONENT)
    synchronized public Response processPasswordReset(Passwordreset passwordreset, @Context UriInfo ui) {
    	Response response = null;

    	//
    	// Create a read/write copy of the UriInfo info
    	//
    	ui = new UriInfoWrapper(ui);
        MultivaluedMap<String,String> queryParams = ui.getQueryParameters();

        //
        // Get the 'token' and 'password' params
        //
        String tokenId = passwordreset.getToken();
        if (tokenId == null || tokenId.trim().isEmpty()) {
        	response = Response.status(Response.Status.BAD_REQUEST).entity(
        			"The query parameter 'token' is missing or contains no value.").type("text/plain").build();
        	return response;
        }

        String base64EncodedPassword = passwordreset.getPassword();
        if (base64EncodedPassword == null || base64EncodedPassword.trim().isEmpty()) {
        	response = Response.status(Response.Status.BAD_REQUEST).entity(
        			"The query parameter 'password' is missing or contains no value.").type("text/plain").build();
        	return response;
        }
        String password = new String(DatatypeConverter.parseBase64Binary(base64EncodedPassword), StandardCharsets.UTF_8);

        //
        // Retrieve the token from the DB
        //
        Token token;
		try {
			token = TokenStorageClient.get(tokenId);
			if (token != null && token.isEnabled() == false) {
				throw new DocumentNotFoundException();
			}
		} catch (DocumentNotFoundException e1) {
    		String errMsg = String.format("The token '%s' is not valid or does not exist.",
    				tokenId);
        	response = Response.status(Response.Status.BAD_REQUEST).entity(errMsg).type("text/plain").build();
        	return response;
		}

		//
		// Make sure the token is not null
		//
        if (token == null) {
    		String errMsg = String.format("The token '%s' is not valid.",
    				tokenId);
        	response = Response.status(Response.Status.BAD_REQUEST).entity(errMsg).type("text/plain").build();
        	return response;
        }

        //
        // From the token, get the account to update.
        //
        queryParams.add(AuthN.TENANT_ID_QUERY_PARAM, token.getTenantId());
        AccountsCommon targetAccount = getAccount(ui, token.getAccountCsid());
        if (targetAccount == null) {
    		String errMsg = String.format("The token '%s' is not valid.  The account it was created for no longer exists.",
    				tokenId);
        	response = Response.status(Response.Status.BAD_REQUEST).entity(errMsg).type("text/plain").build();
        	return response;
        }
        //
        // Finally, try to update the account with the new password.
        //
        String tenantId = token.getTenantId();
    	TenantBindingType tenantBindingType = ServiceMain.getInstance().getTenantBindingConfigReader().getTenantBinding(tenantId);
    	EmailConfig emailConfig = tenantBindingType.getEmailConfig();
    	if (emailConfig != null) {
    		try {
	    		ServiceContext<AccountsCommon, AccountsCommon> ctx = createServiceContext((AccountsCommon) null, AccountsCommon.class, ui);
	            TransactionContext transactionCtx = ctx.openConnection();
	    		try {
					if (AuthorizationCommon.hasTokenExpired(emailConfig, token) == false) {
						transactionCtx.beginTransaction();
						AccountsCommon accountUpdate = new AccountsCommon();
						accountUpdate.setUserId(targetAccount.getUserId());
						accountUpdate.setPassword(password.getBytes());
						updateAccountPassword(ctx, ui, targetAccount.getCsid(), accountUpdate);
						TokenStorageClient.update(transactionCtx, tokenId, false); // disable the token so it can't be used again.
						transactionCtx.commitTransaction();
						//
						// Success!
						//
						String msg = String.format("Successfully reset password using token ID='%s'.", token.getId());
			        	response = Response.status(Response.Status.OK).entity(msg).type("text/plain").build();
			        } else {
			        	String errMsg = String.format("Could not reset password using token with ID='%s'. Password reset token has expired.",
								token.getId());
			        	response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).type("text/plain").build();
			        }
				} catch (Throwable t) {
					transactionCtx.markForRollback();
					transactionCtx.close(); // https://jira.ets.berkeley.edu/jira/browse/CC-241
					String errMsg = String.format("Could not reset password using token ID='%s'. Error: '%s'",
							t.getMessage(), token.getId());
		        	response = Response.status(Response.Status.BAD_REQUEST).entity(errMsg).type("text/plain").build();
				} finally {
					ctx.closeConnection();
				}
    		} catch (Exception e) {
				String errMsg = String.format("Could not reset password using token ID='%s'. Error: '%s'",
						e.getMessage(), token.getId());
	        	response = Response.status(Response.Status.BAD_REQUEST).entity(errMsg).type("text/plain").build();
			}
    	} else {
    		String errMsg = String.format("The email configuration for tenant ID='%s' is missing.  Please ask your CollectionSpace administrator to check the configuration.",
    				tenantId);
        	response = Response.status(Response.Status.BAD_REQUEST).entity(errMsg).type("text/plain").build();
    	}

    	return response;
    }

    @GET
    @Path(AccountClient.PASSWORD_RESET_PATH_COMPONENT)
    @Produces(MediaType.TEXT_HTML)
    public String requestPasswordResetForm(@Context HttpServletRequest request) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
        Map<String, Object> uiConfig = new HashMap<>();

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            Map<String, Object> csrfConfig = new HashMap<>();

            csrfConfig.put("parameterName", csrfToken.getParameterName());
            csrfConfig.put("token", csrfToken.getToken());

            uiConfig.put("csrf", csrfConfig);
        }

        String tenantId = request.getParameter(AuthN.TENANT_ID_QUERY_PARAM);

        if (tenantId != null) {
            uiConfig.put("tenantId", tenantId);
        }

        String uiConfigJS;

        try {
            uiConfigJS = new ObjectMapper().writeValueAsString(uiConfig);
        } catch (JsonProcessingException e) {
            logger.error("Error generating login page UI configuration", e);

            uiConfigJS = "";
        }

        Map<String, String> dataModel = new HashMap<>();

        dataModel.put("uiConfig", uiConfigJS);

        Configuration freeMarkerConfig = ServiceMain.getInstance().getFreeMarkerConfig();
        Template template = freeMarkerConfig.getTemplate("service-ui.ftlh");
        Writer out = new StringWriter();

        template.process(dataModel, out);

        out.close();

        return out.toString();
    }

    @POST
    @Path(AccountClient.PASSWORD_RESET_PATH_COMPONENT)
    public Response requestPasswordReset(@Context UriInfo ui) {
        MultivaluedMap<String,String> queryParams = ui.getQueryParameters();
        String email = queryParams.getFirst(AccountClient.EMAIL_QUERY_PARAM);

        if (StringUtils.isEmpty(email)) {
        	return Response.status(Response.Status.BAD_REQUEST).entity("You must specify an 'email' query paramater.").type("text/plain").build();
        }

        final String tenantId = queryParams.getFirst(AuthN.TENANT_ID_QUERY_PARAM);

        ui = new UriInfoWrapper(ui);

        if (StringUtils.isEmpty(tenantId)) {
            // If no tenant ID was supplied, pick an arbitrary one for purposes of account search.
            // It doesn't matter which, because all accounts will be returned regardless of the
            // tenant ID used to list the accounts.

            TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
            String effectiveTenantId = tenantBindingConfigReader.getTenantIds().get(0);

            ui.getQueryParameters().putSingle(AuthN.TENANT_ID_QUERY_PARAM, effectiveTenantId);
        }

        //
        // Search for an account with the provided email and (optional) tenant ID.
        //
		AccountListItem accountListItem = null;
    	AccountsCommonList accountList = getAccountList(ui);

    	if (accountList != null || accountList.getTotalItems() > 0) {
            accountListItem = accountList.getAccountListItem().stream()
                .filter(new Predicate<AccountListItem>() {
                    @Override
                    public boolean test(AccountListItem item) {
                        if (item == null) {
                            return false;
                        }

                        if (StringUtils.isEmpty(tenantId)) {
                            return true;
                        }

                        String itemTenantId = item.getTenantid();

                        return (itemTenantId != null && itemTenantId.equalsIgnoreCase(tenantId));
                    }
                })
                .findFirst()
                .orElse(null);
        }

        if (accountListItem == null) {
            String msg = String.format(
                StringUtils.isEmpty(tenantId)
                    ? "Could not locate an account associated with the email %s"
                    : "Could not locate an account associated with the email %s and tenant ID '%s'",
                email, tenantId
            );

            return Response.status(Response.Status.NOT_FOUND).entity(msg).type("text/plain").build();
        }

        ServiceConfig serviceConfig = ServiceMain.getInstance().getServiceConfig();

        if (ConfigUtils.isSsoAvailable(serviceConfig) && accountListItem.isRequireSSO() != null && accountListItem.isRequireSSO()) {
        	return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("The account requires single sign-on.").type("text/plain").build();
        }

        // If no tenant ID was supplied, use the account's first associated tenant ID for purposes
        // of password reset. This is the same way that a tenant is selected for the account when
        // logging in. In practice, accounts are only associated with one tenant anyway.

        String targetTenantId = StringUtils.isEmpty(tenantId)
            ? accountListItem.getTenants().get(0).getTenantId()
            : tenantId;

        try {
            return requestPasswordReset(ui, targetTenantId, accountListItem);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type("text/plain").build();
        }
    }

    private boolean contains(String targetTenantID, List<AccountTenant> accountTenantList) {
    	boolean result = false;

    	for (AccountTenant accountTenant : accountTenantList) {
    		if (accountTenant.getTenantId().equalsIgnoreCase(targetTenantID)) {
    			result = true;
    			break;
    		}
    	}

    	return result;
    }

    /*
     * Sends an email to a user allow them to reset their password.
     */
    private Response requestPasswordReset(UriInfo ui, String targetTenantID, AccountListItem accountListItem) throws Exception {
    	Response result = null;

    	if (contains(targetTenantID, accountListItem.getTenants()) == false) {
			String errMsg = String.format("Could not send a password request email to user ID='%s'.  That account is not associated with the targeted tenant ID = '%s'.",
					accountListItem.email, targetTenantID);
        	result = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).type("text/plain").build();
        	return result;
    	}

    	TenantBindingType tenantBindingType = ServiceMain.getInstance().getTenantBindingConfigReader().getTenantBinding(targetTenantID);
    	EmailConfig emailConfig = tenantBindingType.getEmailConfig();
    	if (emailConfig != null) {
    		UriBuilder baseUrlBuilder = ui.getBaseUriBuilder();
        	String deprecatedConfigBaseUrl = emailConfig.getBaseurl();

        	Object[] emptyValues = new String[0];
        	String baseUrl = baseUrlBuilder.build(emptyValues).toString();

        	emailConfig.setBaseurl(baseUrl);
        	//
        	// Configuring (via config files) the base URL is not supported as of CSpace v5.0.  Log a warning if we find config for it.
        	//
        	if (deprecatedConfigBaseUrl != null) {
        		if (deprecatedConfigBaseUrl.equalsIgnoreCase(baseUrl) == false) {
	        		String warnMsg = String.format("Ignoring deprecated 'baseurl' email config value '%s'.  Using '%s' instead.",
	        				deprecatedConfigBaseUrl, baseUrl);
	        		logger.warn(warnMsg);
        		}
        	}

        	Token token = TokenStorageClient.create(accountListItem.getCsid(), targetTenantID,
        			emailConfig.getPasswordResetConfig().getTokenExpirationSeconds());
    		String message = AuthorizationCommon.generatePasswordResetEmailMessage(emailConfig, accountListItem, token);
    		String status = EmailUtil.sendMessage(emailConfig, accountListItem.getEmail(), message);
    		if (status != null) {
    			String errMsg = String.format("Could not send email to %s: %s",
    					accountListItem.email, status);
            	result = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errMsg).type("text/plain").build();
    		} else {
    			String okMsg = accountListItem.getEmail();
    			result = Response.status(Response.Status.OK).entity(okMsg).type("text/plain").build();
    		}
    	} else {
    		String errMsg = String.format("The email configuration for tenant %s is missing. Please ask your CollectionSpace administrator to check the configuration.",
    				targetTenantID);
        	result = Response.status(Response.Status.BAD_REQUEST).entity(errMsg).type("text/plain").build();
    	}

    	return result;
    }

	@DELETE
    @Path("{csid}")
    public Response deleteAccount(@Context UriInfo uriInfo, @PathParam("csid") String csid) {
        logger.debug("deleteAccount with csid=" + csid);
        ensureCSID(csid, ServiceMessages.DELETE_FAILED);

        try {
        	AccountsCommon account = (AccountsCommon)get(csid, AccountsCommon.class);
        	//
            // If marked as metadata immutable, do not delete
            //
        	if (AccountClient.IMMUTABLE.equals(account.getMetadataProtection())) {
                Response response =
                	Response.status(Response.Status.FORBIDDEN).entity("Account: "+csid+" is immutable.").type("text/plain").build();
                return response;
            }
            //
            // We need to delete the account and the account/role relationships in a
            // single transaction
            //
            ServiceContext<AccountsCommon, AccountsCommon> ctx = createServiceContext((AccountsCommon) null,
                    AccountsCommon.class, uriInfo);
            TransactionContext transactionContext = ctx.openConnection();
            try {
            	transactionContext.beginTransaction();
            	//
            	// Delete all the account-role relationships
            	//
	            AccountRoleSubResource subResource = new AccountRoleSubResource("accounts/accountroles");
	            subResource.deleteAccountRole(ctx, csid, SubjectType.ROLE);
	            //
	            // Now delete the account.
	            //
	            getStorageClient(ctx).delete(ctx, csid);
	            transactionContext.commitTransaction();
            } catch (Throwable t) {
            	transactionContext.markForRollback();
            	throw t;
            } finally {
            	ctx.closeConnection();
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        }

        return Response.status(HttpResponseCodes.SC_OK).build();
    }

	@POST
    @Path("{csid}/accountroles")
    public Response createAccountRole(
    		@Context UriInfo uriInfo,
    		@QueryParam("_method") String method,
            @PathParam("csid") String accCsid,
            AccountRole input) {
        if (method != null) {
            if ("delete".equalsIgnoreCase(method)) { // How would this ever be true?
                return deleteAccountRole(accCsid, input);
            }
        }
        logger.debug("createAccountRole with accCsid=" + accCsid);
        ensureCSID(accCsid, ServiceMessages.POST_FAILED+ "accountroles account ");

        try {
        	AccountsCommon account = (AccountsCommon)get(accCsid, AccountsCommon.class);
            // If marked as immutable, fail.
            if (AccountClient.IMMUTABLE.equals(account.getRolesProtection())) {
                Response response =
                	Response.status(Response.Status.FORBIDDEN).entity("Roles for Account: "+accCsid+" are immutable.").type("text/plain").build();
                return response;
            }

            ServiceContext<AccountsCommon, AccountsCommon> ctx = createServiceContext((AccountsCommon) null, AccountsCommon.class, uriInfo);
            ctx.openConnection();
            try {
	            AccountRoleSubResource subResource =
	                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
	            String accrolecsid = subResource.createAccountRole(ctx, input, SubjectType.ROLE);
	            UriBuilder path = UriBuilder.fromResource(AccountResource.class);
	            path.path(accCsid + "/accountroles/" + accrolecsid);
	            Response response = Response.created(path.build()).build();
	            return response;
            } finally {
            	ctx.closeConnection();
            }
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
        ServiceContext<AccountsCommon, AccountsCommon> ctx = null;

        try {
            AccountRoleSubResource subResource =
                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
            //get relationships for an account
            result = subResource.getAccountRoleRel(ctx,	accCsid, SubjectType.ROLE, accrolecsid);
         } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, accCsid);
        }
        checkResult(result, accCsid, ServiceMessages.GET_FAILED);

        return result;
    }

    @GET
    @Path("{csid}/accountroles")
    public AccountRole getAccountRole(@PathParam("csid") String accCsid) {
        logger.debug("getAccountRole with accCsid=" + accCsid);

        ensureCSID(accCsid, ServiceMessages.GET_FAILED+ "accountroles account ");
        AccountRole result = null;

        try {
            result = JpaStorageUtils.getAccountRoles(accCsid);
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
            if (AccountClient.IMMUTABLE.equals(account.getRolesProtection())) {
                Response response =
                	Response.status(Response.Status.FORBIDDEN).entity("Roles for Account: "+accCsid+" are immutable.").type("text/plain").build();
                return response;
            }

            ServiceContext<AccountsCommon, AccountsCommon> ctx = createServiceContext((AccountsCommon) null,
                    AccountsCommon.class, (UriInfo) null);
            ctx.openConnection();
            try {
	            AccountRoleSubResource subResource =
	                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
	            //delete all relationships for an account
	            subResource.deleteAccountRole(ctx, accCsid, SubjectType.ROLE, input);
            } finally {
            	ctx.closeConnection();
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, accCsid);
        }

        return Response.status(HttpResponseCodes.SC_OK).build();
    }

    @DELETE
    @Path("{csid}/accountroles")
    public Response deleteAccountRole(@Context UriInfo uriInfo, @PathParam("csid") String accCsid) {

        logger.debug("deleteAccountRole: All roles related to account with accCsid=" + accCsid);
        ensureCSID(accCsid, ServiceMessages.DELETE_FAILED+ "accountroles account ");

        try {
            // If marked as roles immutable, do not delete
        	AccountsCommon account = (AccountsCommon)get(accCsid, AccountsCommon.class);
            if (AccountClient.IMMUTABLE.equals(account.getRolesProtection())) {
                Response response =
                	Response.status(Response.Status.FORBIDDEN).entity("Roles for Account: "+accCsid+" are immutable.").type("text/plain").build();
                return response;
            }

            ServiceContext<AccountsCommon, AccountsCommon> ctx = createServiceContext((AccountsCommon) null, AccountsCommon.class, uriInfo);
            ctx.openConnection();
            try {
	            AccountRoleSubResource subResource =
	                    new AccountRoleSubResource(AccountRoleSubResource.ACCOUNT_ACCOUNTROLE_SERVICE);
	            //delete all relationships for an account
	            subResource.deleteAccountRole(ctx, accCsid, SubjectType.ROLE);
            } finally {
            	ctx.closeConnection();
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, accCsid);
        }

        return Response.status(HttpResponseCodes.SC_OK).build();

    }
}
