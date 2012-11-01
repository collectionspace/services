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
package org.collectionspace.services.collectionobject.nuxeo;

/**
 * CollectionObjectConstants processes CollectionObject document
 *
 */
public class CollectionObjectConstants {

    public final static String NUXEO_DOCTYPE = "CollectionObject";
    public final static String NUXEO_SCHEMA_NAME = "collectionobject";
    public final static String NUXEO_DC_TITLE = "CollectionSpace-CollectionObject";
    
    public final static String CORE_SCHEMA_NAME = "collectionspace_core";
    public final static String COMMON_SCHEMA_NAME = "collectionobjects_common";
    public final static String NATURALHISTORY_SCHEMA_NAME = "collectionobjects_naturalhistory";
    public final static String BOTGARDEN_SCHEMA_NAME = "collectionobjects_botgarden";

    public final static String WORKFLOW_STATE_SCHEMA_NAME = CORE_SCHEMA_NAME;
    public final static String WORKFLOW_STATE_FIELD_NAME = "workflowState";

    public final static String FIELD_COLLECTION_PLACE_SCHEMA_NAME = NATURALHISTORY_SCHEMA_NAME;
    public final static String FIELD_COLLECTION_PLACE_FIELD_NAME = "localityGroupList/localityGroup/fieldLocPlace";
    
    public final static String COMMENT_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String COMMENT_FIELD_NAME = "comments/comment";

    public final static String DEAD_FLAG_SCHEMA_NAME = BOTGARDEN_SCHEMA_NAME;
    public final static String DEAD_FLAG_FIELD_NAME = "deadFlag";

    public final static String DEAD_DATE_SCHEMA_NAME = BOTGARDEN_SCHEMA_NAME;
    public final static String DEAD_DATE_FIELD_NAME = "deadDate";
   
	public static final String DELETED_STATE = "deleted";
}
