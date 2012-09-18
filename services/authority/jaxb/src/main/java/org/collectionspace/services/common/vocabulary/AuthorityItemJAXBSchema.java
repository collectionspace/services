/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright 2009 University of California at Berkeley
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.collectionspace.services.common.vocabulary;

/**
 * @author pschmitz
 *
 */
public interface AuthorityItemJAXBSchema {

    final static String IN_AUTHORITY = "inAuthority"; // REM - Is this a CSID? Or a refname? Either?
    final static String REF_NAME = "refName";
    final static String ORDER = "order";
    final static String SHORT_IDENTIFIER = "shortIdentifier";
    final static String CSID = "csid";
    final static String DISPLAY_NAME = "displayName"; // This is the display name element for the Vocabulary service's item	
    final static String TERM_DISPLAY_NAME = "termDisplayName"; // This is the display name element for all Authority services' items
    final static String TERM_NAME = "termName";
    final static String TERM_STATUS = "termStatus";
    final static String TERM_INFO_GROUP_SCHEMA_NAME = ""; // FIXME: REM - Needs
    // to be defined.
    // CSPACE-4813 - Remove all the below values and recompile all authorityitem
    // related classes
    final static String DISPLAY_NAME_COMPUTED = "displayNameComputed";
    final static String SHORT_DISPLAY_NAME = "shortDisplayName";
    final static String SHORT_DISPLAY_NAME_COMPUTED = "shortDisplayNameComputed";
    // final static String TERM_STATUS = "termStatus";
}
