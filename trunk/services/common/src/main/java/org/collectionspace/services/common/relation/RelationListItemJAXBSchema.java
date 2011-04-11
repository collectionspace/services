/**	
 * RelationListItemJAXBSchema.java
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

/**
 * The Interface RelationListItemJAXBSchema.
 */
public interface RelationListItemJAXBSchema {
	
	/** The Constant REL_ROOT_ELEM_NAME. */
	final static String REL_ROOT_ELEM_NAME = "relation-list";
	
	/** The Constant CSID. */
	final static String CSID = "csid";
	
	final static String SUBJECT_CSID = "subjectCsid";
	final static String RELATIONSHIP_TYPE = "relationshipType";
	final static String OBJECT_CSID = "objectCsid";
	
	/** The Constant RELATIONSHIP_TYPE_DISPLAYNAME. */
	final static String RELATIONSHIP_TYPE_DISPLAYNAME = "predicateDisplayName";

	/** The Constant URI. */
	final static String URI = "url";
}
