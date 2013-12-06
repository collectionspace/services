/*
 * This is an adaptation of Florent Guillame's nuxeo-reindex-fulltext module,
 * modified to run as a CollectionSpace batch job. Original copyright below. 
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.batch.AbstractBatchInvocable;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ServiceMain;
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
//import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * JAX-RS component used to do fulltext reindexing of the whole database.
 *
 */
//@Path("reindexFulltext")
public class ReindexFullTextBatchJob extends AbstractBatchJob {

    public static Log log = LogFactory.getLog(ReindexFullTextBatchJob.class);

    protected static final String DC_TITLE = "dc:title";

    protected static final int DEFAULT_BATCH_SIZE = 100;
    
    protected static int batchSize = DEFAULT_BATCH_SIZE;

    @Context
    protected HttpServletRequest request;

    protected CoreSession coreSession;

    protected Session session;

    protected ModelFulltext fulltextInfo;

    protected static class Info {
        public final String id;

        public final String type;

        public Info(String id, String type) {
            this.id = id;
            this.type = type;
        }
    }
   
    protected Map<String, ResourceBase> resourcesByDocType;

    public ReindexFullTextBatchJob() {
    	setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_NO_CONTEXT, INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST));
    }
    
	@Override
	public void run() {
		setCompletionStatus(STATUS_MIN_PROGRESS);
		
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
					throw new Exception("no docType was supplied");
				}

				logger.debug("reindexing " + docType + " records with csids: " + StringUtils.join(csids, ", "));
				
				reindexDocuments(docType, csids);
			}
			else if (requestIsForInvocationModeNoContext()) {
				Set<String> docTypes = new LinkedHashSet<String>();
				String docType;
				
				docType = getInvocationContext().getDocType();

				if (StringUtils.isNotEmpty(docType)) {
					docTypes.add(docType);					
				}
				
				// Read batch size and additional doctypes from params.				
				
				for (Param param : this.getParams()) {
					if (param.getKey().equals("batchSize")) {
						batchSize = Integer.parseInt(param.getValue());
					}
					else if (param.getKey().equals("docType")) {
						docType = param.getValue();
						
						if (StringUtils.isNotEmpty(docType)) {
							docTypes.add(docType);					
						}
					}
				}
				
				// FIXME: This is needed so that resource calls (which start transactions)
		        // will work. Otherwise, a javax.transaction.NotSupportedException 
				// ("Nested transactions are not supported") is thrown.
		        TransactionHelper.commitOrRollbackTransaction();

				initResources();
				reindexDocuments(docTypes);
				
				// FIXME: This is needed so that when the session is released after this
				// batch job exits (in BatchDocumentModelHandler), there isn't an exception.
				// Otherwise, a "Session invoked in a container without a transaction active"
				// error is thrown from RepositoryJavaClientImpl.releaseRepositorySession.
				TransactionHelper.startTransaction();
				
	//			
	//			try {
	//				coreSession = getRepoSession().getSession();
	//				String message = reindexFulltext(docType, batchSize, batch);
	//				
	//				InvocationResults results = new InvocationResults();
	//				results.setUserNote(message);
	//				
	//				setResults(results);
	//				setCompletionStatus(STATUS_COMPLETE);
	//			}
	//			catch(Exception e) {
	//				this.setErrorResult(e.getMessage());
	//			}
			}
			
			logger.debug("reindexing complete");
			
			setCompletionStatus(STATUS_COMPLETE);
		}
		catch(Exception e) {
			setErrorResult(e.getMessage());
		}
	}
	
	private void initResources() {
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

		logger.debug("getAllDocTypes found: " + StringUtils.join(docTypes, ", "));
		
		return docTypes;
	}
	
	private void reindexDocuments(String docType) throws Exception {
		log.debug("reindexing docType " + docType);
		
		ResourceBase resource = resourcesByDocType.get(docType);
		
		if (resource == null) {
			log.warn("no resource found for docType " + docType);
			return;
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
			List<String> vocabularyCsids = getVocabularyCsids((AuthorityResource) resource);

			for (String vocabularyCsid : vocabularyCsids) {
				int pageNum = 0;
				List<String> csids = null;

				log.debug("reindexing vocabulary of " + docType + " with csid " + vocabularyCsid);
				
				do {
					csids = findAllAuthorityItems((AuthorityResource) resource, vocabularyCsid, pageSize, pageNum);
					
					if (csids.size() > 0) {
						log.debug("reindexing vocabulary of " + docType +" with csid " + vocabularyCsid + ", batch " + (pageNum + 1) + ": " + csids.size() + " records starting with " + csids.get(0));
						
						reindexDocuments(docType, csids);
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
					
					reindexDocuments(docType, csids);
				}
				
				pageNum++;
			}
			while(csids.size() == pageSize);
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
        
        //doBatch(infos);
	}
	
	private List<String> quoteList(List<String> values) {
		List<String> quoted = new ArrayList<String>(values.size());
		
		for (String value : values) {
			quoted.add("'" + value + "'");
		}
		
		return quoted;
	}
	
//    @GET
//    public String get(@QueryParam("batchSize") int batchSize,
//            @QueryParam("batch") int batch) throws Exception {
//        coreSession = SessionFactory.getSession(request);
//        return reindexFulltext(batchSize, batch);
//    }
    
    /**
     * Launches a fulltext reindexing of the database.
     *
     * @param batchSize the batch size, defaults to 100
     * @param batch if present, the batch number to process instead of all
     *            batches; starts at 1
     * @return when done, ok + the total number of docs
     */
    public String reindexFulltext(String docType, int batchSize, int batch) throws Exception {
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
        List<Info> infos = getInfos(docType);
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

    protected List<Info> getInfos(String docType) throws Exception {
        getLowLevelSession();
        List<Info> infos = new ArrayList<Info>();
        String query = "SELECT ecm:uuid, ecm:primaryType FROM Document"
                + " WHERE ecm:isProxy = 0"
        		+ " AND ecm:primaryType LIKE '" + docType + "%'"
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
