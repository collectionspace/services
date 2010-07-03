/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;

/**
 * @author pschmitz
 *
 */
public interface LocationJAXBSchema extends AuthorityJAXBSchema {
	final static String LOCATIONS_COMMON = "locations_common";
	final static String DISPLAY_NAME_COMPUTED = "displayNameComputed";
	final static String NAME = "name";
	final static String CONDITION_NOTE = "conditionNote";
	final static String CONDITION_NOTE_DATE = "conditionNoteDate";
	final static String SECURITY_NOTE = "securityNote";
	final static String LOCATION_TYPE = "locationType";
}

