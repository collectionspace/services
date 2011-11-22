/**	
 * CollectionSpacePerformanceTest.java
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
package org.collectionspace.services.PerformanceTests.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.TitleGroup;
import org.collectionspace.services.collectionobject.TitleGroupList;
import org.collectionspace.services.intake.IntakesCommon;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationshipType;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

/**
 * The Class CollectionSpacePerformanceTests.
 */
public abstract class CollectionSpacePerformanceTest {

	protected final static String OBJECT_NUMBER = "objectNumber_";
	protected final static String OBJECT_TITLE = "objectTitle_";
	
	/*
	 * Package scoped methods.
	 */

	/**
	 * Fill collection object.
	 * 
	 * @param co the co
	 * @param identifier the identifier
	 */
	void fillCollectionObject(CollectionobjectsCommon co, String identifier) {
		fillCollectionObject(co, OBJECT_NUMBER + identifier, OBJECT_TITLE + identifier);
	}

	/**
	 * Fill collection object.
	 * 
	 * @param co the co
	 * @param objectNumber the object number
	 * @param title the object title
	 */
	void fillCollectionObject(CollectionobjectsCommon co, String objectNumber,
			String title) {
		co.setObjectNumber(objectNumber);
                TitleGroupList titleGroupList = new TitleGroupList();
                List<TitleGroup> titleGroups = titleGroupList.getTitleGroup();
                TitleGroup titleGroup = new TitleGroup();
                titleGroup.setTitle(title);
                titleGroups.add(titleGroup);
                co.setTitleGroupList(titleGroupList);
	}

	/**
	 * Fill intake.
	 * 
	 * @param theIntake the the intake
	 * @param identifier the identifier
	 */
	void fillIntake(IntakesCommon theIntake, String identifier) {
		fillIntake(theIntake, "entryNumber-" + identifier, "entryDate-"
				+ identifier);
	}

	/**
	 * Fill intake.
	 * 
	 * @param theIntake the the intake
	 * @param entryNumber the entry number
	 * @param entryDate the entry date
	 */
	void fillIntake(IntakesCommon theIntake, String entryNumber, String entryDate) {
		theIntake.setEntryNumber(entryNumber);
		theIntake.setEntryDate(entryDate);
	}

    /**
     * Fill relation.
     * 
     * @param relation the relation
     * @param subjectCsid the document id1
     * @param subjectDocumentType the document type1
     * @param objectCsid the document id2
     * @param objectDocumentType the document type2
     * @param rt the rt
     */
    void fillRelation(RelationsCommon relation, String subjectCsid, String subjectDocumentType,
    		String objectCsid, String objectDocumentType, RelationshipType rt)
    {
        relation.setSubjectCsid(subjectCsid);
        relation.setSubjectDocumentType(subjectDocumentType);
        relation.setSubjectCsid(objectCsid);
        relation.setObjectDocumentType(objectDocumentType);
        
        relation.setRelationshipType(rt.toString());
    }
	
	/**
	 * Creates the identifier.
	 * 
	 * @return the string
	 */
	String createIdentifier() {
		long identifier = System.currentTimeMillis();
		return Long.toString(identifier);
	}

	/**
	 * Extract id.
	 * 
	 * @param res the res
	 * 
	 * @return the string
	 */
	String extractId(ClientResponse<Response> res) {
		String result = null;
		
		MultivaluedMap mvm = res.getMetadata();
		String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
		verbose("extractId:uri=" + uri);
		String[] segments = uri.split("/");
		result = segments[segments.length - 1];
		verbose("id=" + result);
		
		return result;
	}

	/**
	 * Extract part.
	 * 
	 * @param input
	 *            the input
	 * @param label
	 *            the label
	 * @param clazz
	 *            the clazz
	 * 
	 * @return the object
	 * 
	 * @throws Exception
	 *             the exception
	 */
	static Object extractPart(MultipartInput input, String label, Class clazz) {
		Object obj = null;
		
		try {
			for (InputPart part : input.getParts()) {
				String partLabel = part.getHeaders().getFirst("label");
				if (label.equalsIgnoreCase(partLabel)) {
					String partStr = part.getBodyAsString();
					obj = part.getBody(clazz, null);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return obj;
	}
	
	/**
	 * Verbose.
	 * 
	 * @param msg the msg
	 */
	void verbose(String msg) {
//		System.out.println(msg);
	}

	/**
	 * Verbose.
	 * 
	 * @param msg the msg
	 * @param o the o
	 * @param clazz the clazz
	 */
	void verbose(String msg, Object o, Class clazz) {
		try {
			verbose(msg);
			JAXBContext jc = JAXBContext.newInstance(clazz);
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(o, System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verbose map.
	 * 
	 * @param map the map
	 */
	void verboseMap(MultivaluedMap map) {
		for (Object entry : map.entrySet()) {
			MultivaluedMap.Entry mentry = (MultivaluedMap.Entry) entry;
			verbose("  name=" + mentry.getKey() + " value=" + mentry.getValue());
		}
	}

        boolean isEnabled() {
            return Boolean.getBoolean("cspace.perf");
        }

}
