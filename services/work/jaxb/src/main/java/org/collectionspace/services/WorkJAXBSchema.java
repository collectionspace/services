/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

/**
 * @author 
 *
 */
public interface WorkJAXBSchema extends AuthorityItemJAXBSchema {
    final static String WORKS_COMMON = "works_common";
    final static String WORK_HISTORY_NOTE = "historyNote";
    final static String WORK_AUTH_TYPE = "workAuthType";

    final static String WORK_CREATOR_GROUP_LIST = "creatorGroupList";
    final static String WORK_CREATOR_GROUP_CREATOR = "creator";
    final static String WORK_CREATOR_GROUP_CREATOR_TYPE = "creatorType";

    final static String WORK_PUBLISHER_GROUP_LIST = "publisherGroupList";
    final static String WORK_PUBLISHER_GROUP_PUBLISHER = "publisher";
    final static String WORK_PUBLISHER_GROUP_PUBLISHER_TYPE = "publisherType";
    
    final static String WORK_TERM_GROUP_LIST = "workTermGroupList";
    final static String WORK_TERM_DISPLAY_NAME = "termDisplayName";
    final static String WORK_TERM_NAME = "termName";
    final static String WORK_TERM_TYPE = "termType";
    final static String WORK_TERM_STATUS = "termStatus";
    final static String WORK_TERM_QUALIFIER = "termQualifier";
    final static String WORK_TERM_LANGUAGE = "termLanguage";
    final static String WORK_TERM_PREFFORLANGUAGE = "termPrefForLang";
    final static String WORK_TERM_SOURCE = "termSource";
    final static String WORK_TERM_SOURCE_DETAIL = "termSourceDetail";
    final static String WORK_TERM_SOURCE_ID = "termSourceID";
    final static String WORK_TERM_SOURCE_NOTE = "termSourceNote";
    
    final static String WORK_SHORT_IDENTIFIER = "shortIdentifier";
    final static String WORK_REFNAME = "refName";
    final static String WORK_INAUTHORITY = "inAuthority";
}