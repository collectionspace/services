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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PermissionJpaFilter is to build where clause for role queries
 * @author 
 */
public class PermissionJpaFilter extends JpaDocumentFilter {

    private final Logger logger = LoggerFactory.getLogger(PermissionJpaFilter.class);

    @Override
    public List<ParamBinding> buildWhereForSearch(StringBuilder queryStrBldr) {

        List<ParamBinding> paramList = new ArrayList<ParamBinding>();
        boolean hasWhere = false;
        //TODO: add tenant id

        String resName = null;
        List<String> rn = getQueryParam(PermissionStorageConstants.Q_RESOURCE_NAME);
        if (null != rn) {
            resName = rn.get(0);
        }
        if (null != resName && !resName.isEmpty()) {
            hasWhere = true;
            queryStrBldr.append(" WHERE");
            queryStrBldr.append(" UPPER(a." + PermissionStorageConstants.RESOURCE_NAME + ")");
            queryStrBldr.append(" LIKE");
            queryStrBldr.append(" :" + PermissionStorageConstants.Q_RESOURCE_NAME);
            paramList.add(new ParamBinding(PermissionStorageConstants.Q_RESOURCE_NAME, "%"
                    + resName.toUpperCase() + "%"));
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
