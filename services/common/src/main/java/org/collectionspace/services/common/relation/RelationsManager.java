/**	
 * RelationsManager.java
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
package org.collectionspace.services.common.relation;

import java.io.IOException;
import java.util.List;

import org.collectionspace.services.common.repository.DocumentException;
import org.collectionspace.services.nuxeo.relation.RelationUtilsNuxeoImpl;
import org.collectionspace.services.relation.Relation;
import org.collectionspace.services.common.relation.RelationUtils;

import org.dom4j.Document;

/**
 * The Class RelationsManager.
 * 
 * This class loosely uses the RDF terms subject, predicate, object to describe
 * relationships between entity objects.  For example, if a CollectionObject entity named
 * CO#1 is related to a Intake entity named IN#1 then the corresponding
 * RDF-like term would be: (Subject) CO#1 (Predicate) has-intake (Object) IN#1.
 * 
 * Many of the methods below, refer to RDF-like terms such as "Subject" and "Object" and
 * "Predicate."
 *  
 */
public class RelationsManager {

	/** The relation utils. */
	static private RelationUtils relationUtils = new RelationUtilsNuxeoImpl();

	/**
	 * Gets the ALL relationships in the system.
	 * 
	 * @param repoSession
	 *            the repo session
	 * 
	 * @return the relationships
	 * 
	 * @throws DocumentException
	 *             the document exception
	 */
	static public List<Relation> getRelationships(Object repoSession)
			throws DocumentException {
		return relationUtils.getRelationships(repoSession);
	}
		
	/**
	 * Gets the relationships.  Null values act as wild card and match everything.
	 * 
	 * @param repoSession the repo session
	 * @param subjectCsid the subject csid
	 * @param predicate the predicate
	 * @param objectCsid the object csid
	 * 
	 * @return the relationships
	 * 
	 * @throws DocumentException the document exception
	 */
	static public List<Relation> getRelationships(Object repoSession, String subjectCsid, String predicate, String objectCsid)
			throws DocumentException {
		return relationUtils.getRelationships(repoSession, subjectCsid, predicate, objectCsid);
	}
	
	/**
	 * Gets the relationships for the entity corresponding to the CSID=csid.
	 * The csid refers to either the subject OR the object
	 * 
	 * @param repoSession
	 *            the repo session
	 * @param csid
	 *            the csid
	 * 
	 * @return the relationships
	 * 
	 * @throws DocumentException
	 *             the document exception
	 */
	static public List<Relation> getRelationships(Object repoSession,
			String csid) throws DocumentException {
		return relationUtils.getRelationships(repoSession, csid);
	}
		
	/**
	 * Creates the relationship.
	 * 
	 * @param repoSession the repo session
	 * @param subjectCsid the subject csid
	 * @param predicate the predicate
	 * @param objectCsid the object csid
	 * 
	 * @return the relation
	 * 
	 * @throws DocumentException the document exception
	 */
	static public Relation createRelationship(Object repoSession,
			String subjectCsid,
			String predicate,
			String objectCsid) throws DocumentException {
		return relationUtils.createRelationship(repoSession, subjectCsid,
				predicate, objectCsid);
	}

}
