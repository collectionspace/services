package org.collectionspace.services.nuxeo;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.RelationJAXBSchema;
import org.collectionspace.services.common.RelationUtils;
import org.collectionspace.services.relation.Relation;
import org.collectionspace.services.relation.RelationshipType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.common.repository.DocumentException;

//import org.dom4j.DocumentException;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.DocumentPart;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationUtilsNuxeoImpl implements RelationUtils {
	
	static public String CS_RELATION_SERVICE_NAME = "relations";
	
	final public static String REL_NUXEO_DOCTYPE = "Relation";
	final public static String REL_NUXEO_SCHEMA_NAME = "relation";
	final public static String REL_NUXEO_SCHEMA_ROOT_ELEMENT = "relationtype";
	final public static String REL_NUXEO_DC_TITLE = "CollectionSpace-Relation";
	
	private static Logger logger = LoggerFactory
	.getLogger(RelationUtilsNuxeoImpl.class);	


	static public void fillRelationFromDocModel(Relation relation, DocumentModel relDocModel)
			throws ClientException {
		String xpathRoot = "/" + REL_NUXEO_SCHEMA_ROOT_ELEMENT + "/";
		Object valueObject = null;

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
				+ RelationJAXBSchema.DOCUMENT_TYPE_1);
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
	
	static public void printDocumentModel(DocumentModel documentModel) {
		System.out.println(documentModel);
	}
	
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

	static public List<Relation> getRelationships(RepositoryInstance repoSession)
			throws DocumentException, IOException, ClientException {
		List<Relation> result = new ArrayList<Relation>();

		DocumentModel relationWorkspace = NuxeoUtils.getWorkspaceModel(
				repoSession, CS_RELATION_SERVICE_NAME);
		DocumentModelList children = repoSession.getChildren(relationWorkspace
				.getRef());
		Relation relation = null;
		for (DocumentModel child : children) {
			relation = new Relation();
			fillRelationFromDocModel(relation, child);
			result.add(relation);
		}

		return result;
	}

}
