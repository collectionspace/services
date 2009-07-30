/**
 * 
 */
package org.collectionspace.services;

/**
 * @author remillet
 *
 */
public interface RelationJAXBSchema {
	
	// The Nuxeo root element name for the relation entity.
	final static String REL_ROOT_ELEM_NAME = "relationtype";
	// Need to fix conflict between the Nuxeo XSD and the JAX-B XSD for the "relation" entity
	
	final static String DOCUMENT_ID_1 = "documentId1";
	final static String DOCUMENT_TYPE_1 = "documentType1";
	final static String DOCUMENT_ID_2 = "documentId2";
	final static String DOCUMENT_TYPE_2 = "documentType2";
	final static String RELATIONSHIP_TYPE = "relationshipType";
	
	final static String ENUM_RELATIONSHIP_TYPE_ASSOC = "association";
	final static String ENUM_RELATIONSHIP_TYPE_CONTAINS = "contains";
}


