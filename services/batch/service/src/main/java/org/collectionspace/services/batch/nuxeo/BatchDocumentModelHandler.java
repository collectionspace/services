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

import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.collectionspace.services.BatchJAXBSchema;
import org.collectionspace.services.jaxb.InvocableJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
import org.collectionspace.services.batch.BatchCommon;
import org.collectionspace.services.batch.BatchInvocable;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.invocable.Invocable.InvocationError;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchDocumentModelHandler 
	extends DocHandlerBase<BatchCommon> {
    private final Logger logger = LoggerFactory.getLogger(BatchDocumentModelHandler.class);

	protected final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();
	
	public InvocationResults invokeBatchJob(
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			String csid,
			ResourceMap resourceMap, 
			InvocationContext invContext) throws Exception {

		RepositoryInstance repoSession = null;
		boolean releaseRepoSession = false;

		String invocationMode = invContext.getMode();
		String modeProperty = null;
		boolean checkDocType = true;
		if(BatchInvocable.INVOCATION_MODE_SINGLE.equalsIgnoreCase(invocationMode)) {
			modeProperty = BatchJAXBSchema.SUPPORTS_SINGLE_DOC;
		} else if(BatchInvocable.INVOCATION_MODE_LIST.equalsIgnoreCase(invocationMode)) {
			modeProperty = BatchJAXBSchema.SUPPORTS_DOC_LIST;
		} else if(BatchInvocable.INVOCATION_MODE_GROUP.equalsIgnoreCase(invocationMode)) {
			modeProperty = BatchJAXBSchema.SUPPORTS_GROUP;
		} else if(Invocable.INVOCATION_MODE_NO_CONTEXT.equalsIgnoreCase(invocationMode)) {
			modeProperty = InvocableJAXBSchema.SUPPORTS_NO_CONTEXT;
			checkDocType = false;
		} else {
			throw new BadRequestException("BatchResource: unknown Invocation Mode: "
					+invocationMode);
		}

		RepositoryJavaClientImpl repoClient = (RepositoryJavaClientImpl)this.getRepositoryClient(ctx);
		repoSession = this.getRepositorySession();
		if (repoSession == null) {
			repoSession = repoClient.getRepositorySession(ctx);
			releaseRepoSession = true;
		}

		String className = null;
		// Get properties from the batch docModel, and release the session
		try {
			DocumentWrapper<DocumentModel> wrapper = repoClient.getDoc(repoSession, ctx, csid);
			DocumentModel docModel = wrapper.getWrappedObject();
			Boolean supports = (Boolean)docModel.getPropertyValue(modeProperty);
			if(!supports) {
				throw new BadRequestException("BatchResource: This Batch Job does not support Invocation Mode: "
						+invocationMode);
			}
			if(checkDocType) {
				List<String> forDocTypeList = 
						(List<String>)docModel.getPropertyValue(BatchJAXBSchema.FOR_DOC_TYPES);
				if(forDocTypeList==null
						|| !forDocTypeList.contains(invContext.getDocType())) {
					throw new BadRequestException(
							"BatchResource: Invoked with unsupported document type: "
									+invContext.getDocType());
				}
			}
			className = (String)docModel.getPropertyValue(BatchJAXBSchema.BATCH_CLASS_NAME);
		} catch (PropertyException pe) {
			if (logger.isDebugEnabled()) {
				logger.debug("Property exception getting batch values: ", pe);
			}
			throw pe;
		} catch (DocumentException de) {
			if (logger.isDebugEnabled()) {
				logger.debug("Problem getting batch doc: ", de);
			}
			throw de;
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception ", e);
			}
			throw new DocumentException(e);
		} finally {
			if (releaseRepoSession && repoSession != null) {
				repoClient.releaseRepositorySession(ctx, repoSession);
			}
		}
		className = className.trim();
		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		Class<?> c = tccl.loadClass(className);
		// enable validation assertions
		tccl.setClassAssertionStatus(className, true);
		if(!BatchInvocable.class.isAssignableFrom(c)) {
			throw new RuntimeException("BatchResource: Class: "
					+className+" does not implement BatchInvocable!");
		}
		BatchInvocable batchInstance = (BatchInvocable)c.newInstance();
		List<String> modes = batchInstance.getSupportedInvocationModes();
		if(!modes.contains(invocationMode)) {
			throw new BadRequestException(
					"BatchResource: Invoked with unsupported context mode: "
							+invocationMode);
		}
		batchInstance.setInvocationContext(invContext);
		if(resourceMap!=null) {
			batchInstance.setResourceMap(resourceMap);
		} else {
			resourceMap = ResteasyProviderFactory.getContextData(ResourceMap.class);
			if(resourceMap!=null) {
				batchInstance.setResourceMap(resourceMap);
			} else {
				logger.warn("BatchResource.invoke did not get a resourceMapHolder in Context!");
			}
		}
		batchInstance.run();
		int status = batchInstance.getCompletionStatus();
		if(status == Invocable.STATUS_ERROR) {
			InvocationError error = batchInstance.getErrorInfo();
			if(error.getResponseCode() == BAD_REQUEST_STATUS) {
				throw new BadRequestException(
						"BatchResouce: batchProcess encountered error: "
								+batchInstance.getErrorInfo());
			} else {
				throw new RuntimeException(
						"BatchResouce: batchProcess encountered error: "
								+batchInstance.getErrorInfo());

			}
		}
		InvocationResults results = batchInstance.getResults();
		return results;
	}
}

