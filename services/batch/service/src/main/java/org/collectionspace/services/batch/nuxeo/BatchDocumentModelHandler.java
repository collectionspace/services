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
package org.collectionspace.services.batch.nuxeo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.account.AccountResource;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoRepositoryClientImpl;
import org.collectionspace.services.batch.BatchCommon;
import org.collectionspace.services.batch.BatchCommon.ForDocTypes;
import org.collectionspace.services.batch.BatchCommon.ForRoles;
import org.collectionspace.services.batch.BatchInvocable;
import org.collectionspace.services.batch.ResourceActionGroup;
import org.collectionspace.services.batch.ResourceActionGroupList;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorization_mgt.ActionGroup;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.invocable.Invocable.InvocationError;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchDocumentModelHandler extends NuxeoDocumentModelHandler<BatchCommon> {
	private final Logger logger = LoggerFactory.getLogger(BatchDocumentModelHandler.class);

	protected final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();
	
	/**
	 * Return true if the batch job supports the requested mode.
	 * @param invocationCtx
	 * @param batchCommon
	 * @return
	 * @throws BadRequestException 
	 */
	protected boolean supportsInvokationMode(InvocationContext invocationCtx, BatchCommon batchCommon) throws BadRequestException {
		boolean result = false;
		
		String invocationMode = invocationCtx.getMode().toLowerCase();
		if (BatchInvocable.INVOCATION_MODE_SINGLE.equalsIgnoreCase(invocationMode)) {
			result = batchCommon.isSupportsSingleDoc(); //BatchJAXBSchema.SUPPORTS_SINGLE_DOC;
		} else if (BatchInvocable.INVOCATION_MODE_LIST.equalsIgnoreCase(invocationMode)) {
			result = batchCommon.isSupportsDocList(); //BatchJAXBSchema.SUPPORTS_DOC_LIST;
		} else if (BatchInvocable.INVOCATION_MODE_GROUP.equalsIgnoreCase(invocationMode)) {
			result = batchCommon.isSupportsGroup(); //BatchJAXBSchema.SUPPORTS_GROUP;
		} else if (Invocable.INVOCATION_MODE_NO_CONTEXT.equalsIgnoreCase(invocationMode)) {
			result = batchCommon.isSupportsNoContext(); //InvocableJAXBSchema.SUPPORTS_NO_CONTEXT;
		} else {
			String msg = String.format("BatchResource: Unknown invocation mode '%s' requested trying to invoke batch job '%s'.",
					invocationMode, batchCommon.getName());
			throw new BadRequestException(msg);
		}
		
		return result;
	}
	
	/**
	 * Returns true if we found any required permissions.
	 * 
	 * @param batchCommon
	 * @return
	 */
	private boolean hasRequiredPermissions(BatchCommon batchCommon) {
		boolean result = false;
		
		try {
			result = batchCommon.getResourceActionGroupList().getResourceActionGroup().size() > 0;
		} catch (NullPointerException e) {
			// ignore exception, we're just testing to see if we have any list elements
		}
		
		return result;
	}
	
	/**
	 * Returns true if we found any required roles.
	 * 
	 * @param batchCommon
	 * @return
	 */
	private boolean hasRequiredRoles(BatchCommon batchCommon) {
		boolean result = false;
		
		try {
			result = batchCommon.getForRoles().getRoleDisplayName().size() > 0;
		} catch (NullPointerException e) {
			// ignore exception, we're just testing to see if we have any list elements
		}
		
		return result;
	}

	/**
	 * The current user is authorized to run the batch job if:
	 * 	1. No permissions or roles are specified in the batch job
	 *  2. No roles are specified, but permissions are specified and the current user has those permissions
	 *  3. Roles are specified and the current user is a member of at least one of the roles.
	 * 
	 * @param batchCommon
	 * @return
	 */
	protected boolean isAuthoritzed(BatchCommon batchCommon) {
		boolean result = true;
		
		if (hasRequiredRoles(batchCommon)) { 
			result = isAuthorizedWithRoles(batchCommon);
		} else if (hasRequiredPermissions(batchCommon)) {
			result = isAuthoritzedWithPermissions(batchCommon);
		}
		 		
		return result;
	}
	
	protected boolean isAuthorizedWithRoles(BatchCommon batchCommon) {
		boolean result = false;
		
		ForRoles forRolesList = batchCommon.getForRoles();
		if (forRolesList != null) {
			AccountResource accountResource = new AccountResource();
			List<String> roleDisplayNameList = accountResource.getAccountRoles(AuthN.get().getUserId(), AuthN.get().getCurrentTenantId());
			for (String target : forRolesList.getRoleDisplayName()) {
				if (Tools.listContainsIgnoreCase(roleDisplayNameList, target)) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Check to see if the current user is authorized to run/invoke this batch job.  If the batch job
	 * did not specify any permissions, we assume that the current user is authorized to run the job.
	 * @param batchCommon
	 * @return
	 */
	protected boolean isAuthoritzedWithPermissions(BatchCommon batchCommon) {
		boolean result = true;
		
		ResourceActionGroupList resourceActionGroupList = batchCommon.getResourceActionGroupList();
		if (resourceActionGroupList != null) {
			String tenantId = AuthN.get().getCurrentTenantId();
			for (ResourceActionGroup resourceActionGroup: resourceActionGroupList.getResourceActionGroup()) {
				String resourceName = resourceActionGroup.getResourceName();
				ActionGroup actionGroup = ActionGroup.creatActionGroup(resourceActionGroup.getActionGroup());
				for (ActionType actionType: actionGroup.getActions()) {
					CSpaceResource res = new URIResourceImpl(tenantId, resourceName, AuthZ.getMethod(actionType));
					if (AuthZ.get().isAccessAllowed(res) == false) {
						return false;
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a copy of the incoming list of strings all lower-cased.  Also removes any duplicates.
	 * 
	 * @param listOfStrings
	 * @return
	 */
	private List<String> toLowerCase(List<String> listOfStrings) {
		List<String> result = null;
		
		if (listOfStrings != null) {
			Set<String> stringSet = new HashSet<String>();
			for (String s : listOfStrings) {
				stringSet.add(s.toLowerCase());
			}
			result = new ArrayList<String>(stringSet);
		}
		
		return result;
	}

	public InvocationResults invokeBatchJob(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid,
			ResourceMap resourceMap, InvocationContext invocationCtx, BatchCommon batchCommon) throws Exception {
		CoreSessionInterface repoSession = null;
		boolean releaseRepoSession = false;

		NuxeoRepositoryClientImpl repoClient = (NuxeoRepositoryClientImpl) this.getRepositoryClient(ctx);
		if (repoSession == null) {
			repoSession = repoClient.getRepositorySession(ctx);
			releaseRepoSession = true;
		}

		// Get properties from the batch docModel, and release the session
		try {
			//
			// Ensure the current user has permission to run this batch job
			if (isAuthoritzed(batchCommon) == false) {
				String msg = String.format("BatchResource: The user '%s' does not have permission to run the batch job '%s' CSID='%s'", 
						AuthN.get().getUserId(), batchCommon.getName(), csid);
				throw new PermissionException(msg);
			}
			
			//
			// Ensure the batch job supports the requested invocation context's mode type
			if (supportsInvokationMode(invocationCtx, batchCommon) == false) {
				String msg = String.format("BatchResource: The batch job '%s' CSID='%s' does not support the invocation mode '%s'.", 
						batchCommon.getName(), csid, invocationCtx.getMode());
				throw new BadRequestException(msg);
			}
			
			//
			// Ensure the batch job supports the requested invocation context's document type
			if (!Invocable.INVOCATION_MODE_NO_CONTEXT.equalsIgnoreCase(invocationCtx.getMode())) {
				ForDocTypes forDocTypes = batchCommon.getForDocTypes();
				if (forDocTypes != null) {
					List<String> forDocTypeList = toLowerCase(forDocTypes.getForDocType()); // convert all strings to lowercase.
					if (forDocTypeList == null || !forDocTypeList.contains(invocationCtx.getDocType().toLowerCase())) {
						String msg = String.format("BatchResource: The batch job '%s' CSID='%s' does not support the invocation document type '%s'.", 
								batchCommon.getName(), csid, invocationCtx.getDocType());
						throw new BadRequestException(msg);
					}
				}
			}

			//
			// Now that we've ensure all the prerequisites have been met, let's try to
			// instantiate and run the batch job.
			//
			
			String className = batchCommon.getClassName().trim();
			ClassLoader tccl = Thread.currentThread().getContextClassLoader();
			Class<?> c = tccl.loadClass(className);
			tccl.setClassAssertionStatus(className, true);
			if (!BatchInvocable.class.isAssignableFrom(c)) {
				throw new RuntimeException("BatchResource: Class: " + className + " does not implement BatchInvocable!");
			}
	
			BatchInvocable batchInstance = (BatchInvocable) c.newInstance();
			List<String> modes = batchInstance.getSupportedInvocationModes();
			if (!modes.contains(invocationCtx.getMode().toLowerCase())) {
				String msg = String.format("BatchResource: Invoked with unsupported mode '%s'.  Batch class '%s' supports these modes: %s.",
						invocationCtx.getMode().toLowerCase(), className, modes.toString());
				throw new BadRequestException(msg);
			}
	
			batchInstance.setInvocationContext(invocationCtx);
			batchInstance.setServiceContext(ctx);
			
			if (resourceMap != null) {
				batchInstance.setResourceMap(resourceMap);
			} else {
				resourceMap = ResteasyProviderFactory.getContextData(ResourceMap.class);
				if (resourceMap != null) {
					batchInstance.setResourceMap(resourceMap);
				} else {
					logger.warn("BatchResource.invoke did not get a resourceMapHolder in context!");
				}
			}
	
			batchInstance.run(batchCommon);
			int status = batchInstance.getCompletionStatus();
			if (status == Invocable.STATUS_ERROR) {
				InvocationError error = batchInstance.getErrorInfo();
				if (error.getResponseCode() == BAD_REQUEST_STATUS) {
					throw new BadRequestException("BatchResouce: batchProcess encountered error: "
							+ batchInstance.getErrorInfo());
				} else {
					throw new RuntimeException("BatchResouce: batchProcess encountered error: "
							+ batchInstance.getErrorInfo());
	
				}
			}
	
			InvocationResults results = batchInstance.getResults();
			return results;
		} catch (PermissionException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("BatchResource: Caught exception ", e);
			}
			throw e;
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("BatchResource: Caught exception ", e);
			}
			throw new DocumentException(e);
		} finally {
			if (releaseRepoSession && repoSession != null) {
				repoClient.releaseRepositorySession(ctx, repoSession);
			}
		}
	}
}
