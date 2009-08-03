/**	
 * RelationsManager.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common;

/**
 * The Class RelationsManager.
 */
public class RelationsManager {
	
	/** The relation utils. */
	private RelationUtils relationUtils = null;
	
	// FIXME: Add singleton patter here.
	/**
	 * Sets the utils.
	 * 
	 * @param utils the new utils
	 */
	public void setUtils(RelationUtils utils) {
		relationUtils = utils;
	}
	
	/**
	 * Gets the relation utils.
	 * 
	 * @return the relation utils
	 */
	public RelationUtils getRelationUtils() {
		return relationUtils;
	}

}
