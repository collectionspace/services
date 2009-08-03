/**
 * 
 */
package org.collectionspace.services;

/**
 * @author Richard Millet
 *
 */
public interface RelationJAXBSchema {
	
	// The Nuxeo root element name for the relation entity.
	/** The Constant REL_ROOT_ELEM_NAME. */
	final static String REL_ROOT_ELEM_NAME = "relationtype";
	// Need to fix conflict between the Nuxeo XSD and the JAX-B XSD for the "relation" entity
	
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
	
	/** The Constant ENUM_RELATIONSHIP_TYPE_ASSOC. */
	final static String ENUM_RELATIONSHIP_TYPE_ASSOC = "association";
	
	/** The Constant ENUM_RELATIONSHIP_TYPE_CONTAINS. */
	final static String ENUM_RELATIONSHIP_TYPE_CONTAINS = "contains";
}


