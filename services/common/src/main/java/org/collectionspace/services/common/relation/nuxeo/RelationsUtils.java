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

import java.lang.StringBuilder;

import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.common.relation.RelationJAXBSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RelationsUtils
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RelationsUtils {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(RelationsUtils.class);

    /**
     * Builds the where clause.
     *
     * @param subject the subject
     * @param predicate the predicate
     * @param object the object
     * @return the string
     */
    public static String buildWhereClause(String subject, String subjectType,
    		String predicate,
    		String object, String objectType) {
    	String result = null;
    	
    	StringBuilder stringBuilder = new StringBuilder();
    	if (subject != null) {
    		stringBuilder.append(RelationConstants.NUXEO_SCHEMA_NAME + ":" +
    				RelationJAXBSchema.SUBJECT_CSID + " = " + "'" + subject + "'");
    	}
    	
    	if (subjectType != null) {
    		if (stringBuilder.length() > 0) {
    			stringBuilder.append(IQueryManager.SEARCH_QUALIFIER_AND);
    		}
    		// BUG - this should use the new field RelationJAXBSchema.SUBJECT_DOCTYPE
    		stringBuilder.append(RelationConstants.NUXEO_SCHEMA_NAME + ":" +
    				RelationJAXBSchema.SUBJECT_DOCTYPE + " = " + "'" + subjectType + "'");
    	}
    	
    	if (predicate != null) {
    		if (stringBuilder.length() > 0) {
    			stringBuilder.append(IQueryManager.SEARCH_QUALIFIER_AND);
    		}
    		stringBuilder.append(RelationConstants.NUXEO_SCHEMA_NAME + ":" +
    				RelationJAXBSchema.RELATIONSHIP_TYPE + " = " + "'" + predicate + "'");
    	}
    	
    	if (object != null) {
    		if (stringBuilder.length() > 0) {
    			stringBuilder.append(IQueryManager.SEARCH_QUALIFIER_AND);
    		}
    		stringBuilder.append(RelationConstants.NUXEO_SCHEMA_NAME + ":" +
    				RelationJAXBSchema.OBJECT_CSID + " = " + "'" + object + "'");
    	}
    	
    	if (objectType != null) {
    		if (stringBuilder.length() > 0) {
    			stringBuilder.append(IQueryManager.SEARCH_QUALIFIER_AND);
    		}
    		// BUG - this should use the new field RelationJAXBSchema.OBJECT_DOCTYPE
    		stringBuilder.append(RelationConstants.NUXEO_SCHEMA_NAME + ":" +
    				RelationJAXBSchema.OBJECT_DOCTYPE + " = " + "'" + objectType + "'");
    	}
    	
    	if (stringBuilder.length() > 0) {
    		result = stringBuilder.toString();
    	}
    	
    	return result;
    }
}

