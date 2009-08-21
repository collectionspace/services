/**	
 * RelationServiceNuxeoImpl.java
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
package org.collectionspace.services.relation.nuxeo;

//import java.io.ByteArrayInputStream;
import java.io.IOException;
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
import java.util.List;
//import java.util.Map;
//import java.util.Iterator;

import org.collectionspace.services.common.repository.DocumentException;
//import org.collectionspace.services.nuxeo.NuxeoRESTClient;
import org.collectionspace.services.nuxeo.CollectionSpaceServiceNuxeoImpl;
import org.collectionspace.services.common.relation.nuxeo.RelationUtilsNuxeoImpl;
import org.collectionspace.services.relation.Relation;
import org.collectionspace.services.relation.RelationService;
//import org.collectionspace.services.relation.RelationList;
//import org.collectionspace.services.relation.RelationshipType;
//import org.collectionspace.services.RelationJAXBSchema;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.common.relation.RelationsManager;
//import org.collectionspace.services.common.ServiceMain;

import org.dom4j.Document;
//import org.dom4j.Element;
//import org.dom4j.dom.DOMDocument;
//import org.dom4j.DocumentException;
//import org.dom4j.io.SAXReader;
//import org.restlet.resource.Representation;

import org.nuxeo.common.utils.IdUtils;
//import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.api.DocumentModel;
//import org.nuxeo.ecm.core.api.DocumentModelList;
//import org.nuxeo.ecm.core.api.model.DocumentPart;
import org.nuxeo.ecm.core.api.DocumentRef;



/**
 * The Class RelationServiceNuxeoImpl.
 */
public class RelationServiceNuxeoImpl extends
		CollectionSpaceServiceNuxeoImpl implements RelationService {

	// replace WORKSPACE_UID for resource workspace
	//	static String CS_RELATION_WORKSPACE_UID = "55f99358-5dbe-4462-8000-c5c3c2063919";

	/* (non-Javadoc)
	 * @see org.collectionspace.services.relation.RelationService#deleteRelation(java.lang.String)
	 */
	public Document deleteRelation(String csid) throws DocumentException {
		Document result = null;

		RepositoryInstance repoSession = null;
		try {
			repoSession = getRepositorySession();
			result = deleteDocument(repoSession, csid);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception ", e);
			}
			throw new DocumentException(e);
		} finally {
			if (repoSession != null) {
				releaseRepositorySession(repoSession);
			}
		}

		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.collectionspace.services.relation.RelationService#getRelation(java.lang.String)
	 */
	public Document getRelation(String csid)
			throws DocumentException, IOException {
		Document result = null;
		RepositoryInstance repoSession = null;
		
		try {
			repoSession = getRepositorySession();
			result = NuxeoUtils.getDocument(repoSession, csid);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception ", e);
			}
			throw new DocumentException(e);
		} finally {
			if (repoSession != null) {
				releaseRepositorySession(repoSession);
			}
		}
		
		// Dump out the contents of the result
		if (logger.isDebugEnabled() == true) {
			System.out.println(result.asXML());
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.relation.RelationService#getRelationList()
	 */
	public Document getRelationList()
			throws DocumentException, IOException {
		Document result = null;
		RepositoryInstance repoSession = null;

		try {
			repoSession = getRepositorySession();
			List<Relation> relationList = RelationsManager.getRelationships(repoSession);
			
			result = RelationUtilsNuxeoImpl.getDocument(relationList);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception ", e);
			}
			throw new DocumentException(e);
		} finally {
			if (repoSession != null) {
				releaseRepositorySession(repoSession);
			}
		}
		
		if (logger.isDebugEnabled() == true) {
			System.out.println(result.asXML());
		}

		return result;
	}
		
	/* (non-Javadoc)
	 * @see org.collectionspace.services.RelationService#getRelationList(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Document getRelationList(String subjectCsid,
			String predicate, String objectCsid)
				throws DocumentException, IOException {
		Document result = null;
		RepositoryInstance repoSession = null;

		try {
			repoSession = getRepositorySession();
			List<Relation> relationList = RelationsManager.getRelationships(repoSession,
					subjectCsid, predicate, objectCsid);
			
			result = RelationUtilsNuxeoImpl.getDocument(relationList);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception ", e);
			}
			throw new DocumentException(e);
		} finally {
			if (repoSession != null) {
				releaseRepositorySession(repoSession);
			}
		}
		
		if (logger.isDebugEnabled() == true) {
			System.out.println(result.asXML());
		}

		return result;
		}

	// Create a new relation document
	/* (non-Javadoc)
	 * @see org.collectionspace.services.relation.RelationService#postRelation(org.collectionspace.services.relation.Relation)
	 */
	public Document postRelation(Relation co) throws DocumentException,
			IOException {
		Document result = null;
		RepositoryInstance repoSession = null;
		
		try {
			repoSession = getRepositorySession();
            DocumentModel resultDocModel = RelationUtilsNuxeoImpl.createRelationship(repoSession, co);
            repoSession.save();
            result = NuxeoUtils.getDocument(repoSession, resultDocModel);
		} catch (Exception e) {
			if (logger.isDebugEnabled() == true) {
				logger.debug("Caught exception ", e);
			}
			throw new DocumentException(e);
		} finally {
			if (repoSession != null) {
				releaseRepositorySession(repoSession);
			}
		}

		// Dump out the contents of the result
		if (logger.isDebugEnabled() == true) {
			System.out.println(result.asXML());
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.collectionspace.services.relation.RelationService#putRelation(java.lang.String, org.collectionspace.services.relation.Relation)
	 */
	public Document putRelation(String csid, Relation theUpdate)
			throws DocumentException, IOException {
		
		Document result = null;
        RepositoryInstance repoSession = null;
        try{
            repoSession = getRepositorySession();
            DocumentRef documentRef = new IdRef(csid);
            DocumentModel documentModel = repoSession.getDocument(documentRef);
            RelationUtilsNuxeoImpl.fillDocModelFromRelation(theUpdate, documentModel);
            repoSession.saveDocument(documentModel);
            repoSession.save();
            result = NuxeoUtils.getDocument(repoSession, documentModel);
        } catch(Exception e){
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception ", e);
			}
			throw new DocumentException(e);
        } finally{
            if(repoSession != null){
                releaseRepositorySession(repoSession);
            }
        }
		
		return result;
	}	

}
