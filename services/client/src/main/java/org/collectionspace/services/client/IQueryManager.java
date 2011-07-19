/**	
 * IQueryManager.java
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
package org.collectionspace.services.client;

public interface IQueryManager {
	
	final static String SEARCH_GROUP_OPEN = "(";
	final static String SEARCH_GROUP_CLOSE = ")";
	final static String SEARCH_TERM_SEPARATOR = " ";
	final static String SEARCH_LIKE = " LIKE ";
	final static String SEARCH_ILIKE = " ILIKE ";
    final static String SEARCH_TYPE_KEYWORDS = "keywords";
    final static String SEARCH_TYPE_KEYWORDS_KW = "kw";
    final static String SEARCH_TYPE_PARTIALTERM = "pt";
    final static String SEARCH_TYPE_DOCTYPE = "doctype";
    final static String SEARCH_TYPE_INVCOATION_MODE = "mode";
    final static String SEARCH_TYPE_INVOCATION = "inv";
	final static String SEARCH_QUALIFIER_AND = SEARCH_TERM_SEPARATOR + "AND" + SEARCH_TERM_SEPARATOR;
	final static String SEARCH_QUALIFIER_OR = SEARCH_TERM_SEPARATOR + "OR" + SEARCH_TERM_SEPARATOR;

	public void execQuery(String queryString);
	
	/**
	 * Creates the where clause from keywords.
	 * 
	 * @param keywords the keywords
	 * 
	 * @return the string
	 */
	public String createWhereClauseFromKeywords(String keywords);

	/**
	 * Creates the where clause for partial term match.
	 * 
	 * @param field the qualified field to match on
	 * @param partialTerm the term to match against
	 * 
	 * @return the string
	 */
	public String createWhereClauseForPartialMatch(String field, String partialTerm);

	/**
	 * Creates a filtering where clause from docType, for invocables.
	 * 
	 * @param schema the schema name for this invocable type
	 * @param docType the docType
	 * 
	 * @return the string
	 */
	public String createWhereClauseForInvocableByDocType(String schema, String docType);
	
	/**
	 * Creates a filtering where clause from invocation mode, for invocables.
	 * 
	 * @param schema the schema name for this invocable type
	 * @param mode the mode
	 * 
	 * @return the string
	 */
	public String createWhereClauseForInvocableByMode(String schema, String mode);
	
}
