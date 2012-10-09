/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.movement.nuxeo;

/**
 * MovementConstants specifies constants for the Movement service
 *
 */
public class MovementConstants {

    public final static String NUXEO_DOCTYPE = "Movement";
    public final static String NUXEO_SCHEMA_NAME = "movement";
    public final static String NUXEO_DC_TITLE = "CollectionSpace-Movement";
    
    public final static String CORE_SCHEMA_NAME = "collectionspace_core";
	public static final String COMMON_SCHEMA_NAME = "movements_common";
	public static final String NATURALHISTORY_SCHEMA_NAME = "movements_naturalhistory";
	
	public static final String ACTION_CODE_SCHEMA_NAME = COMMON_SCHEMA_NAME;
	public static final String ACTION_CODE_FIELD_NAME = "reasonForMove";

	public static final String ACTION_DATE_SCHEMA_NAME = COMMON_SCHEMA_NAME;
	public static final String ACTION_DATE_FIELD_NAME = "locationDate";
	
	public static final String CURRENT_LOCATION_SCHEMA_NAME = COMMON_SCHEMA_NAME;
	public static final String CURRENT_LOCATION_FIELD_NAME = "currentLocation";
	
	public static final String PREVIOUS_LOCATION_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
	public static final String PREVIOUS_LOCATION_FIELD_NAME = "previousLocation";
	
	public static final String WORKFLOW_STATE_SCHEMA_NAME = CORE_SCHEMA_NAME;
	public static final String WORKFLOW_STATE_FIELD_NAME = "workflowState";
	
	public static final String DEAD_ACTION_CODE = "Dead";
	public static final String REVIVED_ACTION_CODE = "Revived";

	public static final String NONE_LOCATION = null;
	
	public static final String DELETE_TRANSITION = "delete";
	public static final String DELETED_STATE = "deleted";
}
