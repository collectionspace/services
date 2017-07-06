package org.collectionspace.services.nuxeo.client.java;

import java.security.Principal;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.impl.LifeCycleFilter;

public interface CoreSessionInterface {

	public CoreSession getCoreSession();
	
    public void setTransactionRollbackOnly();
    
    public boolean isTransactionMarkedForRollbackOnly();
	
    /**
     * Gets the root document of this repository.
     *
     * @return the root document. cannot be null
     * @throws ClientException
     * @throws SecurityException
     */
    public DocumentModel getRootDocument() throws ClientException;
    
    /**
     * Gets the current session id.
     * <p>
     * If the client is not connected returns null.
     *
     * @return the session id or null if not connected
     */
    public String getSessionId();

    /**
     * 
     * @throws Exception
     */
    public void close() throws Exception;
    
    /**
     * Returns the repository name against which this core session is bound.
     *
     * @return the repository name used currently used as an identifier
     */
    public String getRepositoryName();
    
    /**
     * Gets the principal that created the client session.
     *
     * @return the principal
     */
    public Principal getPrincipal();

    public IterableQueryResult queryAndFetch(String query, String queryType,
            Object... params) throws ClientException;

    public DocumentModelList query(String query, Filter filter, long limit,
            long offset, boolean countTotal) throws ClientException;

    public DocumentModelList query(String query) throws ClientException;

    /**
     * Executes the given NXQL query an returns the result.
     *
     * @param query the query to execute
     * @param max number of document to retrieve
     * @return the query result
     * @throws ClientException
     */
    public DocumentModelList query(String query, int max) throws ClientException;
    
    /**
     * Executes the given NXQL query and returns the result that matches the
     * filter.
     *
     * @param query the query to execute
     * @param filter the filter to apply to result
     * @return the query result
     * @throws ClientException
     */
    public DocumentModelList query(String query, LifeCycleFilter workflowStateFilter);

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
    public DocumentModel getDocument(DocumentRef docRef) throws ClientException;

    public DocumentModel saveDocument(DocumentModel docModel) throws ClientException;

    public void save() throws ClientException;

    /**
     * Bulk document saving.
     *
     * @param docModels the document models that needs to be saved
     * @throws ClientException
     */
    public void saveDocuments(DocumentModel[] docModels) throws ClientException;

    /**
     * Removes this document and all its children, if any.
     *
     * @param docRef the reference to the document to remove
     * @throws ClientException
     */
    public void removeDocument(DocumentRef docRef) throws ClientException;

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
    public DocumentModel createDocumentModel(String parentPath, String id,
            String typeName) throws ClientException;
    
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
    public DocumentModel createDocument(DocumentModel model) throws ClientException;
    
    /**
     * Gets the children of the given parent.
     *
     * @param parent the parent reference
     * @return the children if any, an empty list if no children or null if the
     *         specified parent document is not a folder
     * @throws ClientException
     */
    public DocumentModelList getChildren(DocumentRef parent) throws ClientException;

    
}
