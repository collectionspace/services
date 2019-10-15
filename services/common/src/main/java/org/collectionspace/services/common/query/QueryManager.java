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

import java.util.List;

import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.config.TenantBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.query.nuxeo.QueryManagerNuxeoImpl;
import org.collectionspace.services.config.tenant.TenantBindingType;

public class QueryManager {
	static private final IQueryManager queryManager = new QueryManagerNuxeoImpl();
	
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
	
	static public String createWhereClauseFromAdvancedSearch(String keywords) {
		return queryManager.createWhereClauseFromAdvancedSearch(keywords);
	}
	
	static public String createWhereClauseFromCsid(String csid) {
		return queryManager.createWhereClauseFromCsid(csid);
	}	
	
	/**
	 * Creates the where clause for partial term match.
	 * 
	 * @param field the qualified field to match on
	 * @param partialTerm the term to match against
	 * 
	 * @return the string
	 */
	static public String createWhereClauseForPartialMatch(ServiceContext ctx,
			String field,
			String partialTerm) throws Exception {
		String cspaceInstanceId = ServiceMain.getInstance().getCspaceInstanceId();
		String repositoryName = ctx.getRepositoryName();
        // Otherwise, generate that list and cache it for re-use.
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        TenantBindingType tenantBinding = tReader.getTenantBinding(ctx.getTenantId());
        String ptStartingWildcardValue = TenantBindingUtils.getPropertyValue(tenantBinding,
        		IQueryManager.TENANT_USES_STARTING_WILDCARD_FOR_PARTIAL_TERM);
        boolean ptStartingWildcard = (ptStartingWildcardValue==null) 
        			|| Boolean.parseBoolean(ptStartingWildcardValue);

		return queryManager.createWhereClauseForPartialMatch(queryManager.getDatasourceName(),
				repositoryName, cspaceInstanceId, field, ptStartingWildcard, partialTerm);
	}
	
	/**
	 * Creates a query to filter a qualified (string) field according to a list of string values. 
	 * @param qualifiedField The schema-qualified field to filter on
	 * @param filterTerms the list of one or more strings to filter on
	 * @param fExclude If true, will require qualifiedField NOT match the filters strings.
	 * 					If false, will require qualifiedField does match one of the filters strings.
	 * @return queryString
	 */
	static public String createWhereClauseToFilterFromStringList(String qualifiedField, String[] filterTerms, boolean fExclude) {
		return queryManager.createWhereClauseToFilterFromStringList(qualifiedField, filterTerms, fExclude);
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

	static public String createWhereClauseForInvocableByMode(String schema, List<String> modes) {
		return queryManager.createWhereClauseForInvocableByMode(schema, modes);
	}
}
