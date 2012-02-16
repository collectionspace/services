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
    public static String AuthorityItemCSID_REPLACE="${itemCSID}";

    public static String showRelations_QP = "showRelations";
    public static String showSiblings_QP = "showSiblings";
    public static String showAllRelations_QP = "showAllRelations";
}

