/**	
 * QueryManager.java
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
package org.collectionspace.services.common.query;

import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.common.query.nuxeo.QueryManagerNuxeoImpl;

public class QueryManager {
	static private final IQueryManager queryManager = new QueryManagerNuxeoImpl();
	
	static public void execQuery(String queryString) {
		queryManager.execQuery(queryString);
	}
	
	/**
	 * Creates the where clause from keywords.
	 * 
	 * @param keywords the keywords
	 * 
	 * @return the string
	 */
	static public String createWhereClauseFromKeywords(String keywords) {
		return queryManager.createWhereClauseFromKeywords(keywords);
	}
	
	/**
	 * Creates the where clause for partial term match.
	 * 
	 * @param field the qualified field to match on
	 * @param partialTerm the term to match against
	 * 
	 * @return the string
	 */
	static public String createWhereClauseForPartialMatch(String field, String partialTerm) {
		return queryManager.createWhereClauseForPartialMatch(field, partialTerm);
	}
	
	/**
	 * Creates a filtering where clause from docType, for invocables.
	 * 
	 * @param schema the schema name for this invocable type
	 * @param docType the docType
	 * 
	 * @return the string
	 */
	static public String createWhereClauseForInvocableByDocType(String schema, String docType) {
		return queryManager.createWhereClauseForInvocableByDocType(schema, docType);
	}
	
	/**
	 * Creates a filtering where clause from invocation mode, for invocables.
	 * 
	 * @param schema the schema name for this invocable type
	 * @param mode the mode
	 * 
	 * @return the string
	 */
	static public String createWhereClauseForInvocableByMode(String schema, String mode) {
		return queryManager.createWhereClauseForInvocableByMode(schema, mode);
	}
	
}
