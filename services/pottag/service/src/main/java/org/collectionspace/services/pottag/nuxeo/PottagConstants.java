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
package org.collectionspace.services.pottag.nuxeo;

/**
 * PottagConstants specifies constants for the Loans In service
 *
 */
public class PottagConstants {

    public final static String NUXEO_DOCTYPE = "Pottag";
    public final static String NUXEO_SCHEMA_NAME = "pottag";
    public final static String NUXEO_DC_TITLE = "CollectionSpace-Pottag";

	public static final String COMMON_SCHEMA_NAME = "pottags_common";

    public final static String LABEL_REQUESTED_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String LABEL_REQUESTED_FIELD_NAME = "printLabels";
    public final static String LABEL_REQUESTED_YES_VALUE = "yes";
    public final static String LABEL_REQUESTED_NO_VALUE = "no";
}
