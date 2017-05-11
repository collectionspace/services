/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

public interface MaterialJAXBSchema extends AuthorityItemJAXBSchema {
    final static String MATERIALS_COMMON = "materials_common";

    final static String MATERIAL_DESCRIPTION = "description";

    final static String MATERIAL_TERM_GROUP_LIST = "materialTermGroupList";
    final static String MATERIAL_TERM_DISPLAY_NAME = "termDisplayName";
    final static String MATERIAL_TERM_NAME = "termName";
    final static String MATERIAL_TERM_TYPE = "termType";
    final static String MATERIAL_TERM_STATUS = "termStatus";
    final static String MATERIAL_TERM_QUALIFIER = "termQualifier";
    final static String MATERIAL_TERM_LANGUAGE = "termLanguage";
    final static String MATERIAL_TERM_PREFFORLANGUAGE = "termPrefForLang";
    final static String MATERIAL_TERM_SOURCE = "termSource";
    final static String MATERIAL_TERM_SOURCE_DETAIL = "termSourceDetail";
    final static String MATERIAL_TERM_SOURCE_ID = "termSourceID";
    final static String MATERIAL_TERM_SOURCE_NOTE = "termSourceNote";

    final static String MATERIAL_SHORT_IDENTIFIER = "shortIdentifier";
    final static String MATERIAL_REFNAME = "refName";
    final static String MATERIAL_INAUTHORITY = "inAuthority";
}

