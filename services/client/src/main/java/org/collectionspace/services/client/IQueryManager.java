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

	String SEARCH_COMBINE_QUERY_PARAM = "combine";
	String SEARCH_COMBINE_AND = "and";
	String SEARCH_COMBINE_OR = "or";
	String SEARCH_GROUP_OPEN = "(";
	String SEARCH_GROUP_CLOSE = ")";
	String SEARCH_TERM_SEPARATOR = " ";
	String SEARCH_LIKE = " LIKE ";
	String SEARCH_ILIKE = " ILIKE ";
	String SEARCH_TYPE_KEYWORDS = "keywords";
	String SEARCH_TYPE_KEYWORDS_KW = "kw";
	String SEARCH_TYPE_KEYWORDS_AS = "as";
	String SEARCH_TYPE_PARTIALTERM = "pt";
	String SEARCH_TYPE_DOCTYPE = "doctype";
	String SEARCH_TYPE_FILENAME = "filename";
	String SEARCH_TYPE_CLASS_NAME = "classname";
	String SEARCH_TYPE_INVOCATION_MODE = "mode";
	String SEARCH_TYPE_INVOCATION = "inv";
	String SEARCH_QUALIFIER_AND = SEARCH_TERM_SEPARATOR + "AND" + SEARCH_TERM_SEPARATOR;
	String SEARCH_QUALIFIER_OR = SEARCH_TERM_SEPARATOR + "OR" + SEARCH_TERM_SEPARATOR;
	String DEFAULT_SELECT_CLAUSE = "SELECT * FROM ";
	String CSID_QUERY_PARAM = "csid";
	String TAG_QUERY_PARAM = "servicetag";


	//
	// Nuxeo pseudo-values (and filters) for special document properties.
	//
    String NUXEO_UUID = "ecm:uuid";
	String NUXEO_IS_PROXY = "ecm:isProxy";
	String NUXEO_IS_PROXY_FILTER = NUXEO_IS_PROXY + " = 0";
	String NUXEO_IS_VERSION = "ecm:isCheckedInVersion";
	String NUXEO_IS_VERSION_FILTER = NUXEO_IS_VERSION + " = 0";
	// In the CMIS context, the prefix is nuxeo, not ecm
    String NUXEO_CMIS_IS_VERSION = "nuxeo:isVersion";
	String NUXEO_CMIS_IS_VERSION_FILTER = NUXEO_CMIS_IS_VERSION + " = false";

	//
	// Query params for CMIS queries on the relationship (Relation) table.
	//
    String SEARCH_RELATED_TO_CSID_AS_SUBJECT = "rtSbj";
	String SEARCH_RELATED_TO_CSID_AS_OBJECT = "rtObj";
	String SEARCH_RELATED_PREDICATE = "rtPredicate";

	String SEARCH_RELATED_TO_CSID_AS_EITHER = "rtSbjOrObj";
	String SEARCH_RELATED_MATCH_OBJ_DOCTYPES = "rtObjDocTypes";
	String SELECT_DOC_TYPE_FIELD = "selectDocType";

	String MARK_RELATED_TO_CSID_AS_SUBJECT = "mkRtSbj";
	String MARK_RELATED_TO_CSID_AS_EITHER = "mkRtSbjOrObj";

	//
	// Generic CMIS property mapping constants
	//
    String CMIS_OBJECT_ID = "cmis:objectId";
	String CMIS_NUXEO_PATHSEGMENT = "nuxeo:pathSegment";
	//
	// Nuxeo related CMIS property mapping constants
    String CMIS_NUXEO_ID = CMIS_OBJECT_ID;
	String CMIS_NUXEO_NAME = CMIS_NUXEO_PATHSEGMENT;
	String CMIS_NUXEO_TITLE = "dc:title";
	String CMIS_CS_UPDATED_AT = CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA + ":" +
                                      CollectionSpaceClient.COLLECTIONSPACE_CORE_UPDATED_AT;

	// CollectionSpace CMIS property mapping constants
    String CMIS_TARGET_PREFIX = "DOC";
	String CMIS_CORESCHEMA_PREFIX = "CORE";
	// Relations CMIS property mapping constants
    String CMIS_RELATIONS_PREFIX = "REL";

	String CMIS_JOIN_NUXEO_IS_VERSION_FILTER =
			IQueryManager.CMIS_TARGET_PREFIX + "." + IQueryManager.NUXEO_CMIS_IS_VERSION_FILTER;
	String CMIS_JOIN_TENANT_ID_FILTER =
			IQueryManager.CMIS_RELATIONS_PREFIX + "." + CollectionSpaceClient.CORE_TENANTID;

	String CMIS_TARGET_NUXEO_ID = CMIS_TARGET_PREFIX + "." + CMIS_NUXEO_ID;
	String CMIS_TARGET_CSID = CMIS_TARGET_PREFIX + "." + CMIS_NUXEO_NAME;
	String CMIS_TARGET_TITLE = CMIS_TARGET_PREFIX + "." + CMIS_NUXEO_TITLE;
	String CMIS_TARGET_NAME = CMIS_TARGET_PREFIX + "." + CMIS_NUXEO_NAME;
	String CMIS_TARGET_UPDATED_AT = CMIS_TARGET_PREFIX + "." + CMIS_CS_UPDATED_AT;

	String TENANT_USES_STARTING_WILDCARD_FOR_PARTIAL_TERM = "ptStartingWildcard";
        String MAX_LIST_ITEMS_RETURNED_LIMIT_ON_JDBC_QUERIES = "maxListItemsReturnedLimitOnJdbcQueries";
        String JDBC_QUERIES_ARE_TENANT_ID_RESTRICTED = "jdbcQueriesAreTenantIdRestricted";

	String getDatasourceName();

	/**
	 * Creates the where clause from keywords.
	 *
	 * @param keywords the keywords
	 * @param clean if punctuation should be removed from the keywords
	 *
	 * @return the string
	 */
    String createWhereClauseFromKeywords(String keywords, boolean clean);

	String createWhereClauseFromAdvancedSearch(String advancedSearch);

	boolean FILTER_EXCLUDE = true;
	boolean FILTER_INCLUDE = false;

	/**
	 * Creates a query to filter a qualified (string) field according to a list of string values.
	 * @param qualifiedField The schema-qualified field to filter on
	 * @param filterTerms the list of one or more strings to filter on
	 * @param fExclude If true, will require qualifiedField NOT match the filters strings.
	 * 					If false, will require qualifiedField does match one of the filters strings.
	 * @return queryString
	 */
    String createWhereClauseToFilterFromStringList(String qualifiedField, String[] filterTerms, boolean fExclude);

	/**
	 * Creates the where clause for partial term match.
	 *
	 * @param field the qualified field to match on
	 * @param partialTerm the term to match against
	 *
	 * @return the string
	 */
    String createWhereClauseForPartialMatch(String dataSourceName,
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
    String createWhereClauseForInvocableByDocType(String schema, String docType);


	/**
	 * Creates a filtering where clause from filename, for invocables.
	 *
	 * @param schema  the schema name for this invocable
	 * @param docType the filename
	 * @return        the where clause
	 */
    String createWhereClauseForInvocableByFilename(String schema, String filename);

	/**
	 * Creates a filtering where clause from class name, for invocables.
	 *
	 * @param schema  the schema name for this invocable
	 * @param docType the class name
	 * @return        the where clause
	 */
    String createWhereClauseForInvocableByClassName(String schema, String className);

	/**
	 * Creates a filtering where clause from invocation mode, for invocables.
	 *
	 * @param schema the schema name for this invocable type
	 * @param mode the mode
	 *
	 * @return the string
	 */
    String createWhereClauseForInvocableByMode(String schema, String mode);

	String createWhereClauseForInvocableByMode(String schema, List<String> modes);

	/*
	 *
	 */
    String createWhereClauseFromCsid(String csid);

}
