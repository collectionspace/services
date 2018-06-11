package org.collectionspace.services.nuxeo.client.java;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.collectionspace.services.common.document.DocumentException;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.impl.LifeCycleFilter;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.runtime.transaction.TransactionHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreSessionWrapper implements CoreSessionInterface {

	private CoreSession repoSession;
	private boolean transactionSetForRollback = false;
	
    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(CoreSessionWrapper.class);
    
    private void logQuery(String query) {
    	logger.debug(String.format("NXQL: %s", query));
    }
    
    private void logQuery(String query, String queryType) {
    	logger.debug(String.format("Query Type: '%s' NXQL: %s", queryType, query));
    }
    
    private void logQuery(String query, Filter filter, long limit,
    		long offset, boolean countTotal) {
    	logger.debug(String.format("Filter: '%s', Limit: '%d', Offset: '%d', Count Total?: %b, NXQL: %s",
    			filter != null ? filter.toString() : "none", limit, offset, countTotal, query));
    }
    
	public CoreSessionWrapper(CoreSession repoSession) {
		this.repoSession = repoSession;
	}

	/*
	 * Mark this session's transaction for rollback only
	 */
	@Override
    public void setTransactionRollbackOnly() {
		TransactionHelper.setTransactionRollbackOnly();
    	transactionSetForRollback = true;
    }
	
	@Override
    public boolean isTransactionMarkedForRollbackOnly() {
		if (transactionSetForRollback != TransactionHelper.isTransactionMarkedRollback()) {
			logger.error(String.format("Transaction status is in an inconsistent state.  Internal state is '%b'.  TransactionHelper statis is '%b'.",
					transactionSetForRollback, TransactionHelper.isTransactionMarkedRollback()));
		}
    	return transactionSetForRollback;
    }
	
	@Override
	public 	CoreSession getCoreSession() {
		return repoSession;
	}
	
	@Override
    public String getSessionId() {
    	return repoSession.getSessionId();
    }
    
    @Override
    public void close() throws Exception {
    	try {
    		repoSession.close();
    	} catch (Throwable t) {
    		logger.error(String.format("Could not close session for repository '%s'.", this.repoSession.getRepositoryName()),
    				t);
    	}
    }

    /**
     * Gets the root document of this repository.
     *
     * @return the root document. cannot be null
     * @throws ClientException
     * @throws SecurityException
     */
	@Override
    public DocumentModel getRootDocument() throws ClientException {
    	return repoSession.getRootDocument();
    }
    
    /**
     * Returns the repository name against which this core session is bound.
     *
     * @return the repository name used currently used as an identifier
     */
	@Override
    public String getRepositoryName() {
		return repoSession.getRepositoryName();
	}
    
    /**
     * Gets the principal that created the client session.
     *
     * @return the principal
     */
	@Override
	public Principal getPrincipal() {
		return repoSession.getPrincipal();
	}
	
	private String toLocalTimestamp(String utcTime, boolean base64Encoded) throws DocumentException {
		String result = null;

		try {
			if (base64Encoded == true) {
				utcTime = URLDecoder.decode(utcTime, java.nio.charset.StandardCharsets.UTF_8.name());
			}
			LocalDateTime localTime;
			try {
				Instant instant = Instant.parse(utcTime);
				ZonedDateTime localInstant = instant.atZone(ZoneId.systemDefault()); // DateTimeFormatter.ISO_LOCAL_DATE_TIME
				localTime = localInstant.toLocalDateTime();
			} catch (DateTimeParseException e) {
				localTime = LocalDateTime.parse(utcTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			}
			result = localTime.toString();
			if (base64Encoded == true) {
				result = URLEncoder.encode(result, java.nio.charset.StandardCharsets.UTF_8.name());
			}
		} catch (UnsupportedEncodingException e) {
			throw new DocumentException(e);
		}
		
		return result;
	}
	
	private String localizeTimestamps(String query) throws DocumentException {
		String result = query;
		
		if (query.contains("TIMESTAMP")) {
			StringBuffer stringBuffer = new StringBuffer();
			Pattern pattern = Pattern.compile("\\sTIMESTAMP\\s\"(.+?)\"");
			Matcher matcher = pattern.matcher(query);
			while (matcher.find()) {
				String time = matcher.group(1);
				String localizedTime = toLocalTimestamp(time, false);
				matcher.appendReplacement(stringBuffer, String.format(" TIMESTAMP \"%s\"", localizedTime));
			}
			matcher.appendTail(stringBuffer);
			result = stringBuffer.toString();
		}
		
		return result;
	}

	@Override
	public IterableQueryResult queryAndFetch(String query, String queryType,
            Object... params) throws ClientException, DocumentException {
		query = localizeTimestamps(query);
		logQuery(query, queryType);
		return repoSession.queryAndFetch(query, queryType, params);
	}

	@Override
	public DocumentModelList query(String query, Filter filter, long limit,
            long offset, boolean countTotal) throws ClientException, DocumentException {
		query = localizeTimestamps(query);
		logQuery(query, filter, limit, offset, countTotal);
		return repoSession.query(query, filter, limit, offset, countTotal);
	}

	@Override
    public DocumentModelList query(String query, int max) throws ClientException, DocumentException {
		query = localizeTimestamps(query);
		logQuery(query);
    	return repoSession.query(query, max);
    }
    
	@Override
	public DocumentModelList query(String query) throws ClientException, DocumentException {
		query = localizeTimestamps(query);
		logQuery(query);
		return repoSession.query(query);
	}
	
	@Override
	public DocumentModelList query(String query, LifeCycleFilter workflowStateFilter) throws DocumentException {
		query = localizeTimestamps(query);
		return repoSession.query(query, workflowStateFilter);
	}

    /**
     * Gets a document model given its reference.
     * <p>
     * The default schemas are used to populate the returned document model.
     * Default schemas are configured via the document type manager.
     * <p>
     * Any other data model not part of the default schemas will be lazily
     * loaded as needed.
     *
     * @param docRef the document reference
     * @return the document
     * @throws ClientException
     * @throws SecurityException
     */
    @Override
    public DocumentModel getDocument(DocumentRef docRef) throws ClientException {
	    return repoSession.getDocument(docRef);
    }

    @Override
    public DocumentModel saveDocument(DocumentModel docModel) throws ClientException {
    	DocumentModel result = null;
    	
    	try {
    		if (isTransactionMarkedForRollbackOnly() == false) {
    			result = repoSession.saveDocument(docModel);
    		} else {
        		logger.trace(String.format("The repository session on thread '%d' has a transaction that is marked for rollback.",
        				Thread.currentThread().getId()));
        	}
    	} catch (Throwable t) {
    		setTransactionRollbackOnly();
    		throw t;
    	}
    	
    	return result;
    }

    @Override
    public void save() throws ClientException {
    	try {
    		if (isTransactionMarkedForRollbackOnly() == false) {
    			repoSession.save();
    		} else {
        		logger.trace(String.format("The repository session on thread '%d' has a transaction that is marked for rollback.",
        				Thread.currentThread().getId()));
        	}
    	} catch (Throwable t) {
    		setTransactionRollbackOnly();
    		throw t;
    	}
    }

    /**
     * Bulk document saving.
     *
     * @param docModels the document models that needs to be saved
     * @throws ClientException
     */
    @Override
    public void saveDocuments(DocumentModel[] docModels) throws ClientException {
    	try {
	    	if (isTransactionMarkedForRollbackOnly() == false) {
	    		repoSession.saveDocuments(docModels);
	    	} else {
        		logger.trace(String.format("The repository session on thread '%d' has a transaction that is marked for rollback.",
        				Thread.currentThread().getId()));
        	}
    	} catch (Throwable t) {
    		setTransactionRollbackOnly();
    		throw t;
    	}
    }

    /**
     * Removes this document and all its children, if any.
     *
     * @param docRef the reference to the document to remove
     * @throws ClientException
     */
    @Override
    public void removeDocument(DocumentRef docRef) throws ClientException {
    	repoSession.removeDocument(docRef);
    }

    /**
     * Creates a document model using required information.
     * <p>
     * Used to fetch initial datamodels from the type definition.
     * <p>
     * DocumentModel creation notifies a
     * {@link DocumentEventTypes.EMPTY_DOCUMENTMODEL_CREATED} so that core event
     * listener can initialize its content with computed properties.
     *
     * @param parentPath
     * @param id
     * @param typeName
     * @return the initial document model
     * @throws ClientException
     */
    @Override
    public DocumentModel createDocumentModel(String parentPath, String id,
            String typeName) throws ClientException {
    	return repoSession.createDocumentModel(parentPath, id, typeName);
    }
    
    /**
     * Creates a document using given document model for initialization.
     * <p>
     * The model contains path of the new document, its type and optionally the
     * initial data models of the document.
     * <p>
     *
     * @param model the document model to use for initialization
     * @return the created document
     * @throws ClientException
     */
    @Override
    public DocumentModel createDocument(DocumentModel model) throws ClientException {
    	return repoSession.createDocument(model);
    }
    
    /**
     * Gets the children of the given parent.
     *
     * @param parent the parent reference
     * @return the children if any, an empty list if no children or null if the
     *         specified parent document is not a folder
     * @throws ClientException
     */
    @Override
    public DocumentModelList getChildren(DocumentRef parent) throws ClientException {
    	return repoSession.getChildren(parent);
    }

    
	
}
