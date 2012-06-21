package org.collectionspace.services.client;

import org.collectionspace.services.client.IQueryManager;

public interface IRelationsManager {
	public static final String DOC_TYPE = "Relation"; // Used for CMIS queries only -should be the same as what's in the tenant bindings
	public static final String SERVICE_NAME = "relations";
    public static final String SERVICE_COMMONPART_NAME = SERVICE_NAME + AbstractServiceClientImpl.PART_LABEL_SEPARATOR
    		+ AbstractServiceClientImpl.PART_COMMON_LABEL;	

	// Relations CMIS property mapping constants
	public final static String CMIS_CSPACE_RELATIONS_SUBJECT_ID = IQueryManager.CMIS_RELATIONS_PREFIX
			+ "." + SERVICE_COMMONPART_NAME + ":subjectCsid";
	public final static String CMIS_CSPACE_RELATIONS_OBJECT_ID = IQueryManager.CMIS_RELATIONS_PREFIX
			+ "." + SERVICE_COMMONPART_NAME + ":objectCsid";
	public final static String CMIS_CSPACE_RELATIONS_OBJECT_TYPE = IQueryManager.CMIS_RELATIONS_PREFIX
			+ "." + SERVICE_COMMONPART_NAME + ":objectDocumentType";
	public final static String CMIS_CSPACE_RELATIONS_TITLE = IQueryManager.CMIS_RELATIONS_PREFIX
			+ "." + IQueryManager.CMIS_NUXEO_TITLE;

}
