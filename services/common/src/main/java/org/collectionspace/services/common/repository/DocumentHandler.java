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
package org.collectionspace.services.common.repository;

import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.common.repository.DocumentException;
import java.util.Map;
import org.dom4j.Document;

/**
 *
 * DocumentHandler provides document processing methods. It is an interface
 * between Nuxeo repository client and CollectionSpace service resource. It provides
 * methods to setup request via repository client and handle its response.
 *
 * Typical call sequence is:
 * Create handler and repository client
 * Call XXX operation on the repository client and pass the handler
 * repository client calls prepare on the handler
 * The repository client then calls handle on the handler
 *
 */
public interface DocumentHandler<T, TL> {

    public enum Action {

        CREATE, GET, GET_ALL, UPDATE, DELETE
    }

    /**
     * prepare is called by the Nuxeo client to prepare required parameters to set
     * before invoking repository operation. this is mainly useful for create and 
     * update kind of actions
     * @param action
     * @throws Exception
     */
    public void prepare(Action action) throws Exception;

    /**
     * handle is called by the Nuxeo client to hand over the document processing on create
     * function to the CollectionSpace service
     * @param action 
     * @param doc wrapped Nuxeo doc
     * @throws Exception
     */
    public void handle(Action action, DocumentWrapper docWrap) throws Exception;

        /**
     * handleCreate processes create operation response
     * @param wrapDoc
     * @throws Exception
     */
    public void handleCreate(DocumentWrapper wrapDoc) throws Exception;

    /**
     * handleUpdate processes update operation response
     * @param wrapDoc
     * @throws Exception
     */
    public void handleUpdate(DocumentWrapper wrapDoc) throws Exception;

    /**
     * handleGet processes get operation response
     * @param wrapDoc
     * @throws Exception
     */
    public void handleGet(DocumentWrapper wrapDoc) throws Exception;

    /**
     * handleGetAll processes index operation response
     * @param wrapDoc
     * @throws Exception
     */
    public void handleGetAll(DocumentWrapper wrapDoc) throws Exception;
    
    /**
     * extractCommonObject extracts common part of a CS document from given Nuxeo document.
     * @param docWrap nuxeo document
     * @return common part of CS object
     * @throws Exception
     */
    public T extractCommonObject(DocumentWrapper docWrap) throws Exception;

    /**
     * fillCommonObject sets common part of CS object into given Nuxeo document
     * @param obj input object
     * @param docWrap target Nuxeo document
     * @throws Exception
     */
    public void fillCommonObject(T obj, DocumentWrapper docWrap) throws Exception;

        /**
     * extractCommonObject extracts common part of a CS document from given Nuxeo document.
     * @param docWrap nuxeo document
     * @return common part of CS object
     * @throws Exception
     */
    public TL extractCommonObjectList(DocumentWrapper docWrap) throws Exception;

    /**
     * fillCommonObject sets common part of CS object into given Nuxeo document
     * @param obj input object
     * @param docWrap target Nuxeo document
     * @throws Exception
     */
    public void fillCommonObjectList(TL obj, DocumentWrapper docWrap) throws Exception;

    /**
     * getCommonObject provides the common part of a CS document.
     * @return common part of CS document
     */
    public T getCommonObject();

    /**
     * setCommonObject sets common part of CS document as input for operation on
     * Nuxeo repository
     * @param obj input object
     */
    public void setCommonObject(T obj);

    /**
     * getCommonObjectList provides the default list object of a CS document.
     * @return default list of CS document
     */
    public TL getCommonObjectList();

    /**
     * setCommonObjectList sets default list object for CS document as input for operation on
     * Nuxeo repository
     * @param default list of CS document
     */
    public void setCommonObjectList(TL obj);

    /**
     * getDocument get org.dom4j.Document from given DocumentModel
     * @param Nuxeo document wrapper
     * @return
     * @throws DocumentException
     */
    public Document getDocument(DocumentWrapper docWrap) throws DocumentException;
    
    /**
     * Gets the document type.
     * 
     * @return the document type
     */
    public String getDocumentType();

    /**
     * getProperties
     * @return
     */
    public Map<String, Object> getProperties();

    /**
     * setProperties provides means to the CollectionSpace service resource to
     * set up parameters before invoking create request via the Nuxeo client.
     * @param properties
     */
    public void setProperties(Map<String, Object> properties);
}
