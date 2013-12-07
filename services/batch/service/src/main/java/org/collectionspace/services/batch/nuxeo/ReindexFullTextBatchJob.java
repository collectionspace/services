/*
 * This file contains code from Florent Guillame's nuxeo-reindex-fulltext module.
 * 
 */

package org.collectionspace.services.batch.nuxeo;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
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
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.StoredValuesUriTemplate;
import org.collectionspace.services.common.UriTemplateRegistryKey;
import org.collectionspace.services.common.invocable.InvocationContext.ListCSIDs;
import org.collectionspace.services.common.invocable.InvocationContext.Params.Param;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.nuxeo.ecm.core.api.AbstractSession;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.TransactionalCoreSessionWrapper;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.EventContextImpl;
import org.nuxeo.ecm.core.query.QueryFilter;
import org.nuxeo.ecm.core.query.sql.NXQL;
import org.nuxeo.ecm.core.storage.sql.Model;
import org.nuxeo.ecm.core.storage.sql.ModelFulltext;
import org.nuxeo.ecm.core.storage.sql.Node;
import org.nuxeo.ecm.core.storage.sql.Session;
import org.nuxeo.ecm.core.storage.sql.SimpleProperty;
import org.nuxeo.ecm.core.storage.sql.coremodel.BinaryTextListener;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLSession;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexFullTextBatchJob extends AbstractBatchJob {
	final Logger log = LoggerFactory.getLogger(AbstractBatchJob.class);

    public static final String DC_TITLE = "dc:title";
    public static final int DEFAULT_BATCH_SIZE = 100;
    public static final int DEFAULT_BATCH_PAUSE = 0;
    
    private int batchSize = DEFAULT_BATCH_SIZE;
    private int batchPause = DEFAULT_BATCH_PAUSE;

    private CoreSession coreSession;
    private Session session;
    private ModelFulltext fulltextInfo;
    
    private Map<String, ResourceBase> resourcesByDocType;

    public ReindexFullTextBatchJob() {
    	setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_NO_CONTEXT, INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST));
    }
    
	@Override
	public void run() {
		setCompletionStatus(STATUS_MIN_PROGRESS);
		
		int numAffected = 0;
		
		try {
			coreSession = getRepoSession().getSession();

			if (requestIsForInvocationModeSingle()) {
				String csid = getInvocationContext().getSingleCSID();
				
				if (csid == null) {
					throw new Exception("no singleCSID was supplied");
				}

				String docType = getInvocationContext().getDocType();
				
				if (StringUtils.isEmpty(docType)) {
					throw new Exception("no docType was supplied");
				}

				logger.debug("reindexing " + docType + " record with csid: " + csid);
				
				numAffected = reindexDocument(docType, csid);
			}
			else if (requestIsForInvocationModeList()) {
				ListCSIDs list = getInvocationContext().getListCSIDs();
				List<String> csids = list.getCsid();
				
				if (csids == null || csids.size() == 0) {
					throw new Exception("no listCSIDs were supplied");
				}

				String docType = getInvocationContext().getDocType();

				if (StringUtils.isEmpty(docType)) {
					throw new Exception("no docType was supplied");
				}

				logger.debug("reindexing " + docType + " records with csids: " + StringUtils.join(csids, ", "));
				
				numAffected = reindexDocuments(docType, csids);
			}
			else if (requestIsForInvocationModeNoContext()) {
				Set<String> docTypes = new LinkedHashSet<String>();
				String docType;
				
				docType = getInvocationContext().getDocType();

				if (StringUtils.isNotEmpty(docType)) {
					docTypes.add(docType);					
				}
				
				// Read batch size, pause, and additional doctypes from params.				
				
				for (Param param : this.getParams()) {
					if (param.getKey().equals("batchSize")) {
						batchSize = Integer.parseInt(param.getValue());
					}
					if (param.getKey().equals("batchPause")) {
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

				// This is needed so that resource calls (which start transactions)
		        // will work. Otherwise, a javax.transaction.NotSupportedException 
				// ("Nested transactions are not supported") is thrown.
		        boolean isTransactionActive = TransactionHelper.isTransactionActive();
		        
		        if (isTransactionActive) {
		            TransactionHelper.commitOrRollbackTransaction();
		        }
				
				numAffected = reindexDocuments(docTypes);
				
				// This is needed so that when the session is released after this
				// batch job exits (in BatchDocumentModelHandler), there isn't an exception.
				// Otherwise, a "Session invoked in a container without a transaction active"
				// error is thrown from RepositoryJavaClientImpl.releaseRepositorySession.
			
		        if (isTransactionActive) {
		        	TransactionHelper.startTransaction();
		        }
			}
			
			logger.debug("reindexing complete");
			
			InvocationResults results = new InvocationResults();
			results.setNumAffected(numAffected);
			results.setUserNote("reindexed " + numAffected + " records");
			
			setResults(results);
			setCompletionStatus(STATUS_COMPLETE);
		}
		catch(Exception e) {
			setErrorResult(e.getMessage());
		}
	}
	
	private void initResourceMap() {
		resourcesByDocType = new HashMap<String, ResourceBase>();

		for (ResourceBase resource : getResourceMap().values()) {
			Map<UriTemplateRegistryKey, StoredValuesUriTemplate> entries = resource.getUriRegistryEntries();
			
			for (UriTemplateRegistryKey key : entries.keySet()) {
				String docType = key.getDocType();
				String tenantId = key.getTenantId();
				
				if (getTenantId().equals(tenantId)) {
					resourcesByDocType.put(docType, resource);
				}				
			}
		}
	}
	
	private int reindexDocuments(Set<String> docTypes) throws Exception {
		int affectedCount = 0;

		if (docTypes == null) {
			docTypes = new LinkedHashSet<String>();
		}

		// If no types are specified, do them all.
		
		if (docTypes.size() == 0) {
			docTypes.addAll(getAllDocTypes());
		}
				
		for (String docType : docTypes) {
			affectedCount += reindexDocuments(docType);
		}
		
		return affectedCount;
	}
	
	private List<String> getAllDocTypes() {
		List<String> docTypes = new ArrayList<String>(resourcesByDocType.keySet());
		Collections.sort(docTypes);

		logger.debug("getAllDocTypes found: " + StringUtils.join(docTypes, ", "));
		
		return docTypes;
	}
	
	private int reindexDocuments(String docType) throws Exception {
		int affectedCount = 0;

		log.debug("reindexing docType " + docType);
		
		ResourceBase resource = resourcesByDocType.get(docType);
		
		if (resource == null) {
			log.warn("no resource found for docType " + docType);

			return affectedCount;
		}
		
		boolean isAuthorityItem = false;
		
		if (resource instanceof AuthorityResource) {
			UriTemplateRegistryKey key = new UriTemplateRegistryKey(getTenantId(), docType);
			StoredValuesUriTemplate uriTemplate = resource.getUriRegistryEntries().get(key);
			
			log.debug(uriTemplate.toString());
			
			// A bit of a hack to determine if this docType is an authority or an item.
			// If the URL contains "/items/", it's an item.
			
			if (StringUtils.contains(uriTemplate.toString(), "/" + AuthorityClient.ITEMS + "/")) {
				isAuthorityItem = true;
			}
		}
	
		int pageSize = batchSize;

		if (isAuthorityItem) {
			List<String> vocabularyCsids = getVocabularyCsids((AuthorityResource<?, ?>) resource);

			for (String vocabularyCsid : vocabularyCsids) {
				int pageNum = 0;
				List<String> csids = null;

				log.debug("reindexing vocabulary of " + docType + " with csid " + vocabularyCsid);
				
				do {
					csids = findAllAuthorityItems((AuthorityResource<?, ?>) resource, vocabularyCsid, pageSize, pageNum);
					
					if (csids.size() > 0) {
						log.debug("reindexing vocabulary of " + docType +" with csid " + vocabularyCsid + ", batch " + (pageNum + 1) + ": " + csids.size() + " records starting with " + csids.get(0));
						
						affectedCount += reindexDocuments(docType, csids);
					}
					
					pageNum++;
				}
				while(csids.size() == pageSize);
			}
		}
		else {
			int pageNum = 0;
			List<String> csids = null;

			do {
				csids = findAll(resource, pageSize, pageNum);
				
				if (csids.size() > 0) {
					log.debug("reindexing " + docType +" batch " + (pageNum + 1) + ": " + csids.size() + " records starting with " + csids.get(0));
					
					affectedCount += reindexDocuments(docType, csids);
				}
				
				pageNum++;
			}
			while(csids.size() == pageSize);
		}
		
		return affectedCount;
	}
	
	private int reindexDocument(String docType, String csid) throws Exception {
		return reindexDocuments(docType, Arrays.asList(csid));
	}
	
	private int reindexDocuments(String docType, List<String> csids) throws Exception {
		// Convert the csids to structs of nuxeo id and type, as expected
		// by doBatch.

		if (csids == null || csids.size() == 0) {
			return 0;
		}
		
        getLowLevelSession();        
        List<Info> infos = new ArrayList<Info>();

        String query = "SELECT ecm:uuid, ecm:primaryType FROM Document " +
                       "WHERE ecm:name IN (" + StringUtils.join(quoteList(csids), ',') + ") " +
                       "AND ecm:primaryType LIKE '" + docType + "%'";
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
        
        if (batchPause > 0) {
        	log.trace("pausing " + batchPause + " ms");
        	
        	Thread.sleep(batchPause);
        }
        
        doBatch(infos);
        
        return infos.size();
	}
	
	private List<String> quoteList(List<String> values) {
		List<String> quoted = new ArrayList<String>(values.size());
		
		for (String value : values) {
			quoted.add("'" + value + "'");
		}
		
		return quoted;
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
        public final String id;

        public final String type;

        public Info(String id, String type) {
            this.id = id;
            this.type = type;
        }
    }
    
    /**
     * Launches a fulltext reindexing of the database.
     *
     * @param batchSize the batch size, defaults to 100
     * @param batch if present, the batch number to process instead of all
     *            batches; starts at 1
     * @return when done, ok + the total number of docs
     */
    public String reindexFulltext(int batchSize, int batch) throws Exception {
        Principal principal = coreSession.getPrincipal();
        if (!(principal instanceof NuxeoPrincipal)) {
            return "unauthorized";
        }
        NuxeoPrincipal nuxeoPrincipal = (NuxeoPrincipal) principal;
        if (!nuxeoPrincipal.isAdministrator()) {
            return "unauthorized";
        }

        log("Reindexing starting");
        if (batchSize <= 0) {
            batchSize = DEFAULT_BATCH_SIZE;
        }
        List<Info> infos = getInfos();
        int size = infos.size();
        int numBatches = (size + batchSize - 1) / batchSize;
        if (batch < 0 || batch > numBatches) {
            batch = 0; // all
        }
        batch--;

        log("Reindexing of %s documents, batch size: %s, number of batches: %s",
                size, batchSize, numBatches);
        if (batch >= 0) {
            log("Reindexing limited to batch: %s", batch + 1);
        }

        boolean tx = TransactionHelper.isTransactionActive();
        if (tx) {
            TransactionHelper.commitOrRollbackTransaction();
        }

        int n = 0;
        int errs = 0;
        for (int i = 0; i < numBatches; i++) {
            if (batch >= 0 && batch != i) {
                continue;
            }
            int pos = i * batchSize;
            int end = pos + batchSize;
            if (end > size) {
                end = size;
            }
            List<Info> batchInfos = infos.subList(pos, end);
            log("Reindexing batch %s/%s, first id: %s", i + 1, numBatches,
                    batchInfos.get(0).id);
            try {
                doBatch(batchInfos);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Error processing batch " + i + 1, e);
                errs++;
            }
            n += end - pos;
        }

        log("Reindexing done");
        if (tx) {
            TransactionHelper.startTransaction();
        }
        return "done: " + n + " total: " + size + " batch_errors: " + errs;
    }

    protected void log(String format, Object... args) {
        log.warn(String.format(format, args));
    }

    /**
     * This has to be called once the transaction has been started.
     */
    protected void getLowLevelSession() throws Exception {
        CoreSession cs;
        if (Proxy.isProxyClass(coreSession.getClass())) {
            TransactionalCoreSessionWrapper w = (TransactionalCoreSessionWrapper) Proxy.getInvocationHandler(coreSession);
            Field f1 = TransactionalCoreSessionWrapper.class.getDeclaredField("session");
            f1.setAccessible(true);
            cs = (CoreSession) f1.get(w);
        } else {
            cs = coreSession;
        }

        SQLSession s = (SQLSession) ((AbstractSession) cs).getSession();
        Field f2 = SQLSession.class.getDeclaredField("session");
        f2.setAccessible(true);
        session = (Session) f2.get(s);
        fulltextInfo = session.getModel().getFulltextInfo();
    }

    protected List<Info> getInfos() throws Exception {
        getLowLevelSession();
        List<Info> infos = new ArrayList<Info>();
        String query = "SELECT ecm:uuid, ecm:primaryType FROM Document"
                + " WHERE ecm:isProxy = 0"
                + " AND ecm:currentLifeCycleState <> 'deleted'"
                + " ORDER BY ecm:uuid";
        IterableQueryResult it = session.queryAndFetch(query, NXQL.NXQL,
                QueryFilter.EMPTY);
        try {
            for (Map<String, Serializable> map : it) {
                String id = (String) map.get(NXQL.ECM_UUID);
                String type = (String) map.get(NXQL.ECM_PRIMARYTYPE);
                infos.add(new Info(id, type));
            }
        } finally {
            it.close();
        }
        return infos;
    }

    protected void doBatch(List<Info> infos) throws Exception {
        getLowLevelSession(); // for fulltextInfo
        List<Serializable> ids = new ArrayList<Serializable>(infos.size());
        Set<Serializable> asyncIds = new HashSet<Serializable>();
        for (Info info : infos) {
            ids.add(info.id);
            if (fulltextInfo.isFulltextIndexable(info.type)) {
                asyncIds.add(info.id);
            }
        }

        boolean tx;
        boolean ok;

        // transaction for the sync batch
        tx = TransactionHelper.startTransaction();
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

        // transaction for the async batch firing (needs session)
        tx = TransactionHelper.startTransaction();
        ok = false;
        try {
            runAsyncBatch(asyncIds);
            ok = true;
        } finally {
            if (tx) {
                if (!ok) {
                    TransactionHelper.setTransactionRollbackOnly();
                    log.error("Rolling back async fire");
                }
                TransactionHelper.commitOrRollbackTransaction();
            }
        }

        // wait for async completion after transaction commit
        Framework.getLocalService(EventService.class).waitForAsyncCompletion();
    }

    /*
     * Do this at the low-level session level because we may have to modify
     * things like versions which aren't usually modifiable, and it's also good
     * to bypass all listeners.
     */
    protected void runSyncBatch(List<Serializable> ids,
            Set<Serializable> asyncIds) throws Exception {
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

    protected void runAsyncBatch(Set<Serializable> asyncIds)
            throws ClientException {
        if (asyncIds.isEmpty()) {
            return;
        }
        EventContext eventContext = new EventContextImpl(asyncIds, fulltextInfo);
        eventContext.setRepositoryName(coreSession.getRepositoryName());
        Event event = eventContext.newEvent(BinaryTextListener.EVENT_NAME);
        EventService eventService = Framework.getLocalService(EventService.class);
        eventService.fireEvent(event);
    }
}
