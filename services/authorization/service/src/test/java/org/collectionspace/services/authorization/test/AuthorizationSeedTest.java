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
package org.collectionspace.services.authorization.test;

import java.io.File;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.collectionspace.services.authorization.ActionType;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.PermissionConfig;
import org.collectionspace.services.authorization.EffectType;
import org.collectionspace.services.authorization.PermissionConfigList;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author 
 */
public class AuthorizationSeedTest {

    final Logger logger = LoggerFactory.getLogger(AuthorizationSeedTest.class);

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
    public static Object[][] testName(Method m) {
        return new Object[][]{
                    new Object[]{m.getName()}
                };
    }

    @BeforeClass(alwaysRun = true)
    public void seedData() {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                new String[]{"applicationContext-authorization-test.xml"});
        GrantedAuthority gauth = new GrantedAuthorityImpl("ROLE_ADMINISTRATOR");
        HashSet<GrantedAuthority> gauths = new HashSet<GrantedAuthority>();
        gauths.add(gauth);
        Authentication authRequest = new UsernamePasswordAuthenticationToken("test", "test", gauths);

        SecurityContextHolder.getContext().setAuthentication(authRequest);
        AuthZ authZ = AuthZ.get();

        org.springframework.jdbc.datasource.DataSourceTransactionManager txManager =
                (org.springframework.jdbc.datasource.DataSourceTransactionManager) appContext.getBean("transactionManager");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        // explicitly setting the transaction name is something that can only be done programmatically
        def.setName("seedData");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txManager.getTransaction(def);
        try {
            seedRoles();
            seedPermissions();
        } catch (Exception ex) {
            txManager.rollback(status);
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        txManager.commit(status);

    }

    public void seedRoles() throws Exception {
    }

    public void seedPermissions() throws Exception {

        PermissionConfigList pcList =
                (PermissionConfigList) fromFile(PermissionConfigList.class,
                "./test-data/test-permissions.xml");
        AuthZ authZ = AuthZ.get();
        for (PermissionConfig pc : pcList.getPermission()) {
            if(logger.isDebugEnabled()) {
                logger.debug("adding permission for res=" + pc.getResourceName());
            }
            authZ.addPermissions(pc);
        }
    }

    private void genPermissions() {
        PermissionConfigList pcList = new PermissionConfigList();
        ArrayList<PermissionConfig> apcList = new ArrayList<PermissionConfig>();
        pcList.setPermission(apcList);
        PermissionConfig pc = new PermissionConfig();
        pc.setResourceName("accounts");
        pc.setEffect(EffectType.PERMIT);
        ArrayList<String> roles = new ArrayList<String>();
        roles.add("ROLE_USERS");
        roles.add("ROLE_ADMINISTRATOR");
        pc.setRole(roles);
        ArrayList<ActionType> actions = new ArrayList<ActionType>();
        actions.add(ActionType.CREATE);
        actions.add(ActionType.READ);
        actions.add(ActionType.UPDATE);
        actions.add(ActionType.DELETE);
        pc.setAction(actions);
        apcList.add(pc);
        toFile(pcList, PermissionConfigList.class, "./target/test-permissions.xml");

    }

    private void toFile(Object o, Class jaxbClass, String fileName) {
        File f = new File(fileName);
        try {
            JAXBContext jc = JAXBContext.newInstance(jaxbClass);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object fromFile(Class jaxbClass, String fileName) throws Exception {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        InputStream is = tccl.getResourceAsStream(fileName);
        JAXBContext context = JAXBContext.newInstance(jaxbClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        //note: setting schema to null will turn validator off
        unmarshaller.setSchema(null);
        return jaxbClass.cast(unmarshaller.unmarshal(is));
    }

    @Test(dataProvider = "testName", dataProviderClass = AuthorizationSeedTest.class)
    public void test(String testName) {
        if (logger.isDebugEnabled()) {
            logger.debug(testName);
        }
    }
}
