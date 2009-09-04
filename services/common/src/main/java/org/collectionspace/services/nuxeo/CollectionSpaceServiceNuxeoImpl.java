/**
 * Copyright 2009 University of California at Berkeley
 */
package org.collectionspace.services.nuxeo;

import java.io.IOException;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.repository.DocumentNotFoundException;
import org.collectionspace.services.nuxeo.client.rest.NuxeoRESTClient;
import org.collectionspace.services.nuxeo.client.java.NuxeoConnector;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMDocumentFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.client.NuxeoClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author remillet
 * 
 */
public abstract class CollectionSpaceServiceNuxeoImpl {

	
	protected Logger logger = LoggerFactory
			.getLogger(CollectionSpaceServiceNuxeoImpl.class);

//	public NuxeoRESTClient getClient() {
//		NuxeoRESTClient nxClient = new NuxeoRESTClient(CS_NUXEO_URI);
//
//		nxClient.setAuthType(NuxeoRESTClient.AUTH_TYPE_BASIC);
//		nxClient.setBasicAuthentication("Administrator", "Administrator");
//
//		return nxClient;
//	}

    
    protected RepositoryInstance getRepositorySession() throws Exception {
		// FIXME: is it possible to reuse repository session?
		// Authentication failures happen while trying to reuse the session
		NuxeoClient client = NuxeoConnector.getInstance().getClient();
		RepositoryInstance repoSession = client.openRepository();
		if (logger.isDebugEnabled()) {
			logger.debug("getRepository() repository root: "
					+ repoSession.getRootDocument());
		}
		return repoSession;
	}

    protected void releaseRepositorySession(RepositoryInstance repoSession) {
		try {
			NuxeoClient client = NuxeoConnector.getInstance().getClient();
			// release session
			client.releaseRepository(repoSession);
		} catch (Exception e) {
			logger.error("Could not close the repository session", e);
			// no need to throw this service specific exception
		}
	}
    
}
