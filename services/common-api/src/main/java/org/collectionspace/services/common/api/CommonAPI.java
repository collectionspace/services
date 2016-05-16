package org.collectionspace.services.common.api;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class CommonAPI {
    public static final String COMMON_API = "services common api version 1";
    public static String getVersionString(){
        return COMMON_API;
    }
    public static final String AuthorityItemCSID_REPLACE="${itemCSID}";

    public static final String showRelations_QP = "showRelations";
    public static final String showSiblings_QP = "showSiblings";
    public static final String showAllRelations_QP = "showAllRelations";
    
	public static final String GENERATE_BUNDLES = "core";
	public static final String GENERATE_BINDINGS = "delta";
	
	//
	// A Nuxeo facet that let's us know whether or not the image/picture was sourced
	// from an external URL.
	//
    public static final String URL_SOURCED_PICTURE = "URLSourcedPicture";
    public static final String NUXEO_DUBLINCORE_SCHEMANAME = "dublincore";
    public static final String NUXEO_DUBLINCORE_TITLE = "title";
    public static final String NUXEO_DUBLINCORE_SOURCE = "source";
    
}

