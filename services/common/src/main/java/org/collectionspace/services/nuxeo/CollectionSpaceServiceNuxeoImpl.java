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

	// replace host if not running on localhost
	// static String CS_NUXEO_HOST = "173.45.234.217";
	static String CS_NUXEO_HOST = "localhost";
	static String CS_NUXEO_URI = "http://" + CS_NUXEO_HOST + ":8080/nuxeo";
	
	protected Logger logger = LoggerFactory
			.getLogger(CollectionSpaceServiceNuxeoImpl.class);

	public NuxeoRESTClient getClient() {
		NuxeoRESTClient nxClient = new NuxeoRESTClient(CS_NUXEO_URI);

		nxClient.setAuthType(NuxeoRESTClient.AUTH_TYPE_BASIC);
		nxClient.setBasicAuthentication("Administrator", "Administrator");

		return nxClient;
	}

// FIXME: Replace this method after integration of the relation code
	protected RepositoryInstance getRepositorySession() throws Exception {
		// FIXME: is it possible to reuse repository session?
		// Authentication failures happen while trying to reuse the session
		NuxeoConnector nuxeoConnector = NuxeoConnector.getInstance();
		return nuxeoConnector.getRepositorySession();
	}
			
	protected Document deleteDocument(RepositoryInstance repoSession, String csid)
			throws DocumentException, IOException {
		Document result = null;

		try {
			repoSession = getRepositorySession();
			DocumentRef relDocumentRef = new IdRef(csid);
			
            try{
                repoSession.removeDocument(relDocumentRef);
            }catch(ClientException ce){
                String msg = "could not find document to delete with id=" + csid;
                logger.error(msg, ce);
                throw new DocumentNotFoundException(msg, ce);
            }
            repoSession.save();			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	protected Document listWorkspaceContent(RepositoryInstance repoSession,
			String workspaceName) {

		DOMDocumentFactory domfactory = new DOMDocumentFactory();
		DOMDocument result = (DOMDocument) domfactory.createDocument();

		try {
			repoSession = getRepositorySession();
			DocumentModel workspaceModel = NuxeoUtils.getWorkspaceModel(repoSession,
					workspaceName);

			Element current = result.createElement("document");
			try {
				current.setAttribute("title", workspaceModel.getTitle());
			} catch (Exception e) {
				e.printStackTrace();
			}
			current.setAttribute("type", workspaceModel.getType());
			current.setAttribute("id", workspaceModel.getId());
			current.setAttribute("name", workspaceModel.getName());
			current.setAttribute("url", getRelURL(workspaceName, workspaceModel.getRef().toString()));
			result.setRootElement((org.dom4j.Element) current);

			if (workspaceModel.isFolder()) {
				// Element childrenElem = result.createElement("children");
				// root.appendChild(childrenElem);

				DocumentModelList children = null;
				try {
					children = repoSession.getChildren(workspaceModel.getRef());
				} catch (ClientException e) {
					e.printStackTrace();
				}

				for (DocumentModel child : children) {
					Element el = result.createElement("document");
					try {
						el.setAttribute("title", child.getTitle());
					} catch (DOMException e) {
						e.printStackTrace();
					} catch (ClientException e) {
						e.printStackTrace();
					}
					el.setAttribute("type", child.getType());
					el.setAttribute("id", child.getId());
					el.setAttribute("name", child.getName());
					el.setAttribute("url", getRelURL(workspaceName, child.getRef()
							.toString()));
					current.appendChild(el);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (logger.isDebugEnabled() == true) {
			System.out.println(result.asXML());
		}

		return result;
	}

	protected void releaseRepositorySession(RepositoryInstance repoSession) {
		try {
			// release session
			NuxeoConnector nuxeoConnector = NuxeoConnector.getInstance();
			nuxeoConnector.releaseRepositorySession(repoSession);
		} catch (Exception e) {
			logger.error("Could not close the repository session", e);
			// no need to throw this service specific exception
		}
	}
	
    private static String getRelURL(String repo, String uuid) {
        return '/' + repo + '/' + uuid;
    }	
}
