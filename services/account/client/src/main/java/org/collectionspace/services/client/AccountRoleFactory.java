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

package org.collectionspace.services.client;

import java.util.ArrayList;
import java.util.Collection;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.AccountValue;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.authorization.RoleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class AccountRoleFactory {
    static private final Logger logger = LoggerFactory.getLogger(AccountRoleFactory.class);

 /**
     * Creates the account role instance.
     *
     * @param pv the pv
     * @param rvs the rvs
     * @param usePermId the use perm id
     * @param useRoleId the use role id
     * @return the account role
     */
    static public AccountRole createAccountRoleInstance(AccountValue pv,
            Collection<RoleValue> rvs,
            boolean usePermId,
            boolean useRoleId) {

        AccountRole accRole = new AccountRole();
        //service consume is not required to provide subject as it is determined
        //from URI used
        accRole.setSubject(SubjectType.ROLE);
        if (usePermId) {
            ArrayList<AccountValue> pvs = new ArrayList<AccountValue>();
            pvs.add(pv);
            accRole.setAccount(pvs);
        }
        if (useRoleId) {
            //FIXME is there a better way?
            ArrayList<RoleValue> rvas = new ArrayList<RoleValue>();
            for (RoleValue rv : rvs) {
                rvas.add(rv);
            }
            accRole.setRole(rvas);
        }

        return accRole;
    }


}
