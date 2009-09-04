/**	
 * RelationUtilsNuxeoImpl.java
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
package org.collectionspace.services.common.relation.nuxeo;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.common.relation.RelationJAXBSchema;
import org.collectionspace.services.common.relation.RelationListItemJAXBSchema;
import org.collectionspace.services.common.relation.IRelationsManager;

import org.collectionspace.services.relation.Relation;
import org.collectionspace.services.relation.RelationList;
import org.collectionspace.services.relation.RelationshipType;
import org.collectionspace.services.relation.RelationList.RelationListItem;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.common.repository.DocumentException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMDocumentFactory;
//import org.dom4j.DocumentException;

import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.DocumentPart;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.w3c.dom.DOMException;
//import org.w3c.dom.Element;

/**
 * The Class RelationUtilsNuxeoImpl.
 */
public class RelationsManagerNuxeoImpl implements IRelationsManager {
	
	/** The C s_ relatio n_ servic e_ name. */
	static public String CS_RELATION_SERVICE_NAME = "relations";
	
	/** The C s_ empt y_ string. */
	static public String CS_EMPTY_STRING = "";
	
	/** The Constant REL_NUXEO_DOCTYPE. */
	final public static String REL_NUXEO_DOCTYPE = "Relation";
	
	/** The Constant REL_NUXEO_SCHEMA_NAME. */
	final public static String REL_NUXEO_SCHEMA_NAME = "relation";
	
	/** The Constant REL_NUXEO_SCHEMA_ROOT_ELEMENT. */
	final public static String REL_NUXEO_SCHEMA_ROOT_ELEMENT = "relationtype";
	
	/** The Constant REL_NUXEO_DC_TITLE. */
	final public static String REL_NUXEO_DC_TITLE = "CollectionSpace-Relation";
	
	/** The logger. */
	private static Logger logger = LoggerFactory
	.getLogger(RelationsManagerNuxeoImpl.class);	


	/**
	 * Fill relation from doc model.
	 * 
	 * @param relation the relation
	 * @param relDocModel the rel doc model
	 * 
	 * @throws ClientException the client exception
	 */
	static public void fillRelationFromDocModel(Relation relation, DocumentModel relDocModel)
			throws ClientException {
		String xpathRoot = "/" + REL_NUXEO_SCHEMA_ROOT_ELEMENT + "/";
		Object valueObject = null;

		relation.setCsid(relDocModel.getId());

		valueObject = relDocModel.getProperty(REL_NUXEO_SCHEMA_NAME, xpathRoot
				+ RelationJAXBSchema.DOCUMENT_ID_1);
		relation.setDocumentId1((String) valueObject);

		valueObject = relDocModel.getProperty(REL_NUXEO_SCHEMA_NAME, xpathRoot
				+ RelationJAXBSchema.DOCUMENT_TYPE_1);
		relation.setDocumentType1((String) valueObject);

		valueObject = relDocModel.getProperty(REL_NUXEO_SCHEMA_NAME, xpathRoot
				+ RelationJAXBSchema.DOCUMENT_ID_2);
		relation.setDocumentId2((String) valueObject);

		valueObject = relDocModel.getProperty(REL_NUXEO_SCHEMA_NAME, xpathRoot
				+ RelationJAXBSchema.DOCUMENT_TYPE_2);
		relation.setDocumentType2((String) valueObject);

		valueObject = relDocModel.getProperty(REL_NUXEO_SCHEMA_NAME, xpathRoot
				+ RelationJAXBSchema.RELATIONSHIP_TYPE);
		relation.setRelationshipType(RelationshipType
				.fromValue((String) valueObject));

		if (logger.isDebugEnabled() == true) {
			System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
			System.out.println(relation.toString());
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		}
	}

	/**
	 * Fill relation list item from doc model.
	 * 
	 * @param relationListItem the relation list item
	 * @param relDocModel the rel doc model
	 * 
	 * @throws DocumentException the document exception
	 */
	static public void fillRelationListItemFromDocModel(RelationListItem relationListItem,
			DocumentModel relDocModel)
		throws DocumentException {

		try {
			relationListItem.setCsid(
					relDocModel.getId());
			relationListItem.setUri(
					getRelURL(CS_RELATION_SERVICE_NAME, relDocModel.getId()));
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in fillRelationListItemFromDocModel", e);
			}
			throw new DocumentException(e);
		}
	}
	
	/**
	 * Fill doc model list from relation list.
	 * 
	 * @param relationList the relation list
	 * @param relDocModelList the rel doc model list
	 * 
	 * @throws Exception the exception
	 */
	static public void fillDocModelListFromRelationList(RelationList relationList,
			DocumentModelList relDocModelList)
		throws Exception {
	}	
	
	/**
	 * Fill doc model from relation.
	 * 
	 * @param p the p
	 * @param relDocModel the rel doc model
	 * 
	 * @throws Exception the exception
	 */
	static public void fillDocModelFromRelation(Relation p, DocumentModel relDocModel)
			throws Exception {

		// set the DublinCore title (this works)
		relDocModel.setPropertyValue("dublincore:title", "default title");

		// // set value for <documentType1> element
		// try {
		// relDocModel.setProperty("relation", "/relationtype/documentId1",
		// "docId1");
		// } catch (Exception x) {
		// x.printStackTrace();
		// }

		String xpathRoot = "/" + REL_NUXEO_SCHEMA_ROOT_ELEMENT + "/";
		if (p.getDocumentId1() != null) {
			String property = xpathRoot + RelationJAXBSchema.DOCUMENT_ID_1;
			relDocModel.setProperty(REL_NUXEO_SCHEMA_NAME, property, p
					.getDocumentId1());
		}
		if (p.getDocumentType1() != null) {
			String property = xpathRoot + RelationJAXBSchema.DOCUMENT_TYPE_1;
			relDocModel.setProperty(REL_NUXEO_SCHEMA_NAME, property, p
					.getDocumentType1());
		}
		if (p.getDocumentId2() != null) {
			String property = xpathRoot + RelationJAXBSchema.DOCUMENT_ID_2;
			relDocModel.setProperty(REL_NUXEO_SCHEMA_NAME, property, p
					.getDocumentId2());
		}
		if (p.getDocumentType2() != null) {
			String property = xpathRoot + "/"
					+ RelationJAXBSchema.DOCUMENT_TYPE_2;
			relDocModel.setProperty(REL_NUXEO_SCHEMA_NAME, property, p
					.getDocumentType2());
		}

		if (p.getRelationshipType() != null) {
			String property = xpathRoot + RelationJAXBSchema.RELATIONSHIP_TYPE;
			relDocModel.setProperty(REL_NUXEO_SCHEMA_NAME, property, p
					.getRelationshipType().value());
		}
	}
	
	/**
	 * Prints the document model.
	 * 
	 * @param documentModel the document model
	 */
	static public void printDocumentModel(DocumentModel documentModel) {
		System.out.println(documentModel);
	}
	
	/**
	 * Describe document model.
	 * 
	 * @param docModel the doc model
	 * 
	 * @throws Exception the exception
	 */
	static private void describeDocumentModel(DocumentModel docModel) throws Exception {
		String[] schemas = docModel.getDeclaredSchemas();
		for (int i = 0; schemas != null && i < schemas.length; i++) {
			System.out.println("Schema-" + i + "=" + schemas[i]);
		}
		
		DocumentPart[] parts = docModel.getParts();
		Map<String,Serializable> propertyValues = null;
		for (int i = 0; parts != null && i < parts.length; i++) {
			System.out.println("Part-" + i + " name =" + parts[i].getName());
			System.out.println("Part-" + i + " path =" + parts[i].getPath());
			System.out.println("Part-" + i + " schema =" + parts[i].getSchema().getName());
			propertyValues = parts[i].exportValues();
		}

	}

	/**
	 * Creates the relationship.
	 * 
	 * @param nuxeoRepoSession the nuxeo repo session
	 * @param newRelation the new relation
	 * 
	 * @return the document model
	 * 
	 * @throws DocumentException the document exception
	 */
	static public DocumentModel createRelationship(Object nuxeoRepoSession, Relation newRelation)
			throws DocumentException {
		DocumentModel result = null;
		RepositoryInstance repoSession = (RepositoryInstance)nuxeoRepoSession;
		
		try {
			// get the Nuxeo 'Relations' workspace
			DocumentModel workspaceModel = NuxeoUtils.getWorkspaceModel(repoSession,
					CS_RELATION_SERVICE_NAME);
			
	        String docType = REL_NUXEO_DOCTYPE;
	        String id = IdUtils.generateId("New " + docType);
	        
	        //create document model
	        String workspacePath = workspaceModel.getPathAsString();            
	        DocumentModel newRelDocModel = repoSession.createDocumentModel(workspacePath, id, docType);
	
	        newRelation.setCsid(newRelDocModel.getId());
	        fillDocModelFromRelation(newRelation, newRelDocModel);
	        
	        //create document with the new DocumentModel
	        result = repoSession.createDocument(newRelDocModel);
	        repoSession.save();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DocumentException(e);
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.RelationUtils#createRelationship(java.lang.Object, java.lang.String, java.lang.String, java.lang.String)
	 */
	public Relation createRelationship(Object nuxeoRepoSession, String subjectCsid, String predicate,
			String objectCsid) throws DocumentException {
		Relation result = null;
		RepositoryInstance repoSession = (RepositoryInstance)nuxeoRepoSession;
		
        Relation temp = new Relation();
        temp.setDocumentId1(subjectCsid);
        temp.setRelationshipType(null);
        temp.setDocumentId2(objectCsid);
        createRelationship(repoSession, temp);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.RelationUtils#getRelationships(java.lang.Object)
	 */
	public List<Relation> getRelationships(Object nuxeoRepoSession)
			throws DocumentException {
		List<Relation> result = null;
		RepositoryInstance repoSession = (RepositoryInstance)nuxeoRepoSession;

		try {
			DocumentModel relationWorkspace = NuxeoUtils.getWorkspaceModel(
					repoSession, CS_RELATION_SERVICE_NAME);
			DocumentModelList children = repoSession.getChildren(relationWorkspace
					.getRef());
			
			result = new ArrayList<Relation>();
			Relation relation = null;
			for (DocumentModel child : children) {
				relation = new Relation();
				fillRelationFromDocModel(relation, child);
				result.add(relation);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DocumentException(e);
		}

		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.RelationUtils#getRelationships(java.lang.Object, java.lang.String)
	 */
	public List<Relation> getRelationships(Object nuxeoRepoSession, String csid)
			throws DocumentException {
		List<Relation> result = null;
		RepositoryInstance repoSession = (RepositoryInstance)nuxeoRepoSession;
				
		try {
			DocumentModel relationWorkspace = NuxeoUtils.getWorkspaceModel(
					repoSession, CS_RELATION_SERVICE_NAME);
			DocumentModelList children = repoSession.getChildren(relationWorkspace
					.getRef());
			
			result = new ArrayList<Relation>();
			Relation relation = null;
			for (DocumentModel child : children) {
				if ((isSubjectOfRelation(csid, child) == true) ||
						(isObjectOfRelation(csid, child) == true)) {
					relation = new Relation();
					fillRelationFromDocModel(relation, child);
					result.add(relation);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DocumentException(e);
		}
		
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.RelationUtils#getRelationships(java.lang.Object, java.lang.String, java.lang.String, java.lang.String)
	 */
	public List<Relation> getRelationships(Object nuxeoRepoSession,
			String subjectCsid, 
			String predicate, 
			String objectCsid) throws DocumentException {
		List<Relation> result = null;
		RepositoryInstance repoSession = (RepositoryInstance)nuxeoRepoSession;
		
		try {
			DocumentModel relationWorkspace = NuxeoUtils.getWorkspaceModel(
					repoSession, CS_RELATION_SERVICE_NAME);
			DocumentModelList children = repoSession.getChildren(relationWorkspace
					.getRef());
			
			result = new ArrayList<Relation>();
			Relation relation = null;
			for (DocumentModel child : children) {
				if (isQueryMatch(child, subjectCsid, predicate, objectCsid) == true) {
					relation = new Relation();
					fillRelationFromDocModel(relation, child);
					result.add(relation);			}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DocumentException(e);
		}
		
		return result;
	}
		
	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.relation.RelationUtils#getQPropertyName(java.lang.String)
	 */
	public String getQPropertyName(String propertyName) {
		return "/" + REL_NUXEO_SCHEMA_ROOT_ELEMENT + "/" + propertyName;
	}
		
	/**
	 * Checks if is subject of relation.
	 * 
	 * @param csid the csid
	 * @param documentModel the document model
	 * 
	 * @return true, if is subject of relation
	 * 
	 * @throws ClientException the client exception
	 */
	private boolean isSubjectOfRelation(String csid, DocumentModel documentModel)
			throws ClientException {
		boolean result = false;
		String xpathRoot = "/" + REL_NUXEO_SCHEMA_ROOT_ELEMENT + "/";

		Object valueObject = documentModel.getProperty(REL_NUXEO_SCHEMA_NAME, xpathRoot
				+ RelationJAXBSchema.DOCUMENT_ID_1);
		if (valueObject != null && csid != null) {
			String subjectID = (String) valueObject;
			result = subjectID.equals(csid);
		}
		
		return result;
	}

	/**
	 * Checks if is object of relation.
	 * 
	 * @param csid the csid
	 * @param documentModel the document model
	 * 
	 * @return true, if is object of relation
	 * 
	 * @throws ClientException the client exception
	 */
	private boolean isObjectOfRelation(String csid, DocumentModel documentModel)
			throws ClientException {
		boolean result = false;
		String xpathRoot = "/" + REL_NUXEO_SCHEMA_ROOT_ELEMENT + "/";

		Object valueObject = documentModel.getProperty(REL_NUXEO_SCHEMA_NAME,
				xpathRoot + RelationJAXBSchema.DOCUMENT_ID_2);
		if (valueObject != null  && csid != null) {
			String subjectID = (String) valueObject;
			result = subjectID.equals(csid);
		}

		return result;
	}
	
	/**
	 * Checks if is predicate of relation.
	 * 
	 * @param predicate the predicate
	 * @param documentModel the document model
	 * 
	 * @return true, if is predicate of relation
	 * 
	 * @throws ClientException the client exception
	 */
	private boolean isPredicateOfRelation(String predicate,
			DocumentModel documentModel) throws ClientException {
		boolean result = false;
		String xpathRoot = "/" + REL_NUXEO_SCHEMA_ROOT_ELEMENT + "/";

		Object valueObject = documentModel.getProperty(REL_NUXEO_SCHEMA_NAME,
				xpathRoot + RelationJAXBSchema.RELATIONSHIP_TYPE);
		if (valueObject != null  && predicate != null) {
			String relationType = (String) valueObject;
			result = predicate.equalsIgnoreCase(relationType);
		}

		return result;
	}

	/**
	 * Gets the object from subject.
	 * 
	 * @param csid the csid
	 * @param documentModel the document model
	 * 
	 * @return the object from subject
	 * 
	 * @throws ClientException the client exception
	 */
	private String getObjectFromSubject(String csid, DocumentModel documentModel)
			throws ClientException {
		String result = null;
		String xpathRoot = "/" + REL_NUXEO_SCHEMA_ROOT_ELEMENT + "/";

		Object valueObject = documentModel.getProperty(REL_NUXEO_SCHEMA_NAME,
				xpathRoot + RelationJAXBSchema.DOCUMENT_ID_1);
		if (valueObject != null) {
			String subjectID = (String) valueObject;
			if (subjectID.equals(csid) == true) {
				valueObject = documentModel.getProperty(REL_NUXEO_SCHEMA_NAME,
						xpathRoot + RelationJAXBSchema.DOCUMENT_ID_2);
				if (valueObject != null) {
					result = (String) valueObject;
				}
			}
		}

		return result;
	}
	
	/**
	 * Gets the document.
	 * 
	 * @param relationList the relation list
	 * 
	 * @return the document
	 * 
	 * @throws DocumentException the document exception
	 */
	static public Document getDocument(List<Relation> relationList)
			throws DocumentException {
		DOMDocumentFactory domfactory = new DOMDocumentFactory();
		DOMDocument result = (DOMDocument) domfactory.createDocument();

		try {
			// setup the root element
			DOMElement root = (DOMElement) result
					.createElement(RelationListItemJAXBSchema.REL_ROOT_ELEM_NAME);
			result.setRootElement((org.dom4j.Element) root);

			// populate the document with child elements
			for (Relation child : relationList) {
				DOMElement el = (DOMElement) result.createElement(RelationJAXBSchema.REL_ROOT_ELEM_NAME);
				el.setAttribute(RelationListItemJAXBSchema.CSID, child
						.getCsid());
				el.setAttribute(RelationListItemJAXBSchema.URI, getRelURL(
						CS_RELATION_SERVICE_NAME, child.getCsid()));

				if (logger.isDebugEnabled() == true) {
					System.out.println(el.asXML());
				}
				
				root.appendChild(el);
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception ", e);
			}
			throw new DocumentException(e);
		}

		if (logger.isDebugEnabled() == true) {
			System.out.println(result.asXML());
		}

		return result;
	}
	
	/**
	 * Checks if is query match.
	 * 
	 * @param documentModel the document model
	 * @param subjectCsid the subject csid
	 * @param predicate the predicate
	 * @param objectCsid the object csid
	 * 
	 * @return true, if is query match
	 * 
	 * @throws ClientException the client exception
	 */
	public boolean isQueryMatch(DocumentModel documentModel,
			String subjectCsid,
			String predicate,
			String objectCsid) throws DocumentException {
		boolean result = true;
		
		try {
		block: {
			if (subjectCsid != null) {
				if (isSubjectOfRelation(subjectCsid, documentModel) == false) {
					result = false;
					break block;
				}
			}
			if (predicate != null) {
				if (isPredicateOfRelation(predicate, documentModel) == false) {
					result = false;
					break block;
				}
			}
			if (objectCsid != null) {
				if (isObjectOfRelation(objectCsid, documentModel) == false) {
					result = false;
					break block;
				}
			}
		}
		} catch (ClientException e) {
			if (logger.isDebugEnabled() == true) {
				e.printStackTrace();
			}
			throw new DocumentException(e);
		}
		
		return result;
	}
	
    /**
     * Gets the rel url.
     * 
     * @param repo the repo
     * @param uuid the uuid
     * 
     * @return the rel url
     */
    private static String getRelURL(String repo, String uuid) {
        return '/' + repo + '/' + uuid;
    }	
	
}
