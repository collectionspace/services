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
public class AccountJpaFilter extends JpaDocumentFilter {

    private final Logger logger = LoggerFactory.getLogger(AccountJpaFilter.class);

    public AccountJpaFilter(ServiceContext ctx) {
        super(ctx);
    }

    @Override
    public List<ParamBinding> buildWhereForSearch(StringBuilder queryStrBldr) {

        List<ParamBinding> paramList = new ArrayList<ParamBinding>();
        String screenName = null;
        List<String> snvals = getQueryParam(AccountStorageConstants.Q_SCREEN_NAME);
        if (null != snvals && snvals.size() > 0) {
            screenName = snvals.get(0);
        }
        boolean csAdmin = SecurityUtils.isCSpaceAdmin();
        if (!csAdmin) {
            queryStrBldr.append(addTenant(false, paramList));
        }
        if (null != screenName && !screenName.isEmpty()) {
            if (!csAdmin) {
                queryStrBldr.append(" AND");
            } else {
                queryStrBldr.append(" WHERE");
            }
            queryStrBldr.append(" UPPER(a." + AccountStorageConstants.SCREEN_NAME + ")");
            queryStrBldr.append(" LIKE");
            queryStrBldr.append(" :" + AccountStorageConstants.Q_SCREEN_NAME);
            paramList.add(new ParamBinding(AccountStorageConstants.Q_SCREEN_NAME, "%"
                    + screenName.toUpperCase() + "%"));
        }

        String uid = null;
        List<String> uidvals = getQueryParam(AccountStorageConstants.Q_USER_ID);
        if (null != uidvals && uidvals.size() > 0) {
            uid = uidvals.get(0);
        }
        if (null != uid && !uid.isEmpty()) {
            if (!csAdmin) {
                queryStrBldr.append(" AND");
            } else {
                queryStrBldr.append(" WHERE");
            }
            queryStrBldr.append(" UPPER(a." + AccountStorageConstants.USER_ID + ")");
            queryStrBldr.append(" LIKE");
            queryStrBldr.append(" :" + AccountStorageConstants.Q_USER_ID);
            paramList.add(new ParamBinding(AccountStorageConstants.Q_USER_ID, "%"
                    + uid.toUpperCase() + "%"));
        }

        String email = null;
        List<String> emailvals = getQueryParam(AccountStorageConstants.Q_EMAIL);
        if (null != emailvals && emailvals.size() > 0) {
            email = emailvals.get(0);
        }
        if (null != email && !email.isEmpty()) {

            if (!csAdmin) {
                queryStrBldr.append(" AND");
            } else {
                queryStrBldr.append(" WHERE");
            }
            queryStrBldr.append(" UPPER(a." + AccountStorageConstants.EMAIL + ")");
            queryStrBldr.append(" LIKE");
            queryStrBldr.append(" :" + AccountStorageConstants.Q_EMAIL);
            paramList.add(new ParamBinding(AccountStorageConstants.Q_EMAIL, "%"
                    + email.toUpperCase() + "%"));
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
        String tenantId = getTenantId();
        String whereClause = " JOIN a.tenants as at WHERE at.tenantId = :tenantId";
        paramList.add(new ParamBinding("tenantId", tenantId));
        return whereClause;
    }
}
