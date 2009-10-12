/**	
 * IRelationsManager.java
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
 * Copyright (c) 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.relation;

import java.util.List;


import org.collectionspace.services.common.repository.DocumentException;
import org.collectionspace.services.relation.RelationsCommon;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * The Interface IRelationsManager.
 */
public interface IRelationsManager {

    /** The Constant SUBJECT. */
    static public final String SUBJECT = "subjectCsid";
    /** The Constant PREDICATE. */
    static public final String PREDICATE = "predicate";
    /** The Constant OBJECT. */
    static public final String OBJECT = "objectCsid";


    /**
     * Gets the relationships for the entity corresponding to the CSID=csid.
     * The csid refers to either the subject *OR* the object.
     *
     * @param nuxeoRepoSession the nuxeo repo session
     * @param csid the csid
     *
     * @return the relationships
     *
     * @throws DocumentException the document exception
     */
    public List<RelationsCommon> getRelationships(Object nuxeoRepoSession, String csid)
            throws DocumentException;

    /**
     * Gets the relationships.
     *
     * @param repoSession the repo session
     * @param subjectCsid the subject csid
     * @param relationType the relation type
     * @param objectCsid the object csid
     *
     * @return the relationships
     *
     * @throws DocumentException the document exception
     */
    public List<RelationsCommon> getRelationships(Object repoSession,
            String subjectCsid,
            String relationType,
            String objectCsid)
            throws DocumentException;

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
    public RelationsCommon createRelationship(Object repoSession, String subjectCsid,
            String predicate, String objectCsid) throws DocumentException;

    /**
     * Gets the q property name.
     *
     * @param propertyName the property name
     *
     * @return the q property name
     */
    public String getQPropertyName(String propertyName);

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
     * @throws DocumentException the document exception
     */
    public boolean isQueryMatch(DocumentModel documentModel,
            String subjectCsid,
            String predicate,
            String objectCsid) throws DocumentException;
}
