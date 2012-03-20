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
package org.collectionspace.services.place.nuxeo;

import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityDocumentModelHandler;
import org.collectionspace.services.place.PlaceauthoritiesCommon;

/**
 * PlaceAuthorityDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class PlaceAuthorityDocumentModelHandler
        extends AuthorityDocumentModelHandler<PlaceauthoritiesCommon> {

    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "placeauthorities_common";   
    
    public PlaceAuthorityDocumentModelHandler() {
    	super(COMMON_PART_LABEL);
    }
	
    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return PlaceAuthorityConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

