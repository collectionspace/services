/**	
 * RelationService.java
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
package org.collectionspace.services;

import java.io.IOException;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import org.collectionspace.services.relation.Relation;

/**
 * @author remillet
 * 
 */
public interface RelationService {

	/** The Constant REL_SCHEMA_NAME. */
	public final static String REL_SCHEMA_NAME = "relation";

	// Create
	/**
	 * Post relation.
	 * 
	 * @param co the co
	 * 
	 * @return the document
	 * 
	 * @throws DocumentException the document exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	Document postRelation(Relation co)
			throws DocumentException, IOException;

	// Read single object
	/**
	 * Gets the relation.
	 * 
	 * @param csid the csid
	 * 
	 * @return the relation
	 * 
	 * @throws DocumentException the document exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	Document getRelation(String csid) throws DocumentException,
			IOException;

	// Read a list of objects
	/**
	 * Gets the relation list.
	 * 
	 * @return the relation list
	 * 
	 * @throws DocumentException the document exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	Document getRelationList() throws DocumentException, IOException;

	// Update
	/**
	 * Put relation.
	 * 
	 * @param csid the csid
	 * @param theUpdate the the update
	 * 
	 * @return the document
	 * 
	 * @throws DocumentException the document exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	Document putRelation(String csid, Relation theUpdate)
			throws DocumentException, IOException;

	// Delete
	/**
	 * Delete relation.
	 * 
	 * @param csid the csid
	 * 
	 * @return the document
	 * 
	 * @throws DocumentException the document exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	Document deleteRelation(String csid) throws DocumentException,
			IOException;
}
