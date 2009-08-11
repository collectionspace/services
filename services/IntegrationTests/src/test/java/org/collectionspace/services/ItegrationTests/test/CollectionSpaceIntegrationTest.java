package org.collectionspace.services.ItegrationTests.test;

import java.util.ArrayList;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.collectionspace.services.collectionobject.CollectionObject;
import org.collectionspace.services.intake.Intake;
import org.collectionspace.services.relation.Relation;
import org.collectionspace.services.relation.RelationshipType;
import org.jboss.resteasy.client.ClientResponse;

public abstract class CollectionSpaceIntegrationTest {

	/*
	 * Package scoped methods.
	 */

	void fillCollectionObject(CollectionObject co, String identifier) {
		fillCollectionObject(co, "objectNumber-" + identifier, "objectName-"
				+ identifier);
	}

	void fillCollectionObject(CollectionObject co, String objectNumber,
			String objectName) {
		co.setObjectNumber(objectNumber);
		co.setObjectName(objectName);
	}

	void fillIntake(Intake theIntake, String identifier) {
		fillIntake(theIntake, "entryNumber-" + identifier, "entryDate-"
				+ identifier);
	}

	void fillIntake(Intake theIntake, String entryNumber, String entryDate) {
		theIntake.setEntryNumber(entryNumber);
		theIntake.setEntryDate(entryDate);
	}

    void fillRelation(Relation relation, String documentId1, String documentType1,
    		String documentId2, String documentType2, RelationshipType rt)
    {
        relation.setDocumentId1(documentId1);
        relation.setDocumentType1(documentType1);
        relation.setDocumentId2(documentId2);
        relation.setDocumentType2(documentType2);
        
        relation.setRelationshipType(rt);
    }
	
	String createIdentifier() {
		long identifier = System.currentTimeMillis();
		return Long.toString(identifier);
	}

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

	void verbose(String msg) {
		System.out.println(msg);
	}

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

	void verboseMap(MultivaluedMap map) {
		for (Object entry : map.entrySet()) {
			MultivaluedMap.Entry mentry = (MultivaluedMap.Entry) entry;
			verbose("  name=" + mentry.getKey() + " value=" + mentry.getValue());
		}
	}

}
