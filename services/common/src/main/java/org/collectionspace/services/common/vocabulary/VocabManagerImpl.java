package org.collectionspace.services.common.vocabulary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.client.NuxeoClient;

import org.collectionspace.services.nuxeo.client.java.NuxeoConnector;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
//import org.collectionspace.services.common.query.IQueryManager;

public class VocabManagerImpl implements IVocabManager {
	
	private final Logger logger = LoggerFactory
			.getLogger(VocabManagerImpl.class);
	
	public void exampleMethod(String someParam) {
	}
}
