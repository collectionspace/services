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
 */
package org.collectionspace.services.common.document;

import java.util.Map;

import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.query.QueryContext;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.collectionspace.services.lifecycle.Lifecycle;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 *
 * DocumentHandler provides document processing methods. It is an interface
 * between repository client and CollectionSpace service resource. It provides
 * methods to setup request via repository client and handle its response.
 *
 * Typical call sequence is:
 * Create handler and repository client
 * Call XXX operation on the repository client and pass the handler
 * repository client calls prepare on the handler
 * The repository client then calls handle on the handler
 *
 * T - Entity Type (e.g. CollectionObjectsCommon)
 * TL - Entity List Type (e.g. CollectionObjectsCommonList)
 * WT - Wrapped Type (e.g. DocumentModel)
 * WTL - Wrapped List Type (e.g. DocumentModelList)
 *
 */
public interface DocumentHandler<T, TL, WT, WTL> {

    public enum Action {
        CREATE, GET, GET_ALL, UPDATE, DELETE, WORKFLOW, SYNC
    }
    
    public Lifecycle getLifecycle();
    
    public Lifecycle getLifecycle(String serviceObjectName);

    /**
     * getServiceContext returns service context
     * @return
     */
    public ServiceContext getServiceContext();

    /**
     * getServiceContextPath such as "/collectionobjects/"
     * @return
     */
    public String getServiceContextPath();

    /**
     * setServiceContext sets service contex to the handler
     * @param ctx
     */
    public void setServiceContext(ServiceContext ctx);

    /**
     * prepare is called by the client for preparation of stuff before
     * invoking repository operation. this is mainly useful for create and 
     * update kind of actions
     * @param action
     * @throws Exception
     */
    public void prepare(Action action) throws Exception;

    /**
     * updateWorkflowTransition - prepare for a workflow transition
     */
    public void handleWorkflowTransition(ServiceContext ctx, DocumentWrapper<DocumentModel> wrapDoc, TransitionDef transitionDef) throws Exception;
    
    /**
     * prepareCreate processes documents before creating document in repository

     * @throws Exception
     */
    public void prepareCreate() throws Exception;

    /**
     * prepareUpdate processes documents for the update of document in repository
     * @throws Exception
     */
    public void prepareUpdate() throws Exception;

    /**
     * prepareGet processes query before retrieving document from
     * repository
     * @throws Exception
     */
    public void prepareGet() throws Exception;

    /**
     * prepareGetAll processes query before retrieving document(s) from
     * repository
     * @throws Exception
     */
    public void prepareGetAll() throws Exception;

    /**
     * prepareDelete processes delete before deleting document from repository
     * @throws Exception
     */
    public void prepareDelete() throws Exception;

    /**
     * prepare is called by the client to hand over the document processing task
     * @param action 
     * @param doc wrapped doc
     * @throws Exception
     */
    public boolean handle(Action action, DocumentWrapper<?> docWrap) throws Exception;

    /**
     * handleCreate processes documents before creating document in repository
     * @param wrapDoc
     * @throws Exception
     */
    public void handleCreate(DocumentWrapper<WT> wrapDoc) throws Exception;

    /**
     * handleUpdate processes documents for the update of document in repository
     * @param wrapDoc
     * @throws Exception
     */
    public void handleUpdate(DocumentWrapper<WT> wrapDoc) throws Exception;

    /**
     * handleGet processes documents from repository before responding to consumer
     * @param wrapDoc
     * @throws Exception
     */
    public void handleGet(DocumentWrapper<WT> wrapDoc) throws Exception;

    /**
     * handleGetAll processes documents from repository before responding to consumer
     * @param wrapDoc
     * @throws Exception
     */
    public void handleGetAll(DocumentWrapper<WTL> wrapDoc) throws Exception;

    /**
     * handleDelete processes documents for the deletion of document in repository
     * @param wrapDoc
     * @throws Exception
     */
    public boolean handleDelete(DocumentWrapper<WT> wrapDoc) throws Exception;

    /**
     * complete is called by the client to provide an opportunity to the handler
     * to take care of stuff before closing session with the repository. example
     * could be to reclaim resources or to populate response to the consumer
     * @param wrapDoc
     * @throws Exception
     */
    public void complete(Action action, DocumentWrapper<?> wrapDoc) throws Exception;

    /**
     * completeCreate is called by the client to indicate completion of the create call.
     * @param wrapDoc
     * @throws Exception
     */
    public void completeCreate(DocumentWrapper<WT> wrapDoc) throws Exception;

    /**
     * completeUpdate is called by the client to indicate completion of the update call.
     * @param wrapDoc
     * @throws Exception
     */
    public void completeUpdate(DocumentWrapper<WT> wrapDoc) throws Exception;

    /**
     * completeGetis called by the client to indicate completion of the get call.
     * @param wrapDoc
     * @throws Exception
     */
    public void completeGet(DocumentWrapper<WT> wrapDoc) throws Exception;

    /**
     * completeGetAll is called by the client to indicate completion of the getall.
     * @param wrapDoc
     * @throws Exception
     */
    public void completeGetAll(DocumentWrapper<WTL> wrapDoc) throws Exception;

    /**
     * completeDelete is called by the client to indicate completion of the delete call.
     * @param wrapDoc
     * @throws Exception
     */
    public void completeDelete(DocumentWrapper<WT> wrapDoc) throws Exception;

    /**
     * extractCommonPart extracts common part of a CS object from given document.
     * this is usually called AFTER the get operation is invoked on the repository.
     * Called in handle GET/GET_ALL actions.
     * @param docWrap document
     * @return common part of CS object
     * @throws Exception
     */
    public T extractCommonPart(DocumentWrapper<WT> docWrap) throws Exception;

    /**
     * fillCommonPart sets common part of CS object into given document
     * this is usually called BEFORE create/update operations are invoked on the
     * repository. Called in handle CREATE/UPDATE actions.
     * @param obj input object
     * @param docWrap target document
     * @throws Exception
     */
    public void fillCommonPart(T obj, DocumentWrapper<WT> docWrap) throws Exception;

    /**
     * extractCommonPart extracts common part of a CS object from given document.
     * this is usually called AFTER bulk get (index/list) operation is invoked on
     * the repository
     * @param docWrap document
     * @return common part of CS object
     * @throws Exception
     */
    public TL extractCommonPartList(DocumentWrapper<WTL> docWrap) throws Exception;
    

    /**
     * Extract paging info.
     *
     * @param theCommonList the the common list
     * @param wrapDoc the wrap doc
     * @return the tL
     * @throws Exception the exception
     */
    public TL extractPagingInfo(TL theCommonList, DocumentWrapper<WTL> wrapDoc)	throws Exception;    

    /**
     * fillCommonPartList sets list common part of CS object into given document
     * this is usually called BEFORE bulk create/update on the repository
     * (not yet supported)
     * @param obj input object
     * @param docWrap target document
     * @throws Exception
     */
    public void fillCommonPartList(TL obj, DocumentWrapper<WTL> docWrap) throws Exception;

    /**
     * getProperties
     * @return
     */
    public Map<String, Object> getProperties();

    /**
     * setProperties provides means to the CollectionSpace service resource to
     * set up parameters before invoking any request via the client.
     * @param properties
     */
    public void setProperties(Map<String, Object> properties);

    /**
     * createDocumentFilter is a factory method to create a document
     * filter that is relevant to be used with this document handler
     * and corresponding storage client
     * 
     * @return
     */
    public DocumentFilter createDocumentFilter();

    /**
     * getDocumentFilter
     * @return
     */
    public DocumentFilter getDocumentFilter();

    /**
     * setDocumentFilter provides means to the CollectionSpace service resource to
     * set up DocumentFilter values before invoking any request via the client.
     * @param docFilter
     */
    public void setDocumentFilter(DocumentFilter docFilter);

    /**
     * getCommonPart provides the common part of a CS object.
     * @return common part of CS object
     */
    public T getCommonPart();

    /**
     * setCommonPart sets common part of CS object as input for operation on
     * repository
     * @param obj input object
     */
    public void setCommonPart(T obj);

    /**
     * getCommonPartList provides the default list object of a CS object.
     * @return default list of CS object
     */
    public TL getCommonPartList();

    /**
     * setCommonPartList sets common part list entry for CS object as input for operation on
     * repository
     * @param default list of CS object
     */
    public void setCommonPartList(TL obj);

    /**
     * getQProperty get qualified property (useful for mapping to repository document property)
     * @param prop
     * @return
     * @throws DocumentException 
     */
    public String getQProperty(String prop) throws DocumentException;

    /**
     * getUnQProperty unqualifies document property from repository
     * @param qProp qualifeid property
     * @return unqualified property
     */
    public String getUnQProperty(String qProp);
    
    /**
     * get a query string that will be used to return a set of documents that should be indexed/re-index
     * @throws Exception 
     * @throws DocumentException 
     */
    public String getDocumentsToIndexQuery(String indexId, String csid) throws DocumentException, Exception;

    /**
     * get a query string that will be used to return a set of documents that should be indexed/re-index
     * @throws Exception
     * @throws DocumentException
     */
    public String getDocumentsToIndexQuery(String indexId, String documentType, String csid) throws DocumentException, Exception;

    /**
     * Creates the CMIS query from the service context.  Each document handler is responsible for returning a valid CMIS query using the
     * information in the current service context -which includes things like the query parameters, etc.
     * @throws DocumentException 
     */
    public String getCMISQuery(QueryContext queryContext) throws DocumentException;
    
    /**
     * Returns TRUE if a CMIS query should be used (instead of an NXQL query)
     */
    public boolean isCMISQuery();

    /**
     * Returns TRUE if a JDBC/SQL query should be used (instead of an NXQL query)
     */
    public boolean isJDBCQuery();
    
    /**
     * Returns parameter values, relevant to this document handler, that can be used in JDBC/SQL queries
     * 
     * @return a set of zero or more parameter values relevant to this handler
     */
    public Map<String,String> getJDBCQueryParams();

    /**
     * 
     * @throws Exception
     */
	void prepareSync() throws Exception;

	/**
	 * 
	 * @param wrapDoc
	 * @throws Exception
	 */
	boolean handleSync(DocumentWrapper<Object> wrapDoc) throws Exception;

	/**
	 * 
	 * @param wrapDoc
	 * @throws Exception
	 */
	void completeSync(DocumentWrapper<Object> wrapDoc) throws Exception;

	public void sanitize(DocumentWrapper<WT> wrapDoc);

	/**
	 * Should return true if the document supports workflow states (usually, just Nuxeo documents/records)
	 * @return
	 */
	public boolean supportsWorkflowStates();

}
