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
			
    /** The Constant SUBJECT. */
    static public final String SUBJECT = "subjectCsid";
    static public final String SUBJECT_REFNAME = "subjectRefName";    
    static public final String SUBJECT_QP = "sbj";
    static public final String SUBJECT_TYPE = "subjectType";
    static public final String SUBJECT_TYPE_QP = SUBJECT_QP + "Type";
    
    /** The Constant PREDICATE. */
    static public final String PREDICATE = "predicate";
    static public final String PREDICATE_QP = "prd";
    
    /** The Constant OBJECT. */
    static public final String OBJECT = "objectCsid";
    static public final String OBJECT_REFNAME = "objectRefName";
    static public final String OBJECT_QP = "obj";
    static public final String OBJECT_TYPE = "objectType";
    static public final String OBJECT_TYPE_QP = OBJECT_QP + "Type";
}
