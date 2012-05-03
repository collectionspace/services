/**
 * 
 */
package org.collectionspace.services;

import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

/**
 * @author pschmitz
 *
 */
public interface OrganizationJAXBSchema extends AuthorityItemJAXBSchema {

    final static String ORGANIZATIONS_COMMON = "organizations_common";
    final static String MAIN_BODY_GROUP_LIST = "mainBodyGroupList";
    final static String LONG_NAME = "longName";
    final static String SHORT_NAME = "shortName";
    final static String NAME_ADDITIONS = "nameAdditions";
    final static String CONTACT_NAMES = "contactNames";
    // final static String FOUNDING_DATE = "foundingDate";
    // final static String DISSOLUTION_DATE = "dissolutionDate";
    final static String FOUNDING_PLACE = "foundingPlace";
    final static String GROUPS = "groups";
    final static String FUNCTIONS = "functions";
    final static String SUB_BODIES = "subBodies";
    final static String HISTORY_NOTES = "historyNotes";
}


