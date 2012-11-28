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
package org.collectionspace.services.taxonomy.nuxeo;

/**
 * TaxonConstants provides constants for Taxonomy documents
 *
 */
public class TaxonConstants {

    public final static String NUXEO_DOCTYPE = "Taxon";
    public final static String NUXEO_SCHEMA_NAME = "taxon";
    public final static String NUXEO_DC_TITLE = "CollectionSpace-Taxon";
    
    public final static String COMMON_SCHEMA_NAME = "taxon_common";

    public final static String IN_AUTHORITY_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String IN_AUTHORITY_FIELD_NAME = "inAuthority";

    public final static String DISPLAY_NAME_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String DISPLAY_NAME_FIELD_NAME = "taxonTermGroupList/taxonTermGroup/termDisplayName";
    
    public final static String FORMATTED_DISPLAY_NAME_SCHEMA_NAME = COMMON_SCHEMA_NAME;
    public final static String FORMATTED_DISPLAY_NAME_FIELD_NAME = "taxonTermGroupList/taxonTermGroup/termFormattedDisplayName";
}
