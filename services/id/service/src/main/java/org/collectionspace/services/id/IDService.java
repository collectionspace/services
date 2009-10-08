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

// May at some point instead use
// org.jboss.resteasy.spi.NotFoundException
import java.util.List;
import org.collectionspace.services.common.repository.DocumentNotFoundException;
import org.collectionspace.services.common.repository.BadRequestException;


/**
 * IDService
 *
 * Interface for the ID Service.
 *
 * $LastChangedBy$
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
    public String createID(String csid) throws DocumentNotFoundException,
      BadRequestException, IllegalArgumentException, IllegalStateException;
    
    // Returns the last-generated ID associated with the specified ID generator.
    public String readLastID(String csid)
        throws IllegalArgumentException, IllegalStateException;

    // Read a list of objects (aka read multiple)
    
    // ----------------------------------------
    // ID Generators
    // ----------------------------------------
    
    // Create
    
    // Adds a new ID generator.
    public void createIDGenerator(String csid, String serializedIDGenerator)
        throws IllegalArgumentException, IllegalStateException;
    
    // Read single object
    public String readIDGenerator(String csid) throws DocumentNotFoundException,
        IllegalArgumentException, IllegalStateException;
    
    // Read a list of objects (aka read multiple)
    // and return in a full list format.
    public List<String> readIDGeneratorsList() throws IllegalStateException;

    // Read a list of objects (aka read multiple)
    // and return in a summary list format.
    public List<String> readIDGeneratorsSummaryList() throws BadRequestException,
       IllegalStateException;

    // Update (may need to check for changes in the ID generator structure)
    public void updateIDGenerator(String csid, String serializedIDGenerator)
        throws DocumentNotFoundException, BadRequestException,
        IllegalArgumentException, IllegalStateException;
    
    // Delete (possibly not permitted - deactivate instead?)

}
