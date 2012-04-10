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
        final static String PLACE_NAME_GROUP_LIST = "placeNameGroupList";
        final static String PLACE_NAME = "placeName";
	final static String NOTE = "note";
	final static String SOURCE = "source";
	final static String SOURCE_PAGE = "sourcePage";
	final static String PLACE_TYPE = "placeType";
}

