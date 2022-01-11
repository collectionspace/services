package org.collectionspace.services.taxonomy.nuxeo;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.TaxonomyAuthorityClient;

public class TaxonBotGardenConstants {
	public static final String NATURALHISTORY_SCHEMA_NAME = TaxonomyAuthorityClient.SERVICE_ITEM_NAME + CollectionSpaceClient.PART_LABEL_SEPARATOR + CollectionSpaceClient.NATURALHISTORY_EXTENSION_NAME;

    public final static String CONSERVATION_CATEGORY_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String CONSERVATION_CATEGORY_FIELD_NAME = "plantAttributesGroupList/plantAttributesGroup/conservationCategory";    

    public final static String ACCESS_CODE_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String ACCESS_CODE_FIELD_NAME = "accessRestrictions";    
    public final static String ACCESS_CODE_DEAD_VALUE = "Dead";    
    public final static String ACCESS_CODE_UNRESTRICTED_VALUE = "Unrestricted";
    
    public final static String COMMON_VOCABULARY_SHORTID = "common";    
}
