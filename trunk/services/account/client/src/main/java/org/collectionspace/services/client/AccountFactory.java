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
import java.util.List;
import java.util.UUID;
import org.collectionspace.services.account.AccountTenant;
import org.collectionspace.services.account.AccountsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class AccountFactory {
    static private final Logger logger = LoggerFactory.getLogger(AccountFactory.class);

    /**
     * create account instance
     * @param screenName
     * @param userName
     * @param passwd
     * @param email
     * @param tenantId add non-null tenant id else let service take tenant id of
     * the authenticated user
     * @param useScreenName
     * @param invalidTenant
     * @param useUser
     * @param usePassword
     * @return
     */
   public static AccountsCommon createAccountInstance(String screenName,
            String userName, String passwd, String email, String tenantId,
            boolean useScreenName, boolean invalidTenant,
            boolean useUser, boolean usePassword) {

        AccountsCommon account = new AccountsCommon();
        if (useScreenName) {
            account.setScreenName(screenName);
        }
        if (useUser) {
            account.setUserId(userName);
        }
        if (usePassword) {
            //jaxb marshaller already b64 encodes the xs:base64Binary types
            //no need to double encode
//            byte[] b64pass = Base64.encodeBase64(passwd.getBytes());
//            account.setPassword(b64pass);
            if (logger.isDebugEnabled()) {
                logger.debug("user=" + userName + " password=" + passwd
                        + " password length=" + passwd.getBytes().length);
//
            }
            //jaxb encodes password too
            account.setPassword(passwd.getBytes());
        }

        account.setPersonRefName(screenName);
        account.setEmail(email);
        account.setPhone("1234567890");
        List<AccountTenant> atList = new ArrayList<AccountTenant>();
        AccountTenant at = new AccountTenant();
        if (!invalidTenant) {
            //tenant is not required to be added during create, service layer
            //picks up tenant from security context if needed
            if (tenantId != null) {
                at.setTenantId(tenantId);
                atList.add(at);
                account.setTenants(atList);
            }
        } else {
            //use invalid tenant id...called from validation test
            at.setTenantId(UUID.randomUUID().toString());
            atList.add(at);
            account.setTenants(atList);
        }
        return account;

    }

}
