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

import java.util.HashMap;
import java.util.List;

import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.collectionspace.services.common.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RESTeasy interceptor for access control
 * @version $Revision: 1 $
 */
@SecurityPrecedence
@ServerInterceptor
@Provider
public class SecurityInterceptor implements PreProcessInterceptor {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);
	private static final String ACCOUNT_PERMISSIONS = "accounts/*/accountperms";

	/* (non-Javadoc)
	 * @see org.jboss.resteasy.spi.interception.PreProcessInterceptor#preProcess(org.jboss.resteasy.spi.HttpRequest, org.jboss.resteasy.core.ResourceMethod)
	 */
	@Override
	public ServerResponse preProcess(HttpRequest request, ResourceMethod method)
	throws Failure, WebApplicationException {
		String httpMethod = request.getHttpMethod();
		String uriPath = request.getUri().getPath();
		if (logger.isDebugEnabled()) {
			logger.debug("received " + httpMethod + " on " + uriPath);
		}
		String resName = getResourceName(request.getUri());
		String resEntity = SecurityUtils.getResourceEntity(resName);
		
		//
		// If the resource entity is acting as a proxy then all sub-resource will map to the resource itself.
		// This essentially means that the sub-resource inherit all the authz permissions of the entity.
		//
		if (SecurityUtils.isEntityProxy() == true && !resName.equalsIgnoreCase(ACCOUNT_PERMISSIONS)) {
			resName = resEntity;
		}
		
		checkActive();
		
		//
		// All active users are allowed to the their current list of permissions.  If this is not
		// the request, then we'll do a full AuthZ check.
		//
		if (resName.equalsIgnoreCase(ACCOUNT_PERMISSIONS) != true) {
			AuthZ authZ = AuthZ.get();
			CSpaceResource res = new URIResourceImpl(resName, httpMethod);
			if (!authZ.isAccessAllowed(res)) {
				logger.error("Access to " + res.getId() + " is NOT allowed to "
						+ " user=" + AuthN.get().getUserId());
				Response response = Response.status(
						Response.Status.FORBIDDEN).entity(uriPath + " " + httpMethod).type("text/plain").build();
				throw new WebApplicationException(response);
			} else {
				//
				// They passed the first round of security checks, so now let's check to see if they're trying
				// to perform a workflow state change and make sure they are allowed to to this.
				//
				if (uriPath.endsWith(WorkflowClient.SERVICE_PATH_COMPONENT) == true) {
					String workflowSubResName = getResourceName(request.getUri());
					res = new URIResourceImpl(workflowSubResName, httpMethod);
					if (!authZ.isAccessAllowed(res)) {
						logger.error("Access to " + resName + ":" + res.getId() + " is NOT allowed to "
								+ " user=" + AuthN.get().getUserId());
						Response response = Response.status(
								Response.Status.FORBIDDEN).entity(uriPath + " " + httpMethod).type("text/plain").build();
						throw new WebApplicationException(response);
					}
				}
			}
			//
			// We've passed all the checks.  Now just log the results
			//
			if (logger.isDebugEnabled()) {
				logger.debug("Access to " + res.getId() + " is allowed to "
						+ " user=" + AuthN.get().getUserId() +
						" for tenant id=" + AuthN.get().getCurrentTenantName());
			}
		}
		
		return null;
	}

	/**
	 * checkActive check if account is active
	 * @throws WebApplicationException
	 */
	private void checkActive() throws WebApplicationException {
		String userId = AuthN.get().getUserId();
		String tenantId = AuthN.get().getCurrentTenantId();
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
				throw new WebApplicationException(response);
			}
			Object status = JaxbUtils.getValue(account, "getStatus");
			if (status != null) {
				String value = (String) JaxbUtils.getValue(status, "value");
				if ("INACTIVE".equalsIgnoreCase(value)) {
					String msg = "User's account is inactive, userId=" + userId;
					Response response = Response.status(
							Response.Status.FORBIDDEN).entity(msg).type("text/plain").build();
					throw new WebApplicationException(response);
				}
			}

		} catch (Exception e) {
			String msg = "User's account is in invalid state, userId=" + userId;
			Response response = Response.status(
					Response.Status.FORBIDDEN).entity(msg).type("text/plain").build();
			throw new WebApplicationException(response);
		}
	}

	/**
	 * Gets the resource name.
	 *
	 * @param uriInfo the uri info
	 * @return the resource name
	 */
	private String getResourceName(UriInfo uriInfo) {
		String uriPath = uriInfo.getPath();

		MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
		
		for (String pathParamName : pathParams.keySet()) {
			//assumption : path params for csid for any entity has substring csid in name
			String pathParamValue = pathParams.get(pathParamName).get(0);
			if ((pathParamName.toLowerCase().indexOf("csid") > -1)) {
				//replace csids with wildcard
				uriPath = uriPath.replace(pathParamValue, "*");
			}
			if ((pathParamName.toLowerCase().indexOf("predicate") > -1)) {
				//replace csids with wildcard
				uriPath = uriPath.replace(pathParamValue, "*");
			}
			if (pathParamName.toLowerCase().indexOf("specifier") > -1) {
				//replace name and specifiers with wildcard
				uriPath = uriPath.replace("urn:cspace:name(" + pathParamValue
						+ ")", "*");
			}
			if ((pathParamName.toLowerCase().indexOf("ms") > -1)) {
				//replace csids with wildcard
				uriPath = uriPath.replace(pathParamValue, "*");
			}
		}
		
		// FIXME: REM
		// Since the hjid (HyperJaxb3 generated IDs are not unique strings in URIs that also have a CSID,
		// we need to replace hjid last.  We can fix this by having HyperJaxb3 generate UUID.
		// Assumption : path param name for csid is lowercase
		//
		List<String> hjidValueList = pathParams.get("id");
		if (hjidValueList != null) {
			String hjidValue = hjidValueList.get(0); //should be just one value, so get the first.
			uriPath = uriPath.replace(hjidValue, "*");
		}
		
		uriPath = uriPath.replace("//", "/");
		return uriPath;
	}
}
