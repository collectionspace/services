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
 *//**
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.common.storage.jpa;

import java.util.List;
import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.context.ServiceContext;

/**
 * JPA query specific document filter
 */
public class JpaDocumentFilter extends DocumentFilter {

    public JpaDocumentFilter(ServiceContext ctx) {
        super(ctx);
    }

    /**
     * addTenant adds tenant id to the where clause
     * @param append indicates if append to existing where clause
     * @param paramList
     * @return whereClause with tenant context
     */
    protected String addTenant(boolean append, List<ParamBinding> paramList) {
        String whereClause = "";
        if (!append) {
            whereClause = " WHERE tenantId = :tenantId";
        } else {
            whereClause = " AND tenantId = :tenantId";
        }
        paramList.add(new ParamBinding("tenantId", getTenantId()));
        return whereClause;
    }
}
