package org.collectionspace.services.collectionobject.nuxeo;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;

public class CollectionObjectBotGardenConstants {
    public final static String CORE_SCHEMA_NAME = CollectionObjectClient.COLLECTIONSPACE_CORE_SCHEMA;
    public static final String COMMON_SCHEMA_NAME = CollectionObjectClient.SERVICE_NAME + CollectionObjectClient.PART_LABEL_SEPARATOR + CollectionObjectClient.PART_COMMON_LABEL;
    
	public static final String BOTGARDEN_PROFILE_NAME = CollectionSpaceClient.BOTGARDEN_PROFILE_NAME;
    public static final String BOTGARDEN_SCHEMA_NAME = CollectionObjectClient.SERVICE_NAME + CollectionObjectClient.PART_LABEL_SEPARATOR + BOTGARDEN_PROFILE_NAME;

    public final static String URI_SCHEMA_NAME = CORE_SCHEMA_NAME;
    public final static String URI_FIELD_NAME = CollectionSpaceClient.COLLECTIONSPACE_CORE_URI; //"uri";

    public final static String WORKFLOW_STATE_SCHEMA_NAME = CORE_SCHEMA_NAME;
    public final static String WORKFLOW_STATE_FIELD_NAME = CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE;

    public final static String COMMENT_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String COMMENT_FIELD_NAME = "comments/comment";

    public final static String DEAD_FLAG_SCHEMA_NAME = BOTGARDEN_SCHEMA_NAME;
    public final static String DEAD_FLAG_FIELD_NAME = "deadFlag";

    public final static String DEAD_DATE_SCHEMA_NAME = BOTGARDEN_SCHEMA_NAME;
    public final static String DEAD_DATE_FIELD_NAME = "deadDate";

}
