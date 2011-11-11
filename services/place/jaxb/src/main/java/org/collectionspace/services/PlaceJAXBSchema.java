/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;

/**
 * @author pschmitz
 *
 */
public interface PlaceJAXBSchema extends AuthorityItemJAXBSchema {
	final static String PLACES_COMMON = "places_common";
	final static String DISPLAY_NAME = "displayName";
	final static String DISPLAY_NAME_COMPUTED = "displayNameComputed";
	final static String SHORT_DISPLAY_NAME = "shortDisplayName";
	final static String SHORT_DISPLAY_NAME_COMPUTED = "shortDisplayNameComputed";
	final static String FULL_DISPLAY_NAME = "fullDisplayName";
	final static String FULL_DISPLAY_NAME_COMPUTED = "fullDisplayNameComputed";
        final static String PLACE_NAME_GROUP_LIST = "placeNameGroupList";
        final static String NAME = "name";
	final static String NOTE = "note";
	final static String SOURCE = "source";
	final static String SOURCE_PAGE = "sourcePage";
	final static String PLACE_TYPE = "placeType";
}

