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
package org.collectionspace.services.authorization.storage;

import java.util.ArrayList;
import java.util.List;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.storage.jpa.JpaDocumentFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PermissionJpaFilter is to build where clause for role queries
 * @author 
 */
public class PermissionJpaFilter extends JpaDocumentFilter {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(PermissionJpaFilter.class);

    /**
     * Instantiates a new permission jpa filter.
     * 
     * @param ctx the ctx
     */
    public PermissionJpaFilter(ServiceContext ctx) {
        super(ctx);
    }

    /**
     * Append where clause.
     *
     * @param csAdmin the cs admin
     * @param paramList the param list
     * @param queryStrBldr the query str bldr
     * @param fieldName the field name
     * @param queryParam the query param
     * @return the string builder
     */
    private StringBuilder appendWhereClause(boolean whereOpNeeded,
    		StringBuilder queryStrBldr,
    		String fieldName,
    		String queryParamName,
    		List<ParamBinding> paramList,
    		String queryParamValue) {
    	String op = whereOpNeeded ? " WHERE " : " AND ";
        if (queryParamValue != null && !queryParamValue.isEmpty()) {
        	queryStrBldr.append(op);
            queryStrBldr.append("UPPER(a." + fieldName + ")");
            queryStrBldr.append(" LIKE");
            queryStrBldr.append(" :" + queryParamName);            
            paramList.add(new ParamBinding(queryParamName, "%"
                    + queryParamValue.toUpperCase() + "%"));            
        }
        
        return queryStrBldr;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentFilter#buildWhereForSearch(java.lang.StringBuilder)
     */
    @Override
    public List<ParamBinding> buildWhereForSearch(StringBuilder queryStrBldr) {

        List<ParamBinding> paramList = new ArrayList<ParamBinding>();
        boolean whereOpNeeded = true;

        boolean csAdmin = SecurityUtils.isCSpaceAdmin();
        if (!csAdmin) {
            queryStrBldr.append(addTenant(false /* add WHERE or AND */,
            		paramList));
            whereOpNeeded = false;
        }

        // get the resource query param
        String resName = null;
        List<String> resNames = getQueryParam(PermissionStorageConstants.Q_RESOURCE_NAME);
        if (resNames != null && resNames.size() > 0) {
        	// grab just the first instance
            resName = resNames.get(0);
            appendWhereClause(whereOpNeeded,
            		queryStrBldr,
            		PermissionStorageConstants.RESOURCE_NAME,
            		PermissionStorageConstants.Q_RESOURCE_NAME,
            		paramList,
            		resName);
            whereOpNeeded = false;
        }
        
        
        // get the actiongroup query param
        String actionGroup = null;
        List<String> actionGroups = getQueryParam(PermissionStorageConstants.Q_ACTION_GROUP);
        if (actionGroups != null && actionGroups.size() > 0) {
        	actionGroup = actionGroups.get(0);
            appendWhereClause(whereOpNeeded,
            		queryStrBldr,
            		PermissionStorageConstants.ACTION_GROUP,
            		PermissionStorageConstants.Q_ACTION_GROUP,
            		paramList,
            		actionGroup);
            whereOpNeeded = false;
        }        
        
//        if (null != resName && !resName.isEmpty()) {
//            if (!csAdmin) {
//                queryStrBldr.append(" AND");
//            } else {
//                queryStrBldr.append(" WHERE");
//            }
//            queryStrBldr.append(" UPPER(a." + PermissionStorageConstants.RESOURCE_NAME + ")");
//            queryStrBldr.append(" LIKE");
//            queryStrBldr.append(" :" + PermissionStorageConstants.Q_RESOURCE_NAME);
//            paramList.add(new ParamBinding(PermissionStorageConstants.Q_RESOURCE_NAME, "%"
//                    + resName.toUpperCase() + "%"));
//        }

        if (logger.isDebugEnabled()) {
            String query = queryStrBldr.toString();
            logger.debug("query=" + query);
        }

        return paramList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentFilter#buildWhere(java.lang.StringBuilder)
     */
    @Override
    public List<ParamBinding> buildWhere(StringBuilder queryStrBldr) {
        return new ArrayList<ParamBinding>();
    }
}
