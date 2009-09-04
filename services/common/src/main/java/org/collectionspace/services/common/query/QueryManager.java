package org.collectionspace.services.common.query;

import org.collectionspace.services.common.query.nuxeo.QueryManagerNuxeoImpl;

public class QueryManager {
	static private final IQueryManager queryManager = new QueryManagerNuxeoImpl();
	
	static public void execQuery(String queryString) {
		queryManager.execQuery(queryString);
	}
}
