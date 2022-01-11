package org.collectionspace.services.movement.nuxeo;

import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.CollectionSpaceClient;

/**
 * Constants related to the official Botanical Garden profile.
 * @author remillet
 *
 */
public class MovementBotGardenConstants {    
	public static final String BOTGARDEN_PROFILE_NAME = MovementClient.BOTGARDEN_PROFILE_NAME;
	public static final String BOTGARDEN_SCHEMA_NAME = MovementClient.SERVICE_NAME + CollectionSpaceClient.PART_LABEL_SEPARATOR + BOTGARDEN_PROFILE_NAME;
	
	public static final String PREVIOUS_LOCATION_SCHEMA_NAME = BOTGARDEN_SCHEMA_NAME;
	public static final String PREVIOUS_LOCATION_FIELD_NAME = "previousLocation";

	public static final String ACTION_CODE_SCHEMA_NAME = MovementConstants.COMMON_SCHEMA_NAME;
	public static final String ACTION_CODE_FIELD_NAME = "reasonForMove";

	public static final String ACTION_DATE_SCHEMA_NAME = MovementConstants.COMMON_SCHEMA_NAME;
	public static final String ACTION_DATE_FIELD_NAME = "locationDate";
	//
	// See CollectionSpace wiki documentation for "RefName" values --https://collectionspace.atlassian.net/wiki/spaces/DOC/pages/2593663841/RefName
	public static final String DEAD_ACTION_CODE = "urn:NID:NAMESPACE:vocabularies:name(actionCode):item:name(actCode00)";
	public static final String REVIVED_ACTION_CODE = "urn:NID:NAMESPACE:vocabularies:name(actionCode):item:name(actCode06)";
	public static final String OTHER_ACTION_CODE = "urn:NID:NAMESPACE:vocabularies:name(actionCode):item:name(actCode05)";
		
	public static final String LABEL_REQUESTED_SCHEMA_NAME = BOTGARDEN_SCHEMA_NAME;
	public static final String LABEL_REQUESTED_FIELD_NAME = "labelRequested";
	public static final String LABEL_REQUESTED_YES_VALUE = "Yes";
	public static final String LABEL_REQUESTED_NO_VALUE = "No";
}
