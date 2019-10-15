/**	
 * QueryManagerNuxeoImpl.java
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
package org.collectionspace.services.common.query.nuxeo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import org.collectionspace.services.jaxb.InvocableJAXBSchema;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.common.invocable.InvocableUtils;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryManagerNuxeoImpl implements IQueryManager {

	private static String ECM_FULLTEXT_LIKE = "ecm:fulltext"
			+ SEARCH_TERM_SEPARATOR + IQueryManager.SEARCH_LIKE;
	private static String SEARCH_LIKE_FORM = null;

	private final Logger logger = LoggerFactory
			.getLogger(QueryManagerNuxeoImpl.class);

	// Consider that letters, letter-markers, numbers, '_' and apostrophe are
	// words
	private static Pattern nonWordChars = Pattern
			.compile("[^\\p{L}\\p{M}\\p{N}_']");
	private static Pattern kwdTokenizer = Pattern.compile("(?:(['\"])(.*?)(?<!\\\\)(?>\\\\\\\\)*\\1|([^ ]+))");
	private static Pattern unescapedDblQuotes = Pattern.compile("(?<!\\\\)\"");
	private static Pattern unescapedSingleQuote = Pattern.compile("(?<!\\\\)'");
	//private static Pattern kwdSearchProblemChars = Pattern.compile("[\\:\\(\\)\\*\\%]");
	// HACK to work around Nuxeo regression that tokenizes on '.'. 
	private static Pattern kwdSearchProblemChars = Pattern.compile("[\\:\\(\\)\\*\\%\\.]");
	private static Pattern kwdSearchHyphen = Pattern.compile(" - ");
	private static Pattern advSearchSqlWildcard = Pattern.compile(".*?[I]*LIKE\\s*\\\"\\%\\\".*?");
	// Base Nuxeo document type for all CollectionSpace documents/resources
	public static String COLLECTIONSPACE_DOCUMENT_TYPE = "CollectionSpaceDocument";
	public static final String NUXEO_DOCUMENT_TYPE = "Document";


	private static String getLikeForm(String dataSourceName, String repositoryName, String cspaceInstanceId) {
		if (SEARCH_LIKE_FORM == null) {
			try {
				DatabaseProductType type = JDBCTools.getDatabaseProductType(dataSourceName, repositoryName, cspaceInstanceId);
				if (type == DatabaseProductType.MYSQL) {
					SEARCH_LIKE_FORM = IQueryManager.SEARCH_LIKE;
				} else if (type == DatabaseProductType.POSTGRESQL) {
					SEARCH_LIKE_FORM = IQueryManager.SEARCH_ILIKE;
				}
			} catch (Exception e) {
				SEARCH_LIKE_FORM = IQueryManager.SEARCH_LIKE;
			}
		}
		return SEARCH_LIKE_FORM;
	}

	@Override
	public String getDatasourceName() {
		return JDBCTools.NUXEO_DATASOURCE_NAME;
	}
	
	// TODO: This is currently just an example fixed query. This should
	// eventually be
	// removed or replaced with a more generic method.
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.collectionspace.services.common.query.IQueryManager#execQuery(java
	 * .lang.String)
	 */
	@Override
	@Deprecated
	public void execQuery(String queryString) {
		// Intentionally left blank
	}

	@Override
	public String createWhereClauseFromAdvancedSearch(String advancedSearch) {
		String result = null;
		//
		// Process search term.  FIXME: REM - Do we need to perform any string filtering here?
		//
		if (advancedSearch != null && !advancedSearch.isEmpty()) {
                        // Filtering of advanced searches on a single '%' char, per CSPACE-5828
                    	Matcher regexMatcher = advSearchSqlWildcard.matcher(advancedSearch.trim());
                        if (regexMatcher.matches()) {
                            return "";
                        }
			StringBuffer advancedSearchWhereClause = new StringBuffer(
					advancedSearch);
			result = advancedSearchWhereClause.toString();
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.collectionspace.services.common.query.IQueryManager#
	 * createWhereClauseFromKeywords(java.lang.String)
	 */
	// TODO handle keywords containing escaped punctuation chars, then we need
	// to qualify the
	// search by matching on the fulltext.simpletext field.
	// TODO handle keywords containing unescaped double quotes by matching the
	// phrase
	// against the fulltext.simpletext field.
	// Both these require using JDBC, since we cannot get to the fulltext table
	// in NXQL
	@Override
	public String createWhereClauseFromKeywords(String keywords) {
		String result = null;
		StringBuffer fullTextWhereClause = new StringBuffer();
		// Split on unescaped double quotes to handle phrases
		Matcher regexMatcher = kwdTokenizer.matcher(keywords.trim());
		boolean addNOT = false;
		boolean newWordSet = true;
		while (regexMatcher.find()) {
			String phrase = regexMatcher.group();
			// Not needed - already trimmed by split: 
			// String trimmed = phrase.trim();
			// Ignore empty strings from match, or goofy input
			if (phrase.isEmpty())
				continue;
			// Note we let OR through as is
			if("AND".equalsIgnoreCase(phrase)) {
				continue;	// AND is default
			} else if("NOT".equalsIgnoreCase(phrase)) {
				addNOT = true;
				continue;
			}
			// Next comment block of questionable value...
			
			// ignore the special chars except single quote here - can't hurt
			// TODO this should become a special function that strips things the
			// fulltext will ignore, including non-word chars and too-short
			// words,
			// and escaping single quotes. Can return a boolean for anything
			// stripped,
			// which triggers the back-up search. We can think about whether
			// stripping
			// short words not in a quoted phrase should trigger the backup.
			String escapedAndTrimmed = unescapedSingleQuote.matcher(phrase).replaceAll("\\\\'");
			// If there are non-word chars in the phrase, we need to match the
			// phrase exactly against the fulltext table for this object
			// if(nonWordChars.matcher(trimmed).matches()) {
			// }
			// Replace problem chars with spaces. Patches CSPACE-4147,
			// CSPACE-4106
			escapedAndTrimmed = kwdSearchProblemChars.matcher(escapedAndTrimmed).replaceAll(" ").trim();
			escapedAndTrimmed = kwdSearchHyphen.matcher(escapedAndTrimmed).replaceAll(" ").trim();
			if(escapedAndTrimmed.isEmpty()) {
				if (logger.isDebugEnabled() == true) {
					logger.debug("Phrase reduced to empty after replacements: " + phrase);
				}
				continue;
			}

			if (fullTextWhereClause.length()==0) {
				fullTextWhereClause.append(SEARCH_GROUP_OPEN);
			}
			if (newWordSet) {
				fullTextWhereClause.append(ECM_FULLTEXT_LIKE + "'");
				newWordSet = false;
			} else {
				fullTextWhereClause.append(SEARCH_TERM_SEPARATOR);
			}
			if(addNOT) {
				fullTextWhereClause.append("-");	// Negate the next term
				addNOT = false;
			}
			fullTextWhereClause.append(escapedAndTrimmed);
			
			if (logger.isTraceEnabled() == true) {
				logger.trace("Current built whereClause is: "
						+ fullTextWhereClause.toString());
			}
		}
		if (fullTextWhereClause.length()==0) {
			if (logger.isDebugEnabled() == true) {
				logger.debug("No usable keywords specified in string:[" + keywords + "]");
			}
		} else {
			fullTextWhereClause.append("'" + SEARCH_GROUP_CLOSE);
		}

		result = fullTextWhereClause.toString();
		if (logger.isDebugEnabled()) {
			logger.debug("Final built WHERE clause is: " + result);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.collectionspace.services.common.query.IQueryManager#
	 * createWhereClauseFromKeywords(java.lang.String)
	 */
	// TODO handle keywords containing escaped punctuation chars, then we need
	// to qualify the
	// search by matching on the fulltext.simpletext field.
	// TODO handle keywords containing unescaped double quotes by matching the
	// phrase
	// against the fulltext.simpletext field.
	// Both these require using JDBC, since we cannot get to the fulltext table
	// in NXQL
	@Override
	public String createWhereClauseForPartialMatch(String dataSourceName,
			String repositoryName,
			String cspaceInstanceId,
			String field,
			boolean startingWildcard,
			String partialTerm) {
		String trimmed = (partialTerm == null) ? "" : partialTerm.trim();
		if (trimmed.isEmpty()) {
			throw new RuntimeException("No partialTerm specified.");
		}
		if(trimmed.charAt(0) == '*') {
			if(trimmed.length() == 1) { // only a star is not enough
				throw new RuntimeException("No partialTerm specified.");
			}
			trimmed = trimmed.substring(1);
			startingWildcard = true;		// force a starting wildcard match
		}
		if (field == null || field.isEmpty()) {
			throw new RuntimeException("No match field specified.");
		}
			
		StringBuilder ptClause = new StringBuilder(trimmed.length()+field.length()+20);
		ptClause.append(field);
		ptClause.append(getLikeForm(dataSourceName, repositoryName, cspaceInstanceId));
		ptClause.append(startingWildcard?"'%":"'");
		ptClause.append(unescapedSingleQuote.matcher(trimmed).replaceAll("\\\\'"));
		ptClause.append("%'");
		return ptClause.toString();
	}

	/**
	 * Creates a filtering where clause from docType, for invocables.
	 * 
	 * @param docType
	 *            the docType
	 * 
	 * @return the string
	 */
	@Override
	public String createWhereClauseForInvocableByDocType(String schema,
			String docType) {
		String trimmed = (docType == null) ? "" : docType.trim();
		if (trimmed.isEmpty()) {
			throw new RuntimeException("No docType specified.");
		}
		if (schema == null || schema.isEmpty()) {
			throw new RuntimeException("No match schema specified.");
		}
		String wClause = schema + ":" + InvocableJAXBSchema.FOR_DOC_TYPES
				+ " = '" + trimmed + "'";
		return wClause;
	}

	/**
	 * Creates a filtering where clause from invocation mode, for invocables.
	 *
	 * @param mode
	 *            the mode
	 *
	 * @return the string
	 */
	@Override
	public String createWhereClauseForInvocableByMode(String schema, String mode) {
		return createWhereClauseForInvocableByMode(schema, Arrays.asList(mode));
	}

	@Override
	public String createWhereClauseForInvocableByMode(String schema, List<String> modes) {
		if (schema == null || schema.isEmpty()) {
			throw new RuntimeException("No match schema specified.");
		}

		if (modes == null || modes.isEmpty()) {
			throw new RuntimeException("No mode specified.");
		}

		List<String> whereClauses = new ArrayList<String>();

		for (String mode : modes) {
			String propName = InvocableUtils.getPropertyNameForInvocationMode(schema, mode.trim());

			if (propName != null && !propName.isEmpty()) {
				whereClauses.add(propName + " != 0");
			}
		}

		if (whereClauses.size() > 1) {
			return ("(" + StringUtils.join(whereClauses, " OR ") + ")");
		}

		if (whereClauses.size() > 0) {
			return whereClauses.get(0);
		}

		return "";
	}

	/**
	 * @param input
	 * @return true if there were any chars filtered, that will require a backup
	 *         qualifying search on the actual text.
	 */
	private boolean filterForFullText(String input) {
		boolean fFilteredChars = false;

		return fFilteredChars;
	}
	
	/**
	 * Creates a query to filter a qualified (string) field according to a list of string values. 
	 * @param qualifiedField The schema-qualified field to filter on
	 * @param filterTerms the list of one or more strings to filter on
	 * @param fExclude If true, will require qualifiedField NOT match the filters strings.
	 * 					If false, will require qualifiedField does match one of the filters strings.
	 * @return queryString
	 */
	@Override
	public String createWhereClauseToFilterFromStringList(String qualifiedField, String[] filterTerms, boolean fExclude) {
    	// Start with the qualified termStatus field
    	StringBuilder filterClause = new StringBuilder(qualifiedField);
    	if (filterTerms.length == 1) {
    		filterClause.append(fExclude?" <> '":" = '");
    		filterClause.append(filterTerms[0]);
    		filterClause.append('\'');  
    	} else {
    		filterClause.append(fExclude?" NOT IN (":" IN (");
    		for(int i=0; i<filterTerms.length; i++) {
    			if(i>0) {
    				filterClause.append(',');
    			}
    			filterClause.append('\'');  
    			filterClause.append(filterTerms[i]);
    			filterClause.append('\'');  
    		}
    		filterClause.append(')');  
    	}
    	return filterClause.toString();
	}

	@Override
	public String createWhereClauseFromCsid(String csid) {
		String trimmed = (csid == null) ? "" : csid.trim();
		if (trimmed.isEmpty()) {
			throw new RuntimeException("No CSID specified.");
		}

		return NuxeoUtils.getByNameWhereClause(csid);
	}

}
