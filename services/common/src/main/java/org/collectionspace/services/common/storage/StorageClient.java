/**
 *  This entity is a part of the source code and related artifacts
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
package org.collectionspace.services.common.storage;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;

/**
 *
 * @author
 */
public interface StorageClient {

    /**
     * create entity in the persistence store
     * @param ctx service context under which this method is invoked
     * @param handler should be used by the caller to provide and transform the entity
     * @return id in persistence store of the newly created entity
     * @throws BadRequestException data input is bad
     * @throws DocumentException
     */
    String create(ServiceContext ctx, DocumentHandler handler) throws BadRequestException, DocumentException;

    /**
     * delete a entity from the persistence store
     * @param ctx service context under which this method is invoked
     * @param id of the entity
     * @throws DocumentNotFoundException if entity not found
     * @throws DocumentException
     */
    void delete(ServiceContext ctx, String id) throws DocumentNotFoundException, DocumentException;

    /**
     * get entity from the persistence store
     * @param ctx service context under which this method is invoked
     * @param id of the entity to retrieve
     * @param handler should be used by the caller to provide and transform the entity
     * @throws DocumentNotFoundException if entity not found
     * @throws DocumentException
     */
    void get(ServiceContext ctx, String id, DocumentHandler handler) throws DocumentNotFoundException, DocumentException;

    /**
     * getAll get all entitys for an entity service from the persistence store
     * @param ctx service context under which this method is invoked
     * @param handler should be used by the caller to provide and transform the entity
     * @throws DocumentNotFoundException if workspace not found
     * @throws DocumentException
     */
    void getAll(ServiceContext ctx, DocumentHandler handler) throws DocumentNotFoundException, DocumentException;

    /**
     * getFiltered get all entitys for an entity service from the persistence store,
     * given filter parameters specified by the handler.
     * @param ctx service context under which this method is invoked
     * @param handler should be used by the caller to provide and transform the entity
     * @throws DocumentNotFoundException if workspace not found
     * @throws DocumentException
     */
    void getFiltered(ServiceContext ctx, DocumentHandler handler) throws DocumentNotFoundException, DocumentException;

    /**
     * update given entity in the persistence store
     * @param ctx service context under which this method is invoked
     * @param id of the entity
     * @param handler should be used by the caller to provide and transform the entity
     * @throws BadRequestException data input is bad
     * @throws DocumentNotFoundException if entity not found
     * @throws DocumentException
     */
    void update(ServiceContext ctx, String id, DocumentHandler handler) throws BadRequestException, DocumentNotFoundException, DocumentException;

}
