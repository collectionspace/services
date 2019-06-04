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
 * Copyright (c) 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.relation;

import org.collectionspace.services.client.IRelationsManager;

/**
 * The Interface RelationJAXBSchema.
 */
public interface RelationJAXBSchema {

	// The Nuxeo root element name for the relation entity.
	/** The Constant REL_ROOT_ELEM_NAME. */
	final static String REL_ROOT_ELEM_NAME = "relationtype";
	// Need to fix conflict between the Nuxeo XSD and the JAX-B XSD for the
	// "relation" entity

	/** The Constant CSID. */
	final static String CSID = "csid";

	/** The Constant RELATIONSHIP_TYPE. */
	final static String RELATIONSHIP_TYPE = "relationshipType";

	/** The Constant RELATIONSHIP_TYPE_DISPLAYNAME. */
	final static String RELATIONSHIP_TYPE_DISPLAYNAME = "predicateDisplayName";
	final static String RELATIONSHIP_ACTIVE = "active";
	final static String RELATIONSHIP_META_TYPE = "relationshipMetaType";

	final static String SUBJECT_URI = "subjectUri";
	final static String SUBJECT_CSID = IRelationsManager.SUBJECT;
	final static String SUBJECT_REFNAME = IRelationsManager.SUBJECT_REFNAME;
	final static String SUBJECT_DOCTYPE = "subjectDocumentType";

	final static String OBJECT_URI = "objectUri";
	final static String OBJECT_CSID = IRelationsManager.OBJECT;
	final static String OBJECT_REFNAME = IRelationsManager.OBJECT_REFNAME;
	final static String OBJECT_DOCTYPE = "objectDocumentType";

}
