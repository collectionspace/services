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
    public String createID(String csid) throws Exception;

    // Returns the last-generated ID associated with the specified ID generator.
    public String readLastID(String csid)
            throws Exception;

    // Read a list of objects (aka read multiple)
    // ----------------------------------------
    // ID Generators
    // ----------------------------------------
    // Create
    // Adds a new ID generator.
    public void createIDGenerator(String csid, String serializedIDGenerator)
            throws Exception;

    // Read single object
    public IDGeneratorInstance readIDGenerator(String csid)
            throws Exception;

    // Read a list of objects (aka read multiple)
    // and return a list (map) of those objects and their identifiers.
    public Map<String, IDGeneratorInstance> readIDGeneratorsList()
            throws Exception;

    // Update
    public void updateIDGenerator(String csid, String serializedIDGenerator)
            throws Exception;

    // Delete (possibly not permitted - deactivate instead?)
    public void deleteIDGenerator(String csid)
            throws Exception;
}
