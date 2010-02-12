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

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.client.NuxeoClient;

import org.collectionspace.services.nuxeo.client.java.NuxeoConnector;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.query.IQueryManager;

public class QueryManagerNuxeoImpl implements IQueryManager {
	
	private final Logger logger = LoggerFactory
			.getLogger(RepositoryJavaClientImpl.class);
	
	//TODO: This is currently just an example fixed query.  This should eventually be
	// removed or replaced with a more generic method.
	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.query.IQueryManager#execQuery(java.lang.String)
	 */
	public void execQuery(String queryString) {
		NuxeoClient client = null;
		try {
			client = NuxeoConnector.getInstance().getClient();
			RepositoryInstance repoSession = client.openRepository();
			
			DocumentModelList docModelList = repoSession.query("SELECT * FROM Relation WHERE relation:relationtype.documentId1='updated-Subject-1'");
//			DocumentModelList docModelList = repoSession.query("SELECT * FROM Relation");
//			DocumentModelList docModelList = repoSession.query("SELECT * FROM CollectionObject WHERE collectionobject:objectNumber='objectNumber-1251305545865'");
			for (DocumentModel docModel : docModelList) {
				System.out.println("--------------------------------------------");
				System.out.println(docModel.getPathAsString());
				System.out.println(docModel.getName());
				System.out.println(docModel.getPropertyValue("dc:title"));
//				System.out.println("documentId1=" + docModel.getProperty("relation", "relationtype/documentId1").toString());
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.query.IQueryManager#createWhereClauseFromKeywords(java.lang.String)
	 */
	public String createWhereClauseFromKeywords(String keywords) {
		String result = null;
		StringBuffer whereClause = new StringBuffer();
		StringTokenizer stringTokenizer = new StringTokenizer(keywords);
		while (stringTokenizer.hasMoreElements() == true) {
			whereClause.append(ECM_FULLTEXT_LIKE + "'" +
					stringTokenizer.nextToken() + "'");
			if (stringTokenizer.hasMoreElements() == true) {
				whereClause.append(SEARCH_TERM_SEPARATOR +
						SEARCH_QUALIFIER_OR +
						SEARCH_TERM_SEPARATOR);
			}
			if (logger.isDebugEnabled() == true) {
				logger.debug("Current built whereClause is: " + whereClause.toString());
			}
		}            	
		
		result = whereClause.toString();
	    if (logger.isDebugEnabled()) {
	    	logger.debug("Final built WHERE clause is: " + result);
	    }
	    
	    return result;
	}
}
