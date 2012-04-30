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
import org.collectionspace.services.common.storage.jpa.JpaDocumentFilter;
import org.collectionspace.services.common.authorization_mgt.RoleStorageConstants;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RoleJpaFilter is to build where clause for role queries
 * @author 
 */
public class RoleJpaFilter extends JpaDocumentFilter {

    private final Logger logger = LoggerFactory.getLogger(RoleJpaFilter.class);

    public RoleJpaFilter(ServiceContext ctx) {
        super(ctx);
    }

    @Override
    public List<ParamBinding> buildWhereForSearch(StringBuilder queryStrBldr) {

        List<ParamBinding> paramList = new ArrayList<ParamBinding>();
        String roleName = null;
        List<String> rn = getQueryParam(RoleStorageConstants.Q_ROLE_NAME);
        if (null != rn && rn.size() > 0) {
            roleName = rn.get(0);
        }
        boolean csAdmin = SecurityUtils.isCSpaceAdmin();
        if (!csAdmin) {
            queryStrBldr.append(addTenant(false, paramList));
        }
        if (null != roleName && !roleName.isEmpty()) {
            if (!csAdmin) {
                queryStrBldr.append(" AND");
            } else {
                queryStrBldr.append(" WHERE");
            }
            queryStrBldr.append(" UPPER(a." + RoleStorageConstants.ROLE_NAME + ")");
            queryStrBldr.append(" LIKE");
            queryStrBldr.append(" :" + RoleStorageConstants.Q_ROLE_NAME);
            paramList.add(new ParamBinding(RoleStorageConstants.Q_ROLE_NAME, "%"
                    + roleName.toUpperCase() + "%"));
            queryStrBldr.append(addTenant(true, paramList));
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
}
