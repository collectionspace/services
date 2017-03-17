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

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.MovementClient;

/**
 * MovementConstants specifies constants for the Movement service
 *
 */
public class MovementConstants {

    public final static String NUXEO_DOCTYPE = "Movement";
    public final static String NUXEO_SCHEMA_NAME = "movement";
    public final static String NUXEO_DC_TITLE = "CollectionSpace-Movement";
    
    public final static String CORE_SCHEMA_NAME = CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA;
	public static final String COMMON_SCHEMA_NAME = MovementClient.SERVICE_NAME + CollectionSpaceClient.PART_LABEL_SEPARATOR + CollectionSpaceClient.PART_COMMON_LABEL;
		
	public static final String CURRENT_LOCATION_SCHEMA_NAME = COMMON_SCHEMA_NAME;
	public static final String CURRENT_LOCATION_FIELD_NAME = "currentLocation";
	
	public static final String PREVIOUS_LOCATION_SCHEMA_NAME = COMMON_SCHEMA_NAME;
	public static final String PREVIOUS_LOCATION_FIELD_NAME = "previousLocation";
	
	public static final String WORKFLOW_STATE_SCHEMA_NAME = MovementConstants.CORE_SCHEMA_NAME;
	public static final String WORKFLOW_STATE_FIELD_NAME = CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE;
	
	public static final String NONE_LOCATION = null;		
}
