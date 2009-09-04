package org.collectionspace.services.common.query.nuxeo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.client.NuxeoClient;

import org.collectionspace.services.nuxeo.client.java.NuxeoConnector;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClient;
import org.collectionspace.services.common.query.IQueryManager;

public class QueryManagerNuxeoImpl implements IQueryManager {
	
	private final Logger logger = LoggerFactory
			.getLogger(RepositoryJavaClient.class);
	
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

}
