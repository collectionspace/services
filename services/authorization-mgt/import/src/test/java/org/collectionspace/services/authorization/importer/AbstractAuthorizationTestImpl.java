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
package org.collectionspace.services.authorization.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.util.HashSet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.PermissionsList;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author 
 */
public abstract class AbstractAuthorizationTestImpl {

    final Logger logger = LoggerFactory.getLogger(AbstractAuthorizationTestImpl.class);
    private org.springframework.jdbc.datasource.DataSourceTransactionManager txManager;

    /**
     * Returns the name of the currently running test.
     *
     * Note: although the return type is listed as Object[][],
     * this method instead returns a String.
     *
     * @param   m  The currently running test method.
     *
     * @return  The name of the currently running test method.
     */
    @DataProvider(name = "testName")
    protected static Object[][] testName(Method m) {
        return new Object[][]{
                    new Object[]{m.getName()}
                };
    }

    protected void setup() {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                new String[]{"applicationContext-authorization-test.xml"});
        login();
        AuthZ authZ = AuthZ.get();
        txManager = (org.springframework.jdbc.datasource.DataSourceTransactionManager) appContext.getBean("transactionManager");
    }

    protected void login() {
        GrantedAuthority gauth = new GrantedAuthorityImpl("ROLE_ADMINISTRATOR");
        HashSet<GrantedAuthority> gauths = new HashSet<GrantedAuthority>();
        gauths.add(gauth);
        Authentication authRequest = new UsernamePasswordAuthenticationToken("test", "test", gauths);
        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }

    protected void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    protected TransactionStatus beginTransaction(String name) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        // explicitly setting the transaction name is something that can only be done programmatically
        def.setName(name);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return txManager.getTransaction(def);
    }

    protected void rollbackTransaction(TransactionStatus status) {
        txManager.rollback(status);
    }

    protected void commitTransaction(TransactionStatus status) {
        txManager.commit(status);
    }



    @Test(dataProvider = "testName", dataProviderClass = AbstractAuthorizationTestImpl.class)
    public void test(String testName) {
        if (logger.isDebugEnabled()) {
            logger.debug(testName);
        }
    }
}
