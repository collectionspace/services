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
    final static String SEARCH_TYPE_KEYWORDS_AS = "as";
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
	
	public String createWhereClauseFromAdvancedSearch(String advancedSearch);

	final static boolean FILTER_EXCLUDE = true;
	final static boolean FILTER_INCLUDE = false;
	
	/**
	 * Creates a query to filter a qualified (string) field according to a list of string values. 
	 * @param qualifiedField The schema-qualified field to filter on
	 * @param filterTerms the list of one or more strings to filter on
	 * @param fExclude If true, will require qualifiedField NOT match the filters strings.
	 * 					If false, will require qualifiedField does match one of the filters strings.
	 * @return queryString
	 */
	public String createWhereClauseToFilterFromStringList(String qualifiedField, String[] filterTerms, boolean fExclude);
	
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
