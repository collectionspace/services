/*
 * This file contains code from Florent Guillame's nuxeo-reindex-fulltext module.
 *
 */

package org.collectionspace.services.batch.nuxeo;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.common.CollectionSpaceResource;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.StoredValuesUriTemplate;
import org.collectionspace.services.common.UriTemplateFactory;
import org.collectionspace.services.common.UriTemplateRegistryKey;
import org.collectionspace.services.common.invocable.InvocationContext.ListCSIDs;
import org.collectionspace.services.common.invocable.InvocationContext.Params.Param;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.nuxeo.ecm.core.api.AbstractSession;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.query.QueryFilter;
import org.nuxeo.ecm.core.query.sql.NXQL;
import org.nuxeo.ecm.core.storage.FulltextConfiguration;
import org.nuxeo.ecm.core.storage.sql.Model;
import org.nuxeo.ecm.core.storage.sql.Node;
import org.nuxeo.ecm.core.storage.sql.Session;
import org.nuxeo.ecm.core.storage.sql.SimpleProperty;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLFulltextExtractorWork;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLSession;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.core.work.api.WorkManager.Scheduling;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexFullTextBatchJob extends AbstractBatchJob {
	final Logger log = LoggerFactory.getLogger(ReindexFullTextBatchJob.class);

	public static final String DC_TITLE = "dc:title";
	public static final int DEFAULT_BATCH_SIZE = 1000;
	public static final int DEFAULT_START_BATCH = 0;
	public static final int DEFAULT_END_BATCH = 0;
	public static final int DEFAULT_BATCH_PAUSE = 0;
	public static final String BATCH_STOP_FILE = "stopBatch";
	public static final String DOCTYPE_STOP_FILE = "stopDocType";

	private int batchSize = DEFAULT_BATCH_SIZE;
	private int batchPause = DEFAULT_BATCH_PAUSE;
	private int startBatch = DEFAULT_START_BATCH;
	private int endBatch = DEFAULT_END_BATCH;
	private int numAffected = 0;

	private String stopFileDirectory;

	private CoreSession coreSession;
	private Session session = null;
    protected FulltextConfiguration fulltextConfiguration;

	private Map<String, NuxeoBasedResource> resourcesByDocType;

	public ReindexFullTextBatchJob() {
		setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_NO_CONTEXT, INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST));

		stopFileDirectory = System.getProperty("java.io.tmpdir") + File.separator + ReindexFullTextBatchJob.class.getName();

		log.debug("stop file directory is " + stopFileDirectory);
	}

	@Override
	public void run() {
		setCompletionStatus(STATUS_MIN_PROGRESS);

		numAffected = 0;

		boolean isTransactionActive = TransactionHelper.isTransactionActive();

		// Commit and close the transaction that was started by the standard request lifecycle.

		if (isTransactionActive) {
			TransactionHelper.commitOrRollbackTransaction();
		}

		try {
			coreSession = getRepoSession().getCoreSession();

			if (requestIsForInvocationModeSingle()) {
				String csid = getInvocationContext().getSingleCSID();

				if (csid == null) {
					throw new Exception("No singleCSID was supplied in invocation context.");
				}

				String docType = getInvocationContext().getDocType();

				if (StringUtils.isEmpty(docType)) {
					throw new Exception("No docType was supplied in invocation context.");
				}

				log.debug("Reindexing " + docType + " record with csid: " + csid);

				reindexDocument(docType, csid);
			}
			else if (requestIsForInvocationModeList()) {
				ListCSIDs list = getInvocationContext().getListCSIDs();
				List<String> csids = list.getCsid();

				if (csids == null || csids.size() == 0) {
					throw new Exception("no listCSIDs were supplied");
				}

				String docType = getInvocationContext().getDocType();

				if (StringUtils.isEmpty(docType)) {
					throw new Exception("No docType was supplied in invocation context.");
				}

				log.debug("Reindexing " + csids.size() + " " + docType + " records with csids: " + csids.get(0) + ", ...");

				if (log.isTraceEnabled()) {
					log.trace(StringUtils.join(csids, ", "));
				}

				reindexDocuments(docType, csids);
			}
			else if (requestIsForInvocationModeNoContext()) {
				Set<String> docTypes = new LinkedHashSet<String>();
				String docType;

				docType = getInvocationContext().getDocType();

				if (StringUtils.isNotEmpty(docType)) {
					docTypes.add(docType);
				}

				// Read batch size, start and end batches, pause, and additional doctypes from params.

				for (Param param : this.getParams()) {
					if (param.getKey().equals("batchSize")) {
						batchSize = Integer.parseInt(param.getValue());
					}
					else if (param.getKey().equals("startBatch")) {
						startBatch = Integer.parseInt(param.getValue());
					}
					else if (param.getKey().equals("endBatch")) {
						endBatch = Integer.parseInt(param.getValue());
					}
					else if (param.getKey().equals("batchPause")) {
						batchPause = Integer.parseInt(param.getValue());
					}
					else if (param.getKey().equals("docType")) {
						docType = param.getValue();

						if (StringUtils.isNotEmpty(docType)) {
							docTypes.add(docType);
						}
					}
				}

				initResourceMap();
				reindexDocuments(docTypes);
			}

			log.debug("reindexing complete");

			InvocationResults results = new InvocationResults();
			results.setNumAffected(numAffected);
			results.setUserNote("reindexed " + numAffected + " records");

			setResults(results);
			setCompletionStatus(STATUS_COMPLETE);
		}
		catch(StoppedException e) {
			log.debug("reindexing terminated by stop file");

			InvocationResults results = new InvocationResults();
			results.setNumAffected(numAffected);
			results.setUserNote("reindexing terminated by stop file");

			setResults(results);
			setCompletionStatus(STATUS_COMPLETE);
		}
		catch(Exception e) {
			setErrorResult(e.getMessage());
		}
		finally {
			// Start a new transaction so the standard request lifecycle can complete.

			if (isTransactionActive) {
				TransactionHelper.startTransaction();
			}
		}
	}

	private void initResourceMap() {
		resourcesByDocType = new HashMap<String, NuxeoBasedResource>();

		for (CollectionSpaceResource<?, ?> resource : getResourceMap().values()) {
			Map<UriTemplateRegistryKey, StoredValuesUriTemplate> entries = resource.getUriRegistryEntries();

			for (UriTemplateRegistryKey key : entries.keySet()) {
				String docType = key.getDocType();
				String tenantId = key.getTenantId();

				if (getTenantId().equals(tenantId)) {
					if (resourcesByDocType.containsKey(docType)) {
						log.warn("multiple resources found for docType " + docType);

						NuxeoBasedResource currentResource = resourcesByDocType.get(docType);
						NuxeoBasedResource candidateResource = (NuxeoBasedResource) resource;

						// Favor the resource that isn't an AuthorityResource. This
						// is really just to deal with Contacts, which are handled
						// by ContactResource, PersonAuthorityResource, and
						// OrgAuthorityResource. We want to use ContactResource.

						if (!(candidateResource instanceof AuthorityResource) && (currentResource instanceof AuthorityResource)) {
							resourcesByDocType.put(docType, candidateResource);
						}

						log.warn("using " + resourcesByDocType.get(docType));
					}
					else {
						resourcesByDocType.put(docType, (NuxeoBasedResource) resource);
					}
				}
			}
		}
	}

	private void reindexDocuments(Set<String> docTypes) throws Exception {
		if (docTypes == null) {
			docTypes = new LinkedHashSet<String>();
		}

		// If no types are specified, do them all.

		if (docTypes.size() == 0) {
			docTypes.addAll(getAllDocTypes());
		}

		for (String docType : docTypes) {
			reindexDocuments(docType);
		}
	}

	private List<String> getAllDocTypes() {
		List<String> docTypes = new ArrayList<String>(resourcesByDocType.keySet());
		Collections.sort(docTypes);

		log.debug("Call to getAllDocTypes() method found: " + StringUtils.join(docTypes, ", "));

		return docTypes;
	}

	private void reindexDocuments(String docType) throws Exception {
		// Check for a stop file before reindexing the docType.

		if (batchStopFileExists() || docTypeStopFileExists()) {
			throw new StoppedException();
		}

		log.debug("reindexing docType " + docType);

		NuxeoBasedResource resource = resourcesByDocType.get(docType);

		if (resource == null) {
			log.warn("No service resource found for docType " + docType);
		}

		boolean isAuthorityItem = false;

		if (resource instanceof AuthorityResource) {
			UriTemplateRegistryKey key = new UriTemplateRegistryKey(getTenantId(), docType);
			StoredValuesUriTemplate uriTemplate = resource.getUriRegistryEntries().get(key);

			log.debug("uriTemplateType=" + uriTemplate.getUriTemplateType());

			if (uriTemplate.getUriTemplateType() == UriTemplateFactory.ITEM) {
				isAuthorityItem = true;
			}
		}

		int pageSize = batchSize;

		// The supplied start and end batch numbers start with 1, but the page number starts with 0.
		int startPage = (startBatch > 0) ? startBatch - 1 : 0;
		int endPage = (endBatch > 0) ? endBatch - 1 : Integer.MAX_VALUE;

		if (isAuthorityItem) {
			List<String> vocabularyCsids = getVocabularyCsids((AuthorityResource<?, ?>) resource);

			for (String vocabularyCsid : vocabularyCsids) {
				int pageNum = startPage;
				List<String> csids = null;

				log.debug("Reindexing vocabulary of " + docType + " with csid " + vocabularyCsid);

				do {
					// Check for a stop file before reindexing the batch.

					if (batchStopFileExists()) {
						throw new StoppedException();
					}

					csids = findAllAuthorityItems((AuthorityResource<?, ?>) resource, vocabularyCsid, pageSize, pageNum, "collectionspace_core:createdAt, ecm:name");

					if (csids.size() > 0) {
						log.debug("reindexing vocabulary of " + docType +" with csid " + vocabularyCsid + ", batch " + (pageNum + 1) + ": " + csids.size() + " records starting with " + csids.get(0));

						// Pause for the configured amount of time.

						if (batchPause > 0) {
							log.trace("pausing " + batchPause + " ms");

							Thread.sleep(batchPause);
						}

						reindexDocuments(docType, csids);
					}

					pageNum++;
				}
				while(csids.size() == pageSize && pageNum <= endPage);
			}
		} else {
			int pageNum = startPage;
			List<String> csids = null;

			do {
				// Check for a stop file before reindexing the batch.

				if (batchStopFileExists()) {
					throw new StoppedException();
				}

				csids = findAll(resource, pageSize, pageNum, "collectionspace_core:createdAt, ecm:name");

				if (csids.size() > 0) {
					log.debug("reindexing " + docType +" batch " + (pageNum + 1) + ": " + csids.size() + " records starting with " + csids.get(0));

					// Pause for the configured amount of time.

					if (batchPause > 0) {
						log.trace("pausing " + batchPause + " ms");

						Thread.sleep(batchPause);
					}

					reindexDocuments(docType, csids);
				}

				pageNum++;
			}
			while(csids.size() == pageSize && pageNum <= endPage);
		}
	}

	private void reindexDocument(String docType, String csid) throws Exception {
		reindexDocuments(docType, Arrays.asList(csid));
	}

	private void reindexDocuments(String docType, List<String> csids) throws Exception {
		// Convert the csids to structs of nuxeo id and type, as expected
		// by doBatch.

		if (csids == null || csids.size() == 0) {
			return;
		}

		// Transaction for the batch
		boolean tx = TransactionHelper.startTransaction();

		getLowLevelSession();

		List<Info> infos = new ArrayList<Info>();

		String query = "SELECT ecm:uuid, ecm:primaryType FROM Document " +
					   "WHERE ecm:name IN (" + StringUtils.join(quoteList(csids), ',') + ") " +
					   "AND ecm:primaryType LIKE '" + docType + "%' " +
					   "AND ecm:isCheckedInVersion = 0 AND ecm:isProxy = 0";
		IterableQueryResult result = session.queryAndFetch(query, NXQL.NXQL, QueryFilter.EMPTY);

		try {
			for (Map<String, Serializable> map : result) {
				String id = (String) map.get(NXQL.ECM_UUID);
				String type = (String) map.get(NXQL.ECM_PRIMARYTYPE);
				infos.add(new Info(id, type));
			}
		} finally {
			result.close();
		}

		if (csids.size() != infos.size()) {
			log.warn("didn't find info for all the supplied csids: expected " + csids.size() + ", found " + infos.size());
		}

		if (log.isTraceEnabled()) {
			for (Info info : infos) {
				log.trace(info.type + " " + info.id);
			}
		}

		numAffected += infos.size();

		// Below code copied from the doBatch function.

		boolean ok;

		List<Serializable> ids = new ArrayList<Serializable>(infos.size());
		Set<String> asyncIds = new HashSet<String>();
		Model model = session.getModel();
		for (Info info : infos) {
			ids.add(info.id);
			if (fulltextConfiguration.isFulltextIndexable(info.type)) {
				asyncIds.add(model.idToString(info.id));
			}
		}
		ok = false;
		try {
			runSyncBatch(ids, asyncIds);
			ok = true;
		} finally {
			if (tx) {
				if (!ok) {
					TransactionHelper.setTransactionRollbackOnly();
					log.error("Rolling back sync");
				}
				TransactionHelper.commitOrRollbackTransaction();
			}
		}

		runAsyncBatch(asyncIds);

		// wait for async completion after transaction commit
		Framework.getLocalService(EventService.class).waitForAsyncCompletion();
	}

	private List<String> quoteList(List<String> values) {
		List<String> quoted = new ArrayList<String>(values.size());

		for (String value : values) {
			quoted.add("'" + value + "'");
		}

		return quoted;
	}

	private boolean batchStopFileExists() {
		return (stopFileDirectory != null && new File(stopFileDirectory + File.separator + BATCH_STOP_FILE).isFile());
	}

	private boolean docTypeStopFileExists() {
		return (stopFileDirectory != null && new File(stopFileDirectory + File.separator + DOCTYPE_STOP_FILE).isFile());
	}

	private static class StoppedException extends Exception {
		private static final long serialVersionUID = 8813189331855935939L;

		public StoppedException() {

		}
	}

	/*
	 * The code below this comment is copied from the nuxeo-reindex-fulltext
	 * module. The original copyright is below.
	 */

	/*
	 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
	 *
	 * All rights reserved. This program and the accompanying materials
	 * are made available under the terms of the GNU Lesser General Public License
	 * (LGPL) version 2.1 which accompanies this distribution, and is available at
	 * http://www.gnu.org/licenses/lgpl.html
	 *
	 * This library is distributed in the hope that it will be useful,
	 * but WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
	 * Lesser General Public License for more details.
	 *
	 * Contributors:
	 *     Florent Guillaume
	 */

	protected static class Info {
		public final Serializable id;

		public final String type;

		public Info(Serializable id, String type) {
			this.id = id;
			this.type = type;
		}
	}

	/**
	 * This has to be called once the transaction has been started.
	 */
	protected void getLowLevelSession() {
		try {
			SQLSession s = (SQLSession) ((AbstractSession) coreSession).getSession();
			Field f2 = SQLSession.class.getDeclaredField("session");
			f2.setAccessible(true);
			session = (Session) f2.get(s);
			fulltextConfiguration = session.getModel().getFulltextConfiguration();
		} catch (ReflectiveOperationException e) {
			throw new NuxeoException(e);
		}
	}

	protected void doBatch(List<Info> infos) {
		boolean tx;
		boolean ok;

		// transaction for the sync batch
		tx = TransactionHelper.startTransaction();

		getLowLevelSession(); // for fulltextInfo
		List<Serializable> ids = new ArrayList<Serializable>(infos.size());
		Set<String> asyncIds = new HashSet<String>();
		Model model = session.getModel();
		for (Info info : infos) {
			ids.add(info.id);
			if (fulltextConfiguration.isFulltextIndexable(info.type)) {
				asyncIds.add(model.idToString(info.id));
			}
		}
		ok = false;
		try {
			runSyncBatch(ids, asyncIds);
			ok = true;
		} finally {
			if (tx) {
				if (!ok) {
					TransactionHelper.setTransactionRollbackOnly();
					log.error("Rolling back sync");
				}
				TransactionHelper.commitOrRollbackTransaction();
			}
		}

		runAsyncBatch(asyncIds);

		// wait for async completion after transaction commit
		Framework.getLocalService(EventService.class).waitForAsyncCompletion();
	}

	/*
		* Do this at the low-level session level because we may have to modify
		* things like versions which aren't usually modifiable, and it's also good
		* to bypass all listeners.
		*/
	protected void runSyncBatch(List<Serializable> ids, Set<String> asyncIds) {
		getLowLevelSession();

		session.getNodesByIds(ids); // batch fetch

		Map<Serializable, String> titles = new HashMap<Serializable, String>();
		for (Serializable id : ids) {
			Node node = session.getNodeById(id);
			if (asyncIds.contains(id)) {
				node.setSimpleProperty(Model.FULLTEXT_JOBID_PROP, id);
			}
			SimpleProperty prop;
			try {
				prop = node.getSimpleProperty(DC_TITLE);
			} catch (IllegalArgumentException e) {
				continue;
			}
			String title = (String) prop.getValue();
			titles.put(id, title);
			prop.setValue(title + " ");
		}
		session.save();

		for (Serializable id : ids) {
			Node node = session.getNodeById(id);
			SimpleProperty prop;
			try {
				prop = node.getSimpleProperty(DC_TITLE);
			} catch (IllegalArgumentException e) {
				continue;
			}
			prop.setValue(titles.get(id));
		}
		session.save();
	}

	protected void runAsyncBatch(Set<String> asyncIds)
			{
		if (asyncIds.isEmpty()) {
			return;
		}
		String repositoryName = coreSession.getRepositoryName();
		WorkManager workManager = Framework.getLocalService(WorkManager.class);
		for (String id : asyncIds) {
			Work work = new SQLFulltextExtractorWork(repositoryName, id);
			// schedule immediately, we're outside a transaction
			workManager.schedule(work, Scheduling.IF_NOT_SCHEDULED, false);
		}
	}
}