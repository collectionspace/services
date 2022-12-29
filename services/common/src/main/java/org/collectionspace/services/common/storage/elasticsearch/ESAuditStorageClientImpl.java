package org.collectionspace.services.common.storage.elasticsearch;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;

import org.apache.commons.collections4.MultiValuedMap;
import org.collectionspace.services.client.AuditClientUtils;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentException;
import org.elasticsearch.cluster.routing.allocation.decider.Decision.Multi;
import org.nuxeo.elasticsearch.audit.ESAuditBackend;
import org.nuxeo.ecm.platform.audit.service.NXAuditEventsService;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.query.sql.model.Predicate;
import org.nuxeo.ecm.platform.audit.api.AuditLogger;
import org.nuxeo.ecm.platform.audit.api.AuditQueryBuilder;
import org.nuxeo.ecm.platform.audit.api.LogEntry;
import org.nuxeo.ecm.platform.audit.api.Predicates;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.RegistrationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.collectionspace.services.common.storage.elasticsearch.ESAuditConstants;

public class ESAuditStorageClientImpl implements StorageClient {

    private final Logger logger = LoggerFactory.getLogger(ESAuditStorageClientImpl.class);

    protected String logException(Exception e, String msg) {
        String result = null;

        String exceptionMessage = e.getMessage();
        exceptionMessage = exceptionMessage != null ? exceptionMessage : "<No details provided>";
        result = msg = msg + ". Caught exception:" + exceptionMessage;

        if (logger.isTraceEnabled() == true) {
            logger.error(msg, e);
        } else {
            logger.error(msg);
        }

        return result;
    }

	@Override
	public String create(ServiceContext ctx, DocumentHandler handler) throws BadRequestException, DocumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(ServiceContext ctx, String id) throws DocumentNotFoundException, DocumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteWithWhereClause(ServiceContext ctx, String whereClause, DocumentHandler handler)
			throws DocumentNotFoundException, DocumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean delete(ServiceContext ctx, String id, DocumentHandler handler)
			throws DocumentNotFoundException, DocumentException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void get(ServiceContext ctx, DocumentHandler handler) throws DocumentNotFoundException, DocumentException {
	}

	@Override
	public void get(ServiceContext ctx, List<String> csidList, DocumentHandler handler)
			throws DocumentNotFoundException, DocumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void getAll(ServiceContext ctx, DocumentHandler handler)
			throws DocumentNotFoundException, DocumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(ServiceContext ctx, String id, DocumentHandler handler)
			throws BadRequestException, DocumentNotFoundException, DocumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doWorkflowTransition(ServiceContext ctx, String id, DocumentHandler handler,
			TransitionDef transitionDef) throws BadRequestException, DocumentNotFoundException, DocumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean synchronize(ServiceContext ctx, Object specifier, DocumentHandler handler)
			throws DocumentNotFoundException, TransactionException, DocumentException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean synchronizeItem(ServiceContext ctx, AuthorityItemSpecifier itemSpecifier, DocumentHandler handler)
			throws DocumentNotFoundException, TransactionException, DocumentException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(ServiceContext ctx, Object entityFound, DocumentHandler handler)
			throws DocumentNotFoundException, DocumentException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void releaseRepositorySession(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, Object repoSession)
			throws TransactionException {
		// TODO Auto-generated method stub

	}

	private LogEntry getLogEntry(String id, DocumentHandler handler) throws DocumentNotFoundException {
		NXAuditEventsService audit = (NXAuditEventsService) Framework.getRuntime()
				.getComponent(NXAuditEventsService.NAME);

		ESAuditBackend esBackend = (ESAuditBackend) audit.getBackend();
		LogEntry logEntry = null;
		
		try {			
			DocumentFilter docFilter = handler.getDocumentFilter();
			if (docFilter == null) {
					docFilter = handler.createDocumentFilter();
			}
			Predicate filter = Predicates.eq(ESAuditConstants.ES_EVENT_ID, id);

			AuditQueryBuilder builder = new AuditQueryBuilder().offset(docFilter.getOffset()).limit(docFilter.getPageSize());
			logEntry = esBackend.queryLogs(builder.predicates(filter).defaultOrder()).get(0);

			// TO DO: Get accurate total #s
      long totalItems =  esBackend.getEventsCount(AuditClientUtils.CSPACE_EVENT_ID);
      docFilter.setTotalItemsResult(totalItems); // Save the items total in the doc filter for later reporting
		} catch (NuxeoException e) {
			throw new DocumentNotFoundException(e);
		}
		
		return logEntry;
	}
	
	private boolean isAuditServiceReady() {
		boolean result = true;
		
		try {
			RegistrationInfo regInfo = Framework.getRuntime().getComponentManager().getRegistrationInfo(NXAuditEventsService.NAME);
			if (regInfo.getState() == RegistrationInfo.START_FAILURE) {
				result = false;
			}
		} catch (Throwable t) {
			result = false;
		}
		
		return result;
	}
		
	private List<LogEntry> getLogEntries(DocumentFilter docFilter) throws DocumentException {
		if (!isAuditServiceReady()) {
			throw new DocumentException("Nuxeo Audit service failed during startup.");
		}

		NXAuditEventsService audit = (NXAuditEventsService) Framework.getRuntime()
				.getComponent(NXAuditEventsService.NAME);
		
		ESAuditBackend esBackend = (ESAuditBackend) audit.getBackend();
		List<LogEntry> logEntryList = null;
		
		try {

			MultivaluedMap<String, String> params = docFilter.getQueryParams();
			List<Predicate> filters = new ArrayList<Predicate>();

			for (String param : params.keySet()) {
				String value = params.getFirst(param);
				if (param.equals(ESAuditConstants.NUXEO_RECORD_CSID)){
					filters.add(Predicates.eq(ESAuditConstants.ES_RECORD_CSID, value));
				} else if (param.equals(ESAuditConstants.NUXEO_CRATED_BY)) {
					filters.add(Predicates.eq(ESAuditConstants.ES_CREATED_BY, value)); // change this to contains?
				} else if (param.equals(ESAuditConstants.NUXEO_EVENT_TYPE)) {
					filters.add(Predicates.eq(ESAuditConstants.ES_EVENT_TYPE, value));
				} else if (param.equals(ESAuditConstants.NUXEO_EVENT_DATE)) {
					filters.add(Predicates.eq(ESAuditConstants.ES_EVENT_DATE, value));
					// TO DO: Fine-tune this more so that we can pass in YYYY-MM-DD format
				}
			}
			AuditQueryBuilder builder = new AuditQueryBuilder().offset(docFilter.getOffset()).limit(docFilter.getPageSize());
			logEntryList = esBackend.queryLogs(builder.predicates(filters).defaultOrder());

			// TO DO: Figure out out how do get the actual total items
      long totalItems =  esBackend.getEventsCount(AuditClientUtils.CSPACE_EVENT_ID);
      docFilter.setTotalItemsResult(totalItems); // Save the items total in the doc filter for later reporting

		} catch (NuxeoException e) {
			throw new DocumentException(e);
		}

		return logEntryList;
	}

	@Override
	public void getFiltered(ServiceContext ctx, DocumentHandler handler)
			throws DocumentNotFoundException, DocumentException {
        
        DocumentFilter docFilter = handler.getDocumentFilter();
        if (docFilter == null) {
            docFilter = handler.createDocumentFilter();
        }

        try {
            handler.prepare(Action.GET_ALL);
            List<LogEntry> list = this.getLogEntries(docFilter);
            DocumentWrapper<List> wrapDoc = new DocumentWrapperImpl<List>(list);
            handler.handle(Action.GET_ALL, wrapDoc);
            handler.complete(Action.GET_ALL, wrapDoc);
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
        }
    }

	@Override
	public void get(ServiceContext ctx, String id, DocumentHandler handler)
			throws DocumentNotFoundException, DocumentException {

        if (handler == null) {
            throw new IllegalArgumentException(
                    "RepositoryJavaClient.get: handler is missing");
        }

        try {
            handler.prepare(Action.GET);
            LogEntry logEntry = null;
            try {
					
                logEntry = getLogEntry(id, handler);
            } catch (org.nuxeo.ecm.core.api.DocumentNotFoundException ce) {
                String msg = logException(ce,
                		String.format("Could not find %s resource/record with ID=%s", ctx.getDocumentType(), id));
                throw new DocumentNotFoundException(msg, ce);
            }
            DocumentWrapper<LogEntry> wrapDoc = new DocumentWrapperImpl<LogEntry>(logEntry);
            handler.handle(Action.GET, wrapDoc);
            handler.complete(Action.GET, wrapDoc);
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (DocumentException de) {
        	if (logger.isDebugEnabled()) {
        		logger.debug(de.getMessage(), de);
        	}
            throw de;
        } catch (Throwable e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new NuxeoDocumentException(e);
        }
    }
}
