/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.id;

import java.util.Map;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;

/**
 * IDService
 *
 * Interface for the ID Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public interface IDService {

    // ----------------------------------------
    // IDs
    // ----------------------------------------
    // Create
    // Read single object
    // Generates and returns a new ID from the specified ID generator.
    public String createID(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid) throws Exception;

    // Returns the last-generated ID associated with the specified ID generator.
    public String readLastID(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid)
            throws Exception;

    // Read a list of objects (aka read multiple)
    // ----------------------------------------
    // ID Generators
    // ----------------------------------------
    // Create
    // Adds a new ID generator.
    public void createIDGenerator(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String csid, String serializedIDGenerator) throws Exception;

    // Read single object
    public IDGeneratorInstance readIDGenerator(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid)
            throws Exception;

    // Read a list of objects (aka read multiple)
    // and return a list (map) of those objects and their identifiers.
    public Map<String, IDGeneratorInstance> readIDGeneratorsList(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx)
            throws Exception;

    // Update
    public void updateIDGenerator(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String csid, 
    		String serializedIDGenerator) throws Exception;

    // Delete (possibly not permitted - deactivate instead?)
    public void deleteIDGenerator(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid)
            throws Exception;
}
