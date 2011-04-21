/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

/**
 * @author pschmitz
 *
 */
public interface LocationJAXBSchema extends AuthorityItemJAXBSchema {
	final static String LOCATIONS_COMMON = "locations_common";
	final static String NAME = "name";
	final static String CONDITION_GROUP_LIST = "conditionGroupList";
	final static String CONDITION_GROUP = "conditionGroup";
	final static String CONDITION_NOTE = "conditionNote";
	final static String CONDITION_NOTE_DATE = "conditionNoteDate";
	final static String SECURITY_NOTE = "securityNote";
	final static String ACCESS_NOTE = "accessNote";
	final static String ADDRESS = "address";
	final static String LOCATION_TYPE = "locationType";
}

