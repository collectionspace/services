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
package org.collectionspace.services.account.storage;

import java.util.ArrayList;
import java.util.List;
import org.collectionspace.services.common.storage.jpa.JpaDocumentFilter;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class TenantJpaFilter extends JpaDocumentFilter {

    private final Logger logger = LoggerFactory.getLogger(TenantJpaFilter.class);

    public TenantJpaFilter(ServiceContext ctx) {
        super(ctx);
    }

    @Override
    public List<ParamBinding> buildWhereForSearch(StringBuilder queryStrBldr) {

        List<ParamBinding> paramList = new ArrayList<ParamBinding>();
        List<String> paramVals = null;
    	boolean whereAdded = false;
        {
        	// Look for a name filter
        	String name = null;
        	paramVals = getQueryParam(TenantStorageConstants.Q_NAME);
        	if (null != paramVals && paramVals.size() > 0) {
        		name = paramVals.get(0);
        	}
        	//boolean csAdmin = SecurityUtils.isCSpaceAdmin();
        	if (null != name && !name.isEmpty()) {
        		queryStrBldr.append(" WHERE UPPER(a.");
        		queryStrBldr.append(TenantStorageConstants.NAME_FIELD);
        		queryStrBldr.append(") LIKE :");
        		queryStrBldr.append(TenantStorageConstants.Q_NAME);
        		paramList.add(new ParamBinding(
        				TenantStorageConstants.Q_NAME, "%" + name.toUpperCase() + "%"));
        		whereAdded = true;
        	}
        }

        {
        	// Look for a disabled sense filter
        	String includeDisabledStr = null;
        	paramVals = getQueryParam(TenantStorageConstants.Q_INCLUDE_DISABLED);
        	if (null != paramVals && paramVals.size() > 0) {
        		includeDisabledStr = paramVals.get(0);
        	}
        	// Default is to exclude disabled tenants, unless they specify to include them
        	boolean includeDisabled = (null != includeDisabledStr && !includeDisabledStr.isEmpty() 
        			&& Boolean.parseBoolean(includeDisabledStr));
        	// If excluding, then add a clause
        	if(!includeDisabled) {
        		queryStrBldr.append(whereAdded?" AND ":" WHERE ");
        		queryStrBldr.append("a.");
        		queryStrBldr.append(TenantStorageConstants.DISABLED_FIELD);
        		queryStrBldr.append("=false");
        	}
        }
       	// Consider order by param. Just pick first for now.
        {
        	String orderBy = null;
        	paramVals = getQueryParam(TenantStorageConstants.Q_ORDER_BY);
        	if (null != paramVals && paramVals.size() > 0) {
        		orderBy = paramVals.get(0);
        	}
        	orderBy = checkOrderByField(orderBy);
        	queryStrBldr.append(" ORDER BY a.");
        	queryStrBldr.append(orderBy);
        }

       	// Consider order direction param. Just pick first for now.
        {
        	String orderDir = null;
        	paramVals = getQueryParam(TenantStorageConstants.Q_ORDER_DIR);
        	if (null != paramVals && paramVals.size() > 0) {
        		orderDir = paramVals.get(0);
        	}
        	orderDir = checkOrderDirValue(orderDir);
        	queryStrBldr.append(orderDir);
       	}


        if (logger.isDebugEnabled()) {
            String query = queryStrBldr.toString();
            logger.debug("query=" + query);
        }

        return paramList;
    }
    
    private String checkOrderByField(String input) {
    	String returnVal = TenantStorageConstants.NAME_FIELD;	// This is the default
        if (null != input && !input.isEmpty()) {
        	if(TenantStorageConstants.ID_FIELD.equalsIgnoreCase(input)) {
        		returnVal = TenantStorageConstants.ID_FIELD;
        	/* Effect of default is same, so skip this
        	} else if(TenantStorageConstants.NAME_FIELD.equalsIgnoreCase(input)) {
        		returnVal = TenantStorageConstants.NAME_FIELD;
        	*/
        	}
        }
    	return returnVal;
    }

    private String checkOrderDirValue(String input) {
    	String returnVal = JPA_ASC;	// This is the default
        if (null != input && !input.isEmpty()) {
        	if(Q_DESC.equalsIgnoreCase(input)) {
        		returnVal = JPA_DESC;
        	/* Effect of default is same, so skip this
        	} else if(Q_ASC.equalsIgnoreCase(input)) {
        		returnVal = JPA_ASC;
        	*/
        	}
        }
    	return returnVal;
    }

    @Override
    public List<ParamBinding> buildWhere(StringBuilder queryStrBldr) {
        return new ArrayList<ParamBinding>();
    }

    @Override
    protected String addTenant(boolean append, List<ParamBinding> paramList) {
    	// unused for tenants - special case
        return "";
    }
}
