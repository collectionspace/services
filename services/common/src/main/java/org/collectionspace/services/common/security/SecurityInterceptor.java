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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.common.security;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.client.index.IndexClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.CollectionSpaceResource;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.login.LoginClient;
import org.collectionspace.services.logout.LogoutClient;
import org.collectionspace.services.systeminfo.SystemInfoClient;
import org.jboss.resteasy.spi.Failure;
import org.nuxeo.runtime.api.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RESTeasy interceptor for access control
 * @version $Revision: 1 $
 */
@Provider
public class SecurityInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

	static {
		System.err.println("Static initialization of: " + SecurityInterceptor.class.getCanonicalName());
	}

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);

	private static final String LOGIN = LoginClient.SERVICE_NAME;
	private static final String LOGOUT = LogoutClient.SERVICE_NAME;
	private static final String SYSTEM_INFO = SystemInfoClient.SERVICE_NAME;
	private static final String NUXEO_ADMIN = null;
    //
    // Use this thread specific member instance to hold our login context with Nuxeo
    //
    private static ThreadLocal<LoginContext> threadLocalLoginContext = null;
    private static int frameworkLogins = 0;
    //
    // Error messages
    //
    private static final String ERROR_NUXEO_LOGOUT = "Attempt to logout when Nuxeo login context was null.";
    private static final String ERROR_UNBALANCED_LOGINS = "The number of Logins vs Logouts to the Nuxeo framework was unbalanced.";

	@Context
	private ResourceInfo resourceInfo;

    private boolean isAnonymousRequest(ContainerRequestContext requestContext) {
    	boolean result = false;

		String resName = SecurityUtils.getResourceName(requestContext.getUriInfo()).toLowerCase();
        if (resName.equals(AuthZ.PASSWORD_RESET)
            || resName.equals(AuthZ.PROCESS_PASSWORD_RESET)
            || resName.equals(LOGIN)
            || resName.equals(LOGOUT)
            || resName.equals(SYSTEM_INFO)
            || resName.isEmpty()) {
            return true;
        }

		Class<?> resourceClass = resourceInfo.getResourceClass();
		try {
			CollectionSpaceResource resourceInstance = (CollectionSpaceResource)resourceClass.newInstance();
			result = resourceInstance.allowAnonymousAccess();
		} catch (InstantiationException e) {
			logger.error("isAnonymousRequest: ", e);
		} catch (IllegalAccessException e) {
			logger.error("isAnonymousRequest: ", e);
		}

    	return result;
    }

	/*
	 * Check to see if the resource required authorization to access
	 *
	 */
	private boolean requiresAuthorization(String resName) {
		boolean result = true;
		//
		// ACCOUNT_PERMISSIONS, ACCOUNT_ROLES: All active users are allowed to see the *their*
		// (we enforce this) current list of permissions and roles.  If this is not the request, then
		// we'll do a full AuthZ check.
		//
		// STRUCTURED_DATE_REQUEST: All user can request the parsing of a structured date string.
		//
        if (resName.equals(AuthZ.STRUCTURED_DATE_REQUEST)
			|| resName.equals(AuthZ.ACCOUNT_PERMISSIONS)
			|| resName.equals(AuthZ.ACCOUNT_ROLES)
			|| resName.equals(AuthZ.REPORTS_MIME_OUTPUTS)) {
            result = false;
        }

        return result;
	}

	private void checkAccessAllowed(final AuthZ authZ,
									final CSpaceResource resource,
									final String resourceName,
									final String uriPath,
									final String httpMethod) throws CSWebApplicationException {
		if (!authZ.isAccessAllowed(resource)) {
			logger.error("Access to {}:{} is NOT allowed to  user={}", resourceName, resource.getId(),
						 AuthN.get().getUserId());
			final Response response = Response.status(Response.Status.FORBIDDEN)
											  .entity(uriPath + " " + httpMethod)
											  .type(MediaType.TEXT_PLAIN_TYPE).build();
			throw new CSWebApplicationException(response);
		}
	}

	@Override
	public void filter(ContainerRequestContext containerRequestContext) throws IOException {
		Request request = containerRequestContext.getRequest();

		try {
			if (isAnonymousRequest(containerRequestContext)) {
				// We don't need to check credentials for anonymous requests.  Just login to Nuxeo and exit
				nuxeoPreProcess();
				return;
			}

			// HACK - this is configured in war, get this from tomcat instead
			final String servicesResource = "/cspace-services/";
			final int servicesResourceLen = servicesResource.length();
			String httpMethod = request.getMethod();
			String uriPath = containerRequestContext.getUriInfo().getPath();

			if (logger.isDebugEnabled()) {
				int servicesResourceIdx = uriPath.indexOf(servicesResource);
				String relativeRequest =
					(servicesResourceIdx <= 0) ? uriPath : uriPath.substring(servicesResourceIdx + servicesResourceLen);
				logger.debug("received {}s on {}", httpMethod, relativeRequest);
			}

			String resName = SecurityUtils.getResourceName(containerRequestContext.getUriInfo());
			String resEntity = SecurityUtils.getResourceEntity(resName);

			//
			// If the resource entity is acting as a proxy then all sub-resources will map to the resource itself.
			// This essentially means sub-resources inherit all the authz permissions of the entity.
			//
			if (SecurityUtils.isResourceProxied(resName)) {
				resName = resEntity;
			} else {
				//
				// If our resName is not proxied, we may need to tweak it.
				//
				if (resName.equals(AuthZ.REPORTS_INVOKE) || resName.equals(AuthZ.BATCH_INVOKE)) {
					resName = resName.replace("/*/", "/");
				}
			}
			//
			// Make sure the account of the user making the request is current and active
			//
			checkActive();

			if (requiresAuthorization(resName)) {
				AuthZ authZ = AuthZ.get();
				CSpaceResource res = new URIResourceImpl(AuthN.get().getCurrentTenantId(), resName, httpMethod);
				checkAccessAllowed(authZ, res, resName, uriPath, httpMethod);

                //
                // They passed the first round of security checks, so now let's check to see if they're trying
                // to perform a workflow state change or fulltext reindex and make sure they are allowed to this.
                //
                if (uriPath.contains(WorkflowClient.SERVICE_PATH)) {
                    String workflowProxyResource = SecurityUtils.getWorkflowResourceName(containerRequestContext);
                    res = new URIResourceImpl(AuthN.get().getCurrentTenantId(), workflowProxyResource, httpMethod);
					checkAccessAllowed(authZ, res, resName, uriPath, httpMethod);
                } else if (uriPath.contains(IndexClient.SERVICE_PATH)) {
                    String indexProxyResource = SecurityUtils.getIndexResourceName();
                    res = new URIResourceImpl(AuthN.get().getCurrentTenantId(), indexProxyResource, httpMethod);
					checkAccessAllowed(authZ, res, resName, uriPath, httpMethod);
                }

                //
				// Login to Nuxeo
				//
				nuxeoPreProcess();

				//
				// We've passed all the checks.  Now just log the results
				//
				logger.trace("Access to {} is allowed to  user={} for tenant id={}", res.getId(),
							 AuthN.get().getUserId(),
							 AuthN.get().getCurrentTenantName());
			}
		} catch (RuntimeException t) {
			logger.error("Error in SecurityInterceptor", t);
			throw t;
		}
	}

	@Override
	public void filter(ContainerRequestContext containerRequestContext,
					   ContainerResponseContext containerResponseContext) throws IOException {
		//
		// Log out of the Nuxeo framework
		//
		nuxeoPostProcess();
	}

	/**
	 * checkActive check if account is active
	 * @throws CSWebApplicationException
	 */
	private void checkActive() throws CSWebApplicationException {
		String userId = AuthN.get().getUserId();

		try {
			//
			// Need to ensure that user's tenant is not disabled
			String tenantId = AuthN.get().getCurrentTenantId();
			TenantBindingType tenantBindingType = ServiceMain.getInstance().getTenantBindingConfigReader().getTenantBinding(tenantId);
			boolean tenantDisabled = tenantBindingType.isCreateDisabled();
			if (tenantDisabled) {
				String errMsg = String.format("The user %s's tenant '%s' is disabled.  Contact your CollectionSpace administrator.",
						userId, tenantBindingType.getDisplayName());
				Response response = Response.status(Response.Status.CONFLICT)
											.entity(errMsg)
											.type(MediaType.TEXT_PLAIN_TYPE).build();
				throw new CSWebApplicationException(response);
			}
		} catch (IllegalStateException ise) {
			String errMsg = "User's account is not associated to any active tenants, userId=" + userId;
			// Note the RFC on return types:
			// If the request already included Authorization credentials, then the 401 response
			// indicates that authorization has been refused for those credentials.
			Response response = Response.status(Response.Status.UNAUTHORIZED)
										.entity(errMsg)
										.type(MediaType.TEXT_PLAIN_TYPE).build();
			throw new CSWebApplicationException(ise, response);
		}

		try {
			//can't use JAXB here as this runs from the common jar which cannot
			//depend upon the account service
			String whereClause = "where userId = :userId";
			HashMap<String, Object> params = new HashMap<>();
			params.put("userId", userId);

			Object account = JpaStorageUtils.getEntity(
					"org.collectionspace.services.account.AccountsCommon", whereClause, params);
			if (account == null) {
				String msg = "User's account not found, userId=" + userId;
				Response response = Response.status(Response.Status.FORBIDDEN)
											.entity(msg)
											.type(MediaType.TEXT_PLAIN_TYPE).build();
				throw new CSWebApplicationException(response);
			}
			Object status = JaxbUtils.getValue(account, "getStatus");
			if (status != null) {
				String value = (String) JaxbUtils.getValue(status, "value");
				if ("INACTIVE".equalsIgnoreCase(value)) {
					String msg = "User's account is inactive, userId=" + userId;
					Response response = Response.status(Response.Status.FORBIDDEN)
												.entity(msg)
												.type(MediaType.TEXT_PLAIN_TYPE).build();
					throw new CSWebApplicationException(response);
				}
			}
		}
		catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			String msg = "User's account is in invalid state, userId=" + userId;
			Response response = Response.status(Response.Status.FORBIDDEN)
										.entity(msg)
										.type(MediaType.TEXT_PLAIN_TYPE).build();
			throw new CSWebApplicationException(e, response);
		}
	}
	//
	// Nuxeo login support
	//
	public void nuxeoPreProcess() throws Failure, CSWebApplicationException {
		try {
			nuxeoLogin(NUXEO_ADMIN);
		} catch (LoginException e) {
			String msg = "Unable to login to the Nuxeo framework";
			logger.error(msg, e);
			Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
										.entity(msg)
										.type(MediaType.TEXT_PLAIN).build();
			throw new CSWebApplicationException(e, response);
		}
	}

	public void nuxeoPostProcess() {
		try {
			nuxeoLogout();
		} catch (LoginException e) {
			logger.error("Unable to logout of the Nuxeo framework.", e);
		}
	}

    private void logLoginContext(LoginContext loginContext) {
    	if (!logger.isTraceEnabled()) {
			return;
		}

        logger.trace("CollectionSpace services now logged in to Nuxeo with LoginContext: {}", loginContext);
		Subject subject = loginContext.getSubject();
		Set<Principal> principals = subject.getPrincipals();
		logger.trace("Nuxeo login performed with principals: ");
		for (Principal principal : principals) {
            logger.trace("[{}]", principal.getName());
		}
    }

    private void logLogoutContext(LoginContext loginContext) {
    	if (!logger.isTraceEnabled()) {
			return;
		}

    	if (loginContext != null) {
            logger.trace("CollectionSpace services now logging out of Nuxeo with LoginContext: {}", loginContext);
			Subject subject = loginContext.getSubject();
			Set<Principal> principals = subject.getPrincipals();
			logger.trace("Nuxeo logout performed with principals: ");
			for (Principal principal : principals) {
                logger.trace("[{}]", principal.getName());
			}
    	} else {
    		logger.trace("Logged out.");
    	}
    }


    /*
     * Login to Nuxeo and save the LoginContext instance in a thread local variable
     */
    private synchronized void nuxeoLogin(String user) throws LoginException {
    	//
    	// Use a ThreadLocal instance to keep track of the Nuxeo login context
    	//
    	if (threadLocalLoginContext == null) {
    		threadLocalLoginContext = new ThreadLocal<>();
            logger.trace("Thread ID {}: Created new ThreadLocal instance: {})", Thread.currentThread(),
                         threadLocalLoginContext);
        }

    	LoginContext loginContext = threadLocalLoginContext.get();

    	if (loginContext == null) {
    		loginContext = Framework.loginAs(user);
    		frameworkLogins++;
    		threadLocalLoginContext.set(loginContext);
            logger.trace("Thread ID {}: Logged in with ThreadLocal instance {} - {} ", Thread.currentThread(),
                         threadLocalLoginContext, threadLocalLoginContext.get());
            //
        	// Debug logging
        	//
   			logLoginContext(loginContext);
    	} else {
    		//
    		// We're already logged in somehow?  This is probably not good.  It seems to mean that the LoginContext last
    		// used on this thread is still active -which is a *potential* security vulnerability.  However, as of 4/2016, we
    		// use the Nuxeo default "system admin" context for ever request, regardless of the CollectionSpace user making
    		// the request.  In short, there's no real security vulnerability here -just bad bookkeeping of logins.
    		//
    		logger.warn("Thread ID {}: Already logged in with ThreadLocal instance {} - {} ", Thread.currentThread(),
                        threadLocalLoginContext, threadLocalLoginContext.get());
    		frameworkLogins++;
    	}
    }

    private synchronized void nuxeoLogout() throws LoginException {
    	LoginContext loginContext = threadLocalLoginContext != null ? threadLocalLoginContext.get() : null;
        if (loginContext != null) {
   			logLogoutContext(loginContext);
            loginContext.logout();
            threadLocalLoginContext.remove(); // We need to clear the login context from this thread, so the next request on this thread has to login again.
            logLogoutContext(null);
            frameworkLogins--;
            logger.trace("Framework logins: {}", frameworkLogins);
        } else {
        	if (frameworkLogins > 0) {
        		logger.warn(ERROR_NUXEO_LOGOUT);  // If we get here, it means our login/logout bookkeeping has failed.
        	}
        }

        if (frameworkLogins == 0) {
        	if (threadLocalLoginContext != null) {
	        	logger.trace("Thread ID {}: Clearing ThreadLocal instance {} - {} ", Thread.currentThread(),
                             threadLocalLoginContext, threadLocalLoginContext.get());
        	}
        	threadLocalLoginContext = null; //Clear the ThreadLocal to void Tomcat warnings associated with thread pools.
        }
    }

}
