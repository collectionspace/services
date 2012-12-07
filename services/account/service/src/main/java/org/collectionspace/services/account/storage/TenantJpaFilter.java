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
        String name = null;
        List<String> nvals = getQueryParam(TenantStorageConstants.Q_NAME);
        if (null != nvals && nvals.size() > 0) {
            name = nvals.get(0);
        }
        boolean csAdmin = SecurityUtils.isCSpaceAdmin();
        if (null != name && !name.isEmpty()) {
            queryStrBldr.append(" WHERE UPPER(a.");
            queryStrBldr.append(TenantStorageConstants.NAME_FIELD);
            queryStrBldr.append(") LIKE :");
            queryStrBldr.append(" :" + TenantStorageConstants.Q_NAME);
            paramList.add(new ParamBinding(
            		TenantStorageConstants.Q_NAME, "%" + name.toUpperCase() + "%"));
        }

        String includeDisabledStr = null;
        List<String> inclDisVals = getQueryParam(TenantStorageConstants.Q_INCLUDE_DISABLED);
        if (null != inclDisVals && inclDisVals.size() > 0) {
            includeDisabledStr = inclDisVals.get(0);
        }
        // Default is to exclude disabled tenants, unless they specify to include them
    	boolean includeDisabled = (null != includeDisabledStr && !includeDisabledStr.isEmpty() 
    			&& Boolean.parseBoolean(includeDisabledStr));
    	// If excluding, then add a clause
       	if(!includeDisabled) {
        	queryStrBldr.append(" WHERE NOT a.");
            queryStrBldr.append(TenantStorageConstants.DISABLED_FIELD);
        }

        if (logger.isDebugEnabled()) {
            String query = queryStrBldr.toString();
            logger.debug("query=" + query);
        }

        return paramList;
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
