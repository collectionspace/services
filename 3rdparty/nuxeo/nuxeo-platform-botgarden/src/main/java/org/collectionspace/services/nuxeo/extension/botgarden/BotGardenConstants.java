package org.collectionspace.services.nuxeo.extension.botgarden;

public class BotGardenConstants {
	public static final String MOVEMENT_DOCTYPE = "Movement";
	
	public static final String ACTION_CODE_SCHEMA_NAME = "movements_common";
	public static final String ACTION_CODE_FIELD_NAME = "reasonForMove";

	public static final String CURRENT_LOCATION_SCHEMA_NAME = "movements_common";
	public static final String CURRENT_LOCATION_FIELD_NAME = "currentLocation";
	
	public static final String PREVIOUS_LOCATION_SCHEMA_NAME = "movements_naturalhistory";
	public static final String PREVIOUS_LOCATION_FIELD_NAME = "previousLocation";
	
	public static final String DEAD_ACTION_CODE = "Dead";
	public static final String REVIVED_ACTION_CODE = "Revived";

	public static final String NONE_LOCATION = null;
	
	public static final String DELETE_TRANSITION = "delete";
}
