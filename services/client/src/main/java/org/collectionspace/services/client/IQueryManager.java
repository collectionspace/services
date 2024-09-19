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

import java.util.List;

public interface IQueryManager {

	final static String SEARCH_COMBINE_QUERY_PARAM = "combine";
	final static String SEARCH_COMBINE_AND = "and";
	final static String SEARCH_COMBINE_OR = "or";
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
	final static String SEARCH_TYPE_FILENAME = "filename";
	final static String SEARCH_TYPE_CLASS_NAME = "classname";
	final static String SEARCH_TYPE_INVOCATION_MODE = "mode";
	final static String SEARCH_TYPE_INVOCATION = "inv";
	final static String SEARCH_QUALIFIER_AND = SEARCH_TERM_SEPARATOR + "AND" + SEARCH_TERM_SEPARATOR;
	final static String SEARCH_QUALIFIER_OR = SEARCH_TERM_SEPARATOR + "OR" + SEARCH_TERM_SEPARATOR;
	final static String DEFAULT_SELECT_CLAUSE = "SELECT * FROM ";
	final static String CSID_QUERY_PARAM = "csid";
	final static String TAG_QUERY_PARAM = "servicetag";


	//
	// Nuxeo pseudo-values (and filters) for special document properties.
	//
	final static String NUXEO_UUID = "ecm:uuid";
	final static String NUXEO_IS_PROXY = "ecm:isProxy";
	final static String NUXEO_IS_PROXY_FILTER = NUXEO_IS_PROXY + " = 0";
	final static String NUXEO_IS_VERSION = "ecm:isCheckedInVersion";
	final static String NUXEO_IS_VERSION_FILTER = NUXEO_IS_VERSION + " = 0";
	// In the CMIS context, the prefix is nuxeo, not ecm
	final static String NUXEO_CMIS_IS_VERSION = "nuxeo:isVersion";
	final static String NUXEO_CMIS_IS_VERSION_FILTER = NUXEO_CMIS_IS_VERSION + " = false";

	//
	// Query params for CMIS queries on the relationship (Relation) table.
	//
	final static String SEARCH_RELATED_TO_CSID_AS_SUBJECT = "rtSbj";
	final static String SEARCH_RELATED_TO_CSID_AS_OBJECT = "rtObj";
	final static String SEARCH_RELATED_PREDICATE = "rtPredicate";

	final static String SEARCH_RELATED_TO_CSID_AS_EITHER = "rtSbjOrObj";
	final static String SEARCH_RELATED_MATCH_OBJ_DOCTYPES = "rtObjDocTypes";
	final static String SELECT_DOC_TYPE_FIELD = "selectDocType";

	final static String MARK_RELATED_TO_CSID_AS_SUBJECT = "mkRtSbj";
	final static String MARK_RELATED_TO_CSID_AS_EITHER = "mkRtSbjOrObj";

	//
	// Generic CMIS property mapping constants
	//
	final static String CMIS_OBJECT_ID = "cmis:objectId";
	final static String CMIS_NUXEO_PATHSEGMENT = "nuxeo:pathSegment";
	//
	// Nuxeo related CMIS property mapping constants
	final static String CMIS_NUXEO_ID = CMIS_OBJECT_ID;
	final static String CMIS_NUXEO_NAME = CMIS_NUXEO_PATHSEGMENT;
	final static String CMIS_NUXEO_TITLE = "dc:title";
	final static String CMIS_CS_UPDATED_AT = CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA + ":" +
			CollectionSpaceClient.COLLECTIONSPACE_CORE_UPDATED_AT;

	// CollectionSpace CMIS property mapping constants
	final static String CMIS_TARGET_PREFIX = "DOC";
	final static String CMIS_CORESCHEMA_PREFIX = "CORE";
	// Relations CMIS property mapping constants
	final static String CMIS_RELATIONS_PREFIX = "REL";

	final static String CMIS_JOIN_NUXEO_IS_VERSION_FILTER =
			IQueryManager.CMIS_TARGET_PREFIX + "." + IQueryManager.NUXEO_CMIS_IS_VERSION_FILTER;
	final static String CMIS_JOIN_TENANT_ID_FILTER =
			IQueryManager.CMIS_RELATIONS_PREFIX + "." + CollectionSpaceClient.CORE_TENANTID;

	final static String CMIS_TARGET_NUXEO_ID = CMIS_TARGET_PREFIX + "." + CMIS_NUXEO_ID;
	final static String CMIS_TARGET_CSID = CMIS_TARGET_PREFIX + "." + CMIS_NUXEO_NAME;
	final static String CMIS_TARGET_TITLE = CMIS_TARGET_PREFIX + "." + CMIS_NUXEO_TITLE;
	final static String CMIS_TARGET_NAME = CMIS_TARGET_PREFIX + "." + CMIS_NUXEO_NAME;
	final static String CMIS_TARGET_UPDATED_AT = CMIS_TARGET_PREFIX + "." + CMIS_CS_UPDATED_AT;

	final static String TENANT_USES_STARTING_WILDCARD_FOR_PARTIAL_TERM = "ptStartingWildcard";
        final static String MAX_LIST_ITEMS_RETURNED_LIMIT_ON_JDBC_QUERIES = "maxListItemsReturnedLimitOnJdbcQueries";
        final static String JDBC_QUERIES_ARE_TENANT_ID_RESTRICTED = "jdbcQueriesAreTenantIdRestricted";

	public void execQuery(String queryString);

	public String getDatasourceName();

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
	public String createWhereClauseForPartialMatch(String dataSourceName,
			String repositoryName,
			String cspaceInstanceId,
			String field,
			boolean startingWildcard,
			String partialTerm);

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
	 * Creates a filtering where clause from filename, for invocables.
	 *
	 * @param schema  the schema name for this invocable
	 * @param docType the filename
	 * @return        the where clause
	 */
	public String createWhereClauseForInvocableByFilename(String schema, String filename);

	/**
	 * Creates a filtering where clause from class name, for invocables.
	 *
	 * @param schema  the schema name for this invocable
	 * @param docType the class name
	 * @return        the where clause
	 */
	public String createWhereClauseForInvocableByClassName(String schema, String className);

	/**
	 * Creates a filtering where clause from invocation mode, for invocables.
	 *
	 * @param schema the schema name for this invocable type
	 * @param mode the mode
	 *
	 * @return the string
	 */
	public String createWhereClauseForInvocableByMode(String schema, String mode);

	public String createWhereClauseForInvocableByMode(String schema, List<String> modes);

	/*
	 *
	 */
	public String createWhereClauseFromCsid(String csid);

}
