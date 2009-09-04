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
 *
 * $LastChangedBy$
 * $LastChangedRevision$
 * $LastChangedDate$
 */
 
package org.collectionspace.services.id;

/**
 * IDGenerator, interface for an IDGenerator, which returns identifiers (IDs).
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public interface IDGenerator {

    /**
     * Returns a new identifier (ID).
     */
	public String newID();

    /**
     * Returns a new identifier (ID), based on a supplied identifier.
     */
	public String newID(String id);

    /**
     * Returns the current identifier (ID).
     */
	public String getCurrentID();

}
