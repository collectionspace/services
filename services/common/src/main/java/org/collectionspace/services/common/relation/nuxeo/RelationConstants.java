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
package org.collectionspace.services.common.relation.nuxeo;

import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.relation.RelationshipType;

/**
 * RelationConstants processes Relation document
 *
 */
public class RelationConstants {

    public final static String NUXEO_DOCTYPE = "Relation";
    public final static String NUXEO_SCHEMA_NAME = "relations_common";
    /** The Constant REL_NUXEO_SCHEMA_ROOT_ELEMENT. */
    final public static String NUXEO_SCHEMA_ROOT_ELEMENT = "relationtype";

    public final static String COMMON_SCHEMA_NAME = IRelationsManager.SERVICE_COMMONPART_NAME;

    public final static String SUBJECT_CSID_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String SUBJECT_CSID_FIELD_NAME = IRelationsManager.SUBJECT;

    public final static String SUBJECT_DOCTYPE_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String SUBJECT_DOCTYPE_FIELD_NAME = IRelationsManager.SUBJECT_DOCTYPE;

    public final static String OBJECT_CSID_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String OBJECT_CSID_FIELD_NAME = IRelationsManager.OBJECT;

    public final static String OBJECT_DOCTYPE_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String OBJECT_DOCTYPE_FIELD_NAME = IRelationsManager.OBJECT_DOCTYPE;

    public final static String TYPE_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String TYPE_FIELD_NAME = IRelationsManager.RELATIONSHIP_TYPE;

    public final static String AFFECTS_TYPE = RelationshipType.AFFECTS.value();
    public final static String BROADER_TYPE = RelationshipType.HAS_BROADER.value();
}
