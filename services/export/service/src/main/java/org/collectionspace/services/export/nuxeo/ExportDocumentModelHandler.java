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
package org.collectionspace.services.export.nuxeo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.ExportJAXBSchema;
import org.collectionspace.services.account.AccountResource;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.ExportClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.JEEServerDeployment;
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorization_mgt.ActionGroup;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationContext.ListCSIDs;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.export.ExportsCommon;
import org.collectionspace.services.jaxb.InvocableJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoRepositoryClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.DocumentModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExportDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class ExportDocumentModelHandler extends NuxeoDocumentModelHandler<ExportsCommon> {
	private final Logger logger = LoggerFactory.getLogger(ExportDocumentModelHandler.class);

	public InputStream invokeExport(
			TenantBindingConfigReaderImpl tenantBindingReader,
			ResourceMap resourceMap,
			ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
			InvocationContext invocationContext,
			StringBuffer outputMimeType,
			StringBuffer outputFileName) throws Exception {

		NuxeoRepositoryClientImpl repoClient = (NuxeoRepositoryClientImpl) this.getRepositoryClient(serviceContext);
		boolean releaseRepoSession = false;
		CoreSessionInterface repoSession = this.getRepositorySession();

		if (repoSession == null) {
			repoSession = repoClient.getRepositorySession(serviceContext);
			releaseRepoSession = true;
		}

		try {
			Iterator<DocumentModel> documents = findDocuments(tenantBindingReader, resourceMap, serviceContext, repoSession, invocationContext);

		}
		finally {
			if (releaseRepoSession && repoSession != null) {
				repoClient.releaseRepositorySession(serviceContext, repoSession);
			}
		}

		return null;
		// return buildExportResult(csid, params, exportFileNameProperty, outMimeType.toString(), outExportFileName);
	}

	private Iterator<DocumentModel> findDocuments(
			TenantBindingConfigReaderImpl tenantBindingReader,
			ResourceMap resourceMap,
			ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
			CoreSessionInterface repoSession,
			InvocationContext invocationContext) throws Exception {

		String docType = invocationContext.getDocType();
		ServiceBindingType binding = tenantBindingReader.getServiceBindingForDocType(serviceContext.getTenantId(), docType);
		String serviceName = binding.getName();

		switch (invocationContext.getMode().toLowerCase()) {
			case Invocable.INVOCATION_MODE_SINGLE:
				return findDocumentByCsid(resourceMap, serviceContext, repoSession, serviceName, invocationContext.getSingleCSID());
			case Invocable.INVOCATION_MODE_LIST:
				return findDocumentsByCsid(resourceMap, serviceContext, repoSession, serviceName, invocationContext.getListCSIDs().getCsid());
			case Invocable.INVOCATION_MODE_GROUP:
				return findDocumentsByGroup(resourceMap, serviceContext, repoSession, invocationContext.getGroupCSID());
			case Invocable.INVOCATION_MODE_NO_CONTEXT:
				return findDocumentsByType(resourceMap, serviceContext, repoSession, invocationContext.getDocType());
			default:
				return null;
		}
	}

	private Iterator<DocumentModel> findDocumentByCsid(
			ResourceMap resourceMap,
			ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
			CoreSessionInterface repoSession,
			String serviceName,
			String csid) throws Exception {

		return findDocumentsByCsid(resourceMap, serviceContext, repoSession, serviceName, Arrays.asList(csid));
	}

	private Iterator<DocumentModel> findDocumentsByCsid(
			ResourceMap resourceMap,
			ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
			CoreSessionInterface repoSession,
			String serviceName,
			List<String> csids) throws Exception {

		return new DocumentsByCsidIterator(resourceMap, serviceContext, repoSession, serviceName, csids);
	}

	private Iterator<DocumentModel> findDocumentsByType(
			ResourceMap resourceMap,
			ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
			CoreSessionInterface repoSession,
			String docType) {

		return null;
	}

	private Iterator<DocumentModel> findDocumentsByGroup(
			ResourceMap resourceMap,
			ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
			CoreSessionInterface repoSession,
			String docType) {

		return null;
	}

	// private InputStream buildExportResult(String exportCSID,
	// 		HashMap<String, Object> params,
	// 		String exportFileName,
	// 		String outputMimeType,
	// 		StringBuffer outExportFileName) throws Exception {
	// 	Connection conn = null;
	// 	InputStream result = null;

	// 	return result;
	// }

	private class DocumentsByCsidIterator implements Iterator<DocumentModel> {
		private NuxeoBasedResource resource;
		private ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext;
		private CoreSessionInterface repoSession;
		private Iterator<String> csidIterator;

		DocumentsByCsidIterator(
				ResourceMap resourceMap,
				ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
				CoreSessionInterface repoSession,
				String serviceName,
				List<String> csids) throws Exception {

			NuxeoBasedResource resource = (NuxeoBasedResource) resourceMap.get(serviceName.toLowerCase());

			if (resource == null) {
				throw new Exception("Resource not found for service name " + serviceName);
			}

			this.resource = resource;
			this.serviceContext = serviceContext;
			this.repoSession = repoSession;
			this.csidIterator = csids.iterator();
		}

		@Override
		public boolean hasNext() {
			return csidIterator.hasNext();
		}

		@Override
		public DocumentModel next() {
			String csid = csidIterator.next();

			try {
				// PoxPayloadOut payload = resource.getWithParentCtx(serviceContext, csid);
				return null;
			}
			catch (Exception e) {
				logger.warn("Could not get document with csid " + csid, e);

				return null;
			}
		}
	}
}
