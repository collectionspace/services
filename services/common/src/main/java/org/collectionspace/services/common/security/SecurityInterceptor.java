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

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashMap;
import java.util.Set;




//import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
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
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.systeminfo.SystemInfoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RESTeasy interceptor for access control
 * @version $Revision: 1 $
 */
@SecurityPrecedence
@ServerInterceptor
@Provider
public class SecurityInterceptor implements PreProcessInterceptor, PostProcessInterceptor {
	
	static {
		System.err.println("Static initialization of: " + SecurityInterceptor.class.getCanonicalName());
	}

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);
	
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
	    
    private boolean isAnonymousRequest(HttpRequest request, ResourceMethodInvoker resourceMethodInvoker) { // see C:\dev\src\cspace\services\services\JaxRsServiceProvider\src\main\webapp\WEB-INF\applicationContext-security.xml
    	boolean result = false;
    	
		String resName = SecurityUtils.getResourceName(request.getUri()).toLowerCase();
		switch (resName) {
			case AuthZ.PASSWORD_RESET:
			case AuthZ.PROCESS_PASSWORD_RESET:
			case SYSTEM_INFO:
				return true;
		}
		    	
		Class<?> resourceClass = resourceMethodInvoker.getResourceClass();
		try {
			CollectionSpaceResource resourceInstance = (CollectionSpaceResource)resourceClass.newInstance();
			result = resourceInstance.allowAnonymousAccess(request, resourceClass);
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
    	// ACCOUNT_PERMISSIONS: All active users are allowed to see the *their* (we enforce this) current list of permissions.  If this is not
		// the request, then we'll do a full AuthZ check.
    	//
    	// STRUCTURED_DATE_REQUEST: All user can request the parsing of a structured date string.
    	//
    	switch (resName) {
			case AuthZ.STRUCTURED_DATE_REQUEST:
			case AuthZ.ACCOUNT_PERMISSIONS:
			case AuthZ.REPORTS_MIME_OUTPUTS:
    			result = false;
    			break;
    		default:
    			result = true;
    	}
    	
    	return result;
    }
    
	/* (non-Javadoc)
	 * @see org.jboss.resteasy.spi.interception.PreProcessInterceptor#preProcess(org.jboss.resteasy.spi.HttpRequest, org.jboss.resteasy.core.ResourceMethod)
	 */
	@Override
	public ServerResponse preProcess(HttpRequest request, ResourceMethodInvoker resourceMethodInvoker)
			throws Failure, CSWebApplicationException {
		ServerResponse result = null; // A null value essentially means success for this method
		Method resourceMethod = resourceMethodInvoker.getMethod();
		
		try {
			if (isAnonymousRequest(request, resourceMethodInvoker) == true) {
				// We don't need to check credentials for anonymous requests.  Just login to Nuxeo and
				// exit
				nuxeoPreProcess(request, resourceMethodInvoker); // We login to Nuxeo only after we've checked authorization
	
				return result;
			}
			
			final String servicesResource = "/cspace-services/"; // HACK - this is configured in war, get this from tomcat instead
			final int servicesResourceLen = servicesResource.length();
			String httpMethod = request.getHttpMethod();
			String uriPath = request.getUri().getPath();
			
			if (logger.isDebugEnabled()) {
				String fullRequest = request.getUri().getRequestUri().toString();
				int servicesResourceIdx = fullRequest.indexOf(servicesResource);
				String relativeRequest = (servicesResourceIdx<=0)? fullRequest
													: fullRequest.substring(servicesResourceIdx+servicesResourceLen);
				logger.debug("received " + httpMethod + " on " + relativeRequest);
			}
			
			String resName = SecurityUtils.getResourceName(request.getUri());
			String resEntity = SecurityUtils.getResourceEntity(resName);
			
			//
			// If the resource entity is acting as a proxy then all sub-resources will map to the resource itself.
			// This essentially means sub-resources inherit all the authz permissions of the entity.
			//
			if (SecurityUtils.isResourceProxied(resName) == true) {
				resName = resEntity;
			} else {
				//
				// If our resName is not proxied, we may need to tweak it.
				//
				switch (resName) {
					case AuthZ.REPORTS_INVOKE:
					case AuthZ.BATCH_INVOKE: {
						resName = resName.replace("/*/", "/");
					}
				}
			}
			//
			// Make sure the account of the user making the request is current and active
			//
			checkActive();
			
			if (requiresAuthorization(resName) == true) { //see comment immediately above
				AuthZ authZ = AuthZ.get();
				CSpaceResource res = new URIResourceImpl(AuthN.get().getCurrentTenantId(), resName, httpMethod);
				if (authZ.isAccessAllowed(res) == false) {
						logger.error("Access to " + res.getId() + " is NOT allowed to "
								+ " user=" + AuthN.get().getUserId());
						Response response = Response.status(
								Response.Status.FORBIDDEN).entity(uriPath + " " + httpMethod).type("text/plain").build();
						throw new CSWebApplicationException(response);
				} else {
					//
					// They passed the first round of security checks, so now let's check to see if they're trying
					// to perform a workflow state change or fulltext reindex and make sure they are allowed to to this.
					//
					if (uriPath.contains(WorkflowClient.SERVICE_PATH) == true) {
						String workflowProxyResource = SecurityUtils.getWorkflowResourceName(request);
						res = new URIResourceImpl(AuthN.get().getCurrentTenantId(), workflowProxyResource, httpMethod);
						if (authZ.isAccessAllowed(res) == false) {
							logger.error("Access to " + resName + ":" + res.getId() + " is NOT allowed to "
									+ " user=" + AuthN.get().getUserId());
							Response response = Response.status(
									Response.Status.FORBIDDEN).entity(uriPath + " " + httpMethod).type("text/plain").build();
							throw new CSWebApplicationException(response);
						}
					} else 	if (uriPath.contains(IndexClient.SERVICE_PATH) == true) {
						String indexProxyResource = SecurityUtils.getIndexResourceName(request);
						res = new URIResourceImpl(AuthN.get().getCurrentTenantId(), indexProxyResource, httpMethod);
						if (authZ.isAccessAllowed(res) == false) {
							logger.error("Access to " + resName + ":" + res.getId() + " is NOT allowed to "
									+ " user=" + AuthN.get().getUserId());
							Response response = Response.status(
									Response.Status.FORBIDDEN).entity(uriPath + " " + httpMethod).type("text/plain").build();
							throw new CSWebApplicationException(response);
						}
					}

				}
				//
				// Login to Nuxeo
				//
				nuxeoPreProcess(request, resourceMethodInvoker); // We login to Nuxeo only after we've checked authorization
				
				//
				// We've passed all the checks.  Now just log the results
				//
				if (logger.isTraceEnabled()) {
					logger.trace("Access to " + res.getId() + " is allowed to " +
							" user=" + AuthN.get().getUserId() +
							" for tenant id=" + AuthN.get().getCurrentTenantName());
				}
			}
		} catch (Throwable t) {
			if (logger.isTraceEnabled() == true) {
				t.printStackTrace();
			}
			throw t;
		}
		
		return result;
	}
	
	@Override
	public void postProcess(ServerResponse arg0) {
		//
		// Log out of the Nuxeo framework
		//
		nuxeoPostProcess(arg0);
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
			if (tenantDisabled == true) {
				String errMsg = String.format("The user %s's tenant '%s' is disabled.  Contact your CollectionSpace administrator.",
						userId, tenantBindingType.getDisplayName());
				Response response = Response.status(
						Response.Status.CONFLICT).entity(errMsg).type("text/plain").build();
				throw new CSWebApplicationException(response);				
			}
		} catch (IllegalStateException ise) {
			String errMsg = "User's account is not associated to any active tenants, userId=" + userId;
			// Note the RFC on return types:
			// If the request already included Authorization credentials, then the 401 response 
			// indicates that authorization has been refused for those credentials.
			Response response = Response.status(
					Response.Status.UNAUTHORIZED).entity(errMsg).type("text/plain").build();
			throw new CSWebApplicationException(ise, response);
		}
		
		try {
			//can't use JAXB here as this runs from the common jar which cannot
			//depend upon the account service
			String whereClause = "where userId = :userId";
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("userId", userId);

			Object account = JpaStorageUtils.getEntity(
					"org.collectionspace.services.account.AccountsCommon", whereClause, params);
			if (account == null) {
				String msg = "User's account not found, userId=" + userId;
				Response response = Response.status(
						Response.Status.FORBIDDEN).entity(msg).type("text/plain").build();
				throw new CSWebApplicationException(response);
			}
			Object status = JaxbUtils.getValue(account, "getStatus");
			if (status != null) {
				String value = (String) JaxbUtils.getValue(status, "value");
				if ("INACTIVE".equalsIgnoreCase(value)) {
					String msg = "User's account is inactive, userId=" + userId;
					Response response = Response.status(
							Response.Status.FORBIDDEN).entity(msg).type("text/plain").build();
					throw new CSWebApplicationException(response);
				}
			}

		} catch (Exception e) {
			String msg = "User's account is in invalid state, userId=" + userId;
			Response response = Response.status(
					Response.Status.FORBIDDEN).entity(msg).type("text/plain").build();
			throw new CSWebApplicationException(e, response);
		}
	}
	//
	// Nuxeo login support
	//
	public ServerResponse nuxeoPreProcess(HttpRequest request, ResourceMethodInvoker resourceMethodInvoker)
			throws Failure, CSWebApplicationException {
		try {
			nuxeoLogin(NUXEO_ADMIN);
		} catch (LoginException e) {
			String msg = "Unable to login to the Nuxeo framework";
			logger.error(msg, e);
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(msg).type("text/plain").build();
			throw new CSWebApplicationException(e, response);
		}
		
		return null;
	}
	
	public void nuxeoPostProcess(ServerResponse arg0) {
		try {
			nuxeoLogout();
		} catch (LoginException e) {
			String msg = "Unable to logout of the Nuxeo framework.";
			logger.error(msg, e);
		}
	}	
    
    private void logLoginContext(LoginContext loginContext) {
    	if (!logger.isTraceEnabled()) return;

		logger.trace("CollectionSpace services now logged in to Nuxeo with LoginContext: "
				+ loginContext);
		Subject subject = loginContext.getSubject();
		Set<Principal> principals = subject.getPrincipals();
		logger.trace("Nuxeo login performed with principals: ");
		for (Principal principal : principals) {
			logger.trace("[" + principal.getName() + "]");
		}
    }
    
    private void logLogoutContext(LoginContext loginContext) {
    	if (!logger.isTraceEnabled()) return;
    	
    	if (loginContext != null) {
			logger.trace("CollectionSpace services now logging out of Nuxeo with LoginContext: "
					+ loginContext);
			Subject subject = loginContext.getSubject();
			Set<Principal> principals = subject.getPrincipals();
			logger.trace("Nuxeo logout performed with principals: ");
			for (Principal principal : principals) {
				logger.trace("[" + principal.getName() + "]");
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
    		threadLocalLoginContext = new ThreadLocal<LoginContext>();
    		if (logger.isTraceEnabled() == true) {
    			logger.trace(String.format("Thread ID %s: Created new ThreadLocal instance: %s)",
    					Thread.currentThread(), threadLocalLoginContext));
    		}
    	}
    	
    	LoginContext loginContext = threadLocalLoginContext.get();
    	
    	if (loginContext == null) {
    		loginContext = Framework.loginAs(user);
    		frameworkLogins++;
    		threadLocalLoginContext.set(loginContext);
    		if (logger.isTraceEnabled() == true) {
	        	logger.trace(String.format("Thread ID %s: Logged in with ThreadLocal instance %s - %s ",
	        			Thread.currentThread(), threadLocalLoginContext, threadLocalLoginContext.get()));
    		}
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
    		logger.warn(String.format(String.format("Thread ID %s: Alreadyed logged in with ThreadLocal instance %s - %s ",
	        			Thread.currentThread(), threadLocalLoginContext, threadLocalLoginContext.get())));
    		frameworkLogins++;
    	}
    }
    
    private synchronized void nuxeoLogout() throws LoginException {
    	LoginContext loginContext = threadLocalLoginContext != null ? threadLocalLoginContext.get() : null; 
        if (loginContext != null) {
   			logLogoutContext(loginContext);
            loginContext.logout();
            threadLocalLoginContext.set(null); // We need to clear the login context from this thread, so the next request on this thread has to login again.
            logLogoutContext(null);
            frameworkLogins--;
            if (logger.isTraceEnabled()) {
            	String.format("Framework logins: ", frameworkLogins);
            }
        } else {
        	if (frameworkLogins > 0) {
        		logger.warn(ERROR_NUXEO_LOGOUT);  // If we get here, it means our login/logout bookkeeping has failed.
        	}
        }
        
        if (frameworkLogins == 0) {
        	if (threadLocalLoginContext != null) {
	        	logger.trace(String.format("Thread ID %s: Clearing ThreadLocal instance %s - %s ",
	        			Thread.currentThread(), threadLocalLoginContext, threadLocalLoginContext.get()));
        	}
        	threadLocalLoginContext = null; //Clear the ThreadLocal to void Tomcat warnings associated with thread pools.
        }
    }	
}
