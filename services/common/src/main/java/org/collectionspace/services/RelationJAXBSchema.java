/**	
 * RelationJAXBSchema.java
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

/**
 * The Interface RelationJAXBSchema.
 */
public interface RelationJAXBSchema {
	
	// The Nuxeo root element name for the relation entity.
	/** The Constant REL_ROOT_ELEM_NAME. */
	final static String REL_ROOT_ELEM_NAME = "relationtype";
	// Need to fix conflict between the Nuxeo XSD and the JAX-B XSD for the "relation" entity
	
	/** The Constant CSID. */
	final static String CSID = "csid";
	
	/** The Constant DOCUMENT_ID_1. */
	final static String DOCUMENT_ID_1 = "documentId1";
	
	/** The Constant DOCUMENT_TYPE_1. */
	final static String DOCUMENT_TYPE_1 = "documentType1";
	
	/** The Constant DOCUMENT_ID_2. */
	final static String DOCUMENT_ID_2 = "documentId2";
	
	/** The Constant DOCUMENT_TYPE_2. */
	final static String DOCUMENT_TYPE_2 = "documentType2";
	
	/** The Constant RELATIONSHIP_TYPE. */
	final static String RELATIONSHIP_TYPE = "relationshipType";
	
	/*
	 * Relation Types/Predicates Enumerations
	 */
	
	/** The Constant ENUM_RELATIONSHIP_TYPE_ASSOC. */
	final static String ENUM_REL_TYPE_ASSOC = "association";
	
	/** The Constant ENUM_RELATIONSHIP_TYPE_CONTAINS. */
	final static String ENUM_REL_TYPE_CONTAINS = "contains";
	
	/** The Constant ENUM_RELATIONSHIP_TYPE_COLLECTIONOBJECT_INTAKE. */
	final static String ENUM_REL_TYPE_COLLECTIONOBJECT_INTAKE = "collectionobject-intake";
	
}


