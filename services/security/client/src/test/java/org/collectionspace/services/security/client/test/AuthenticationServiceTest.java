/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c)) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.security.client.test;

import java.util.List;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.account.AccountTenant;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.Status;
import org.collectionspace.services.client.AccountClient;
import org.collectionspace.services.client.AccountFactory;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionObjectFactory;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.test.BaseServiceTest;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.TitleGroup;
import org.collectionspace.services.collectionobject.TitleGroupList;
import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * AuthenticationServiceTest uses CollectionObject service to test
 * authentication
 * 
 * $LastChangedRevision: 434 $ $LastChangedDate: 2009-07-28 14:34:15 -0700 (Tue,
 * 28 Jul 2009) $
 */
public class AuthenticationServiceTest extends BaseServiceTest<AbstractCommonList> {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationServiceTest.class);
    /** The known resource id. */
    private String barneyAccountId = null; //active
    private String georgeAccountId = null; //inactive
    /** The logger. */
    private final String CLASS_NAME = AuthenticationServiceTest.class.getName();

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#getServicePathComponent()
     */
    @Override
    protected String getServicePathComponent() {
        // no need to return anything but null since no auth resources are
        // accessed
    	throw new UnsupportedOperationException();
    }

	@Override
	protected String getServiceName() {
        // no need to return anything but null since no auth resources are
        // accessed
    	throw new UnsupportedOperationException();
	}
	
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new AccountClient();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AbstractCommonList getCommonList(
            ClientResponse<AbstractCommonList> response) {
        throw new UnsupportedOperationException(); //Since this test does not support lists, this method is not needed.
    }

    @Test(dataProvider = "testName")
    public void createActiveAccount(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        AccountClient accountClient = new AccountClient();
        // This should not be needed - the auth is already set up
        //accountClient.setAuth(true, "test", true, "test", true);

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("barney", "barney08", "barney@dinoland.com",
                accountClient.getTenantId(), false);
        ClientResponse<Response> res = accountClient.create(account);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": barney status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        // Store the ID returned from this create operation
        // for additional tests below.
        barneyAccountId = extractId(res);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": barneyAccountId=" + barneyAccountId);
        }
        res.releaseConnection();

    }

    @Test(dataProvider = "testName")
    public void createInactiveAccount(String testName) throws Exception {
        // Perform setup.
        setupCreate();

        AccountClient accountClient = new AccountClient();
        // This should not be needed - the auth is already set up
        //accountClient.setAuth(true, "test", true, "test", true);

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("george", "george08", "george@curiousland.com",
                accountClient.getTenantId(), false);
        ClientResponse<Response> res = accountClient.create(account);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": george status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        // Store the ID returned from this create operation
        // for additional tests below.
        georgeAccountId = extractId(res);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": georgeAccountId=" + georgeAccountId);
        }
        res.releaseConnection();
        //deactivate
        setupUpdate();
        account.setStatus(Status.INACTIVE);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ":updated object");
            logger.debug(objectAsXmlString(account,
                    AccountsCommon.class));
        }

        // Submit the request to the service and store the response.
        ClientResponse<AccountsCommon> res1 = accountClient.update(georgeAccountId, account);
        statusCode = res1.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        res1.releaseConnection();
    }


    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#create()
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createActiveAccount"})
    public void create(String testName) {
        setupCreate();

        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setAuth(true, "barney", true, "barney08", true);
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug("create: status = " + res.getStatus());
        }
        //so it does not have any permissions out-of-the-box to create a
        //collection object
        Assert.assertEquals(res.getStatus(),
                Response.Status.FORBIDDEN.getStatusCode(), "expected "
                + Response.Status.FORBIDDEN.getStatusCode());

        // Store the ID returned from this create operation for additional tests
        // below.
        res.releaseConnection();

    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createActiveAccount"})
    public void createWithoutAuthn(String testName) {
        setupCreate();
        
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String user = collectionObjectClient.getProperty(collectionObjectClient.USER_PROPERTY);
        String pass = collectionObjectClient.getProperty(collectionObjectClient.PASSWORD_PROPERTY);
        collectionObjectClient.setAuth(false, user, true, pass, true);
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug("create: status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(),
                Response.Status.UNAUTHORIZED.getStatusCode(), "expected "
                + Response.Status.UNAUTHORIZED.getStatusCode());
        res.releaseConnection();

    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createInactiveAccount"})
    public void createWithInactiveAccount(String testName) {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setAuth(true, "george", true, "george08", true);
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);

        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(),
                Response.Status.FORBIDDEN.getStatusCode(), "expected "
                + Response.Status.FORBIDDEN.getStatusCode());
        res.releaseConnection();
    }

    /**
     * Creates the collection object instance without password.
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createActiveAccount"})
    public void createWithoutPassword(String testName) {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String user = collectionObjectClient.getProperty(collectionObjectClient.USER_PROPERTY);
        collectionObjectClient.setAuth(true, user, true, "", false);
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(), "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
        res.releaseConnection();
    }

    /**
     * Creates the collection object with unknown user
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createActiveAccount"})
    public void createWithUnknownUser(String testName) {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setAuth(true, "foo", true, "bar", true);
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(), "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
        res.releaseConnection();
    }

    /**
     * Creates the collection object instance with incorrect password.
     */
    @Test(dataProvider = "testName",
    		dependsOnMethods = {"createActiveAccount"})
    public void createWithIncorrectPassword(String testName) {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String user = collectionObjectClient.getProperty(collectionObjectClient.USER_PROPERTY);
        collectionObjectClient.setAuth(true, user, true, "bar", true);
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(), "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
        res.releaseConnection();
    }

    /**
     * Creates the collection object instance with incorrect user password.
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createActiveAccount"})
    public void createWithIncorrectUserPassword(String testName) {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setAuth(true, "foo", true, "bar", true);
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = "
                    + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(), "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
        res.releaseConnection();
    }

    /**
     * Creates the collection object instance with incorrect user password.
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createActiveAccount"})
    public void createWithoutTenant(String testName) {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setAuth(true, "babybop", true, "babybop09", true);
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = "
                    + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(), "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
        res.releaseConnection();
    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"create", "createWithInactiveAccount"})
    public void deleteAccounts(String testName) throws Exception {
        // Perform setup.
        setupDelete();
        AccountClient accountClient = new AccountClient();
        // accountClient.setAuth(true, "test", true, "test", true);
        // Submit the request to the service and store the response.
        ClientResponse<Response> res = accountClient.delete(barneyAccountId);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": barney status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));

        res = accountClient.delete(georgeAccountId);
        statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": george status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        res.releaseConnection();
    }
    
    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /**
     * Creates the collection object instance.
     *
     * @param commonPartName the common part name
     * @param identifier the identifier
     *
     * @return the multipart output
     */
    private PoxPayloadOut createCollectionObjectInstance(
            String commonPartName, String identifier) {
        return createCollectionObjectInstance(commonPartName, "objectNumber-"
                + identifier, "title-" + identifier);
    }

    /**
     * Creates the collection object instance.
     *
     * @param commonPartName the common part name
     * @param objectNumber the object number
     * @param title the object title
     *
     * @return the multipart output
     */
    private PoxPayloadOut createCollectionObjectInstance(
            String commonPartName, String objectNumber, String title) {
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();

        collectionObject.setObjectNumber(objectNumber);
        TitleGroupList titleGroupList = new TitleGroupList();
        List<TitleGroup> titleGroups = titleGroupList.getTitleGroup();
        TitleGroup titleGroup = new TitleGroup();
        titleGroup.setTitle(title);
        titleGroups.add(titleGroup);
        collectionObject.setTitleGroupList(titleGroupList);
        PoxPayloadOut multipart =
                CollectionObjectFactory.createCollectionObjectInstance(
                commonPartName, collectionObject, null, null);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, collectionobject common ",
                    collectionObject, CollectionobjectsCommon.class);
        }
        return multipart;
    }

    private AccountsCommon createAccountInstance(String screenName,
            String passwd, String email, String tenantId, boolean invalidTenant) {

        AccountsCommon account = AccountFactory.createAccountInstance(screenName,
                screenName, passwd, email, tenantId,
                true, invalidTenant, true, true);

        List<AccountTenant> atl = account.getTenants();

        //disable 2nd tenant till tenant identification is in effect
        //on the service side for 1-n user-tenants
//        AccountsCommon.Tenant at2 = new AccountsCommon.Tenant();
//        at2.setId(UUID.randomUUID().toString());
//        at2.setName("collectionspace.org");
//        atl.add(at2);
//        account.setTenants(atl);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, account common");
            logger.debug(objectAsXmlString(account,
                    AccountsCommon.class));
        }
        return account;

    }

	@Override
	protected Class<AbstractCommonList> getCommonListType() {
		// TODO Auto-generated method stub
		return null;
	}	
}
