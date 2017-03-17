package org.collectionspace.services.collectionobject.nuxeo;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;

public class CollectionObjectBotGardenConstants {
    public final static String CORE_SCHEMA_NAME = CollectionObjectClient.COLLECTIONSPACE_CORE_SCHEMA;
    public static final String COMMON_SCHEMA_NAME = CollectionObjectClient.SERVICE_NAME + CollectionObjectClient.PART_LABEL_SEPARATOR + CollectionObjectClient.PART_COMMON_LABEL;
    
	public static final String BOTGARDEN_PROFILE_NAME = CollectionSpaceClient.BOTGARDEN_PROFILE_NAME;
    public static final String BOTGARDEN_SCHEMA_NAME = CollectionObjectClient.SERVICE_NAME + CollectionObjectClient.PART_LABEL_SEPARATOR + BOTGARDEN_PROFILE_NAME;
    public final static String NATURALHISTORY_SCHEMA_NAME = CollectionObjectClient.SERVICE_NAME + CollectionObjectClient.PART_LABEL_SEPARATOR + CollectionSpaceClient.NATURALHISTORY_EXTENSION_NAME;

    public final static String URI_SCHEMA_NAME = CORE_SCHEMA_NAME;
    public final static String URI_FIELD_NAME = CollectionSpaceClient.COLLECTIONSPACE_CORE_URI; //"uri";

    public final static String WORKFLOW_STATE_SCHEMA_NAME = CORE_SCHEMA_NAME;
    public final static String WORKFLOW_STATE_FIELD_NAME = CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE;

    public final static String FIELD_COLLECTION_PLACE_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String FIELD_COLLECTION_PLACE_FIELD_NAME = "localityGroupList/localityGroup/fieldLocPlace";

    public final static String TAXONOMIC_RANGE_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String TAXONOMIC_RANGE_FIELD_NAME = "localityGroupList/localityGroup/taxonomicRange";

    public final static String COMMENT_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String COMMENT_FIELD_NAME = "comments/comment";

    public final static String DEAD_FLAG_SCHEMA_NAME = BOTGARDEN_SCHEMA_NAME;
    public final static String DEAD_FLAG_FIELD_NAME = "deadFlag";

    public final static String DEAD_DATE_SCHEMA_NAME = BOTGARDEN_SCHEMA_NAME;
    public final static String DEAD_DATE_FIELD_NAME = "deadDate";

    public final static String TAXON_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String TAXON_FIELD_NAME = "taxonomicIdentGroupList/taxonomicIdentGroup/taxon";
    public final static String PRIMARY_TAXON_FIELD_NAME = "taxonomicIdentGroupList/taxonomicIdentGroup[0]/taxon";

    public final static String DETERMINATION_BY_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String DETERMINATION_BY_FIELD_NAME = "taxonomicIdentGroupList/taxonomicIdentGroup/identBy";

    public final static String DETERMINATION_DATE_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String DETERMINATION_DATE_FIELD_NAME = "taxonomicIdentGroupList/taxonomicIdentGroup/identDateGroup/dateDisplayDate";

    public final static String DETERMINATION_INSTITUTION_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String DETERMINATION_INSTITUTION_FIELD_NAME = "taxonomicIdentGroupList/taxonomicIdentGroup/institution";

    public final static String DETERMINATION_KIND_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String DETERMINATION_KIND_FIELD_NAME = "taxonomicIdentGroupList/taxonomicIdentGroup/identKind";
    public final static String DETERMINATION_KIND_DETERMINATION_VALUE = "determination";

    public final static String HYBRID_FLAG_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String HYBRID_FLAG_FIELD_NAME = "taxonomicIdentGroupList/taxonomicIdentGroup/hybridFlag";

    public final static String RARE_FLAG_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String RARE_FLAG_FIELD_NAME = "rare";

    public final static String HYBRID_PARENT_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String HYBRID_PARENT_FIELD_NAME = "taxonomicIdentGroupList/taxonomicIdentGroup/taxonomicIdentHybridParentGroupList/taxonomicIdentHybridParentGroup/taxonomicIdentHybridParent";

    public final static String HYBRID_QUALIFIER_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String HYBRID_QUALIFIER_FIELD_NAME = "taxonomicIdentGroupList/taxonomicIdentGroup/taxonomicIdentHybridParentGroupList/taxonomicIdentHybridParentGroup/taxonomicIdentHybridParentQualifier";

    public final static String HYBRID_QUALIFIER_FEMALE_VALUE = "female";
    public final static String HYBRID_QUALIFIER_MALE_VALUE = "male";
}
