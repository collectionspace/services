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
	
	public static final String ACTION_CODE_SCHEMA_NAME = MovementConstants.COMMON_SCHEMA_NAME;
	public static final String ACTION_CODE_FIELD_NAME = "reasonForMove";

	public static final String ACTION_DATE_SCHEMA_NAME = MovementConstants.COMMON_SCHEMA_NAME;
	public static final String ACTION_DATE_FIELD_NAME = "locationDate";
			
	public static final String DEAD_ACTION_CODE = "Dead";
	public static final String REVIVED_ACTION_CODE = "Revived";
	public static final String OTHER_ACTION_CODE = "Other";
		
	public static final String LABEL_REQUESTED_SCHEMA_NAME = BOTGARDEN_SCHEMA_NAME;
	public static final String LABEL_REQUESTED_FIELD_NAME = "labelRequested";
	public static final String LABEL_REQUESTED_YES_VALUE = "Yes";
	public static final String LABEL_REQUESTED_NO_VALUE = "No";
}
