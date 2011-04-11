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

import org.collectionspace.services.common.document.DocumentHandler.Action;

/**
 * MultipartDocumentHandler is a DocumentHandler provides document processing
 * methods for entities using schema extension that are mapped to multipart
 * request and response.
 * @author
 * @param <T>
 * @param <TL>
 * @param <WT>
 * @param <WTL>
 */
public interface MultipartDocumentHandler<T, TL, WT, WTL>
        extends DocumentHandler<T, TL, WT, WTL> {

    /**
     * extractAllParts extracts all parts of a CS object from given document.
     * this is usually called AFTER the get operation is invoked on the repository
     * Called in handle GET/GET_ALL actions.
     * @param docWrap document
     * @throws Exception
     */
    public void extractAllParts(DocumentWrapper<WT> docWrap) throws Exception;

    /**
     * fillAllParts sets parts of CS object into given document
     * this is usually called BEFORE create/update operations are invoked on the
     * repository. Called in handle CREATE/UPDATE actions.
     * @param obj input object
     * @param docWrap target document
     * @param action one of Action.CREATE or Action.UPDATE
     * @throws Exception
     */
    public void fillAllParts(DocumentWrapper<WT> docWrap, Action action) throws Exception;
}