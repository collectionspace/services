/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.account.client.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import javax.persistence.Query;
import org.collectionspace.services.account.AccountTenant;
import org.collectionspace.services.account.AccountsCommon;
//import org.collectionspace.services.account.Tenant;
import org.collectionspace.services.account.Status;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;

/**
 *
 * @author 
 */
public class AccountTest {

    private final Logger logger = LoggerFactory.getLogger(AccountTest.class);
    private EntityManagerFactory emf;
    private EntityManager em;
    private String id;

    @BeforeMethod
    public void init() {
        emf = Persistence.createEntityManagerFactory("org.collectionspace.services.account");
        em = emf.createEntityManager();
    }

    @AfterMethod
    public void cleanup() {
        if (em != null) {
            em.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "testName", dataProviderClass = AccountTest.class)
    public void create(String testName) throws Exception {
        AccountsCommon account = new AccountsCommon();
        account.setScreenName("john");
        account.setEmail("john.doe@berkeley.edu");
        account.setUserId("johndoe");
        account.setStatus(Status.ACTIVE);
        id = UUID.randomUUID().toString();
        account.setCsid(id);
        account.setCreatedAtItem(new Date());
        AccountTenant tenant = new AccountTenant();
        tenant.setTenantId("123");
        List<AccountTenant> tList = new ArrayList<AccountTenant>();
        tList.add(tenant);
        account.setTenants(tList);
        em.getTransaction().begin();
        em.persist(account);
        // Commit the transaction
        em.getTransaction().commit();
        if (logger.isDebugEnabled()) {
            logger.debug("created account "
                    + " screen name=" + account.getScreenName()
                    + " email=" + account.getEmail());
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "testName", dataProviderClass = AccountTest.class,
    dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        AccountsCommon account = findAccount("john");
        Assert.assertNotNull(account);
        if (logger.isDebugEnabled()) {
            logger.debug("read account "
                    + " screen name=" + account.getScreenName());
        }
    }

    private AccountsCommon findAccount(String screenName) throws Exception {
        Query q = em.createQuery("select a from org.collectionspace.services.account.AccountsCommon a where a.screenName = :screenname");
        q.setParameter("screenname", screenName);
        return (AccountsCommon) q.getSingleResult();

    }

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "testName", dataProviderClass = AccountTest.class,
    dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {
        Query q = em.createQuery("update org.collectionspace.services.account.AccountsCommon set email= :email where csid=:csid");
        q.setParameter("email", "john@berkeley.edu");
        q.setParameter("csid", id);
        em.getTransaction().begin();
        int no = q.executeUpdate();
        // Commit the transaction
        em.getTransaction().commit();
        Assert.assertEquals(no, 1);
        AccountsCommon account = findAccount("john");
        if (logger.isDebugEnabled()) {
            logger.debug("updated account "
                    + " screen name=" + account.getScreenName()
                    + " email=" + account.getEmail());
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "testName", dataProviderClass = AccountTest.class,
    dependsOnMethods = {"update"})
    public void delete(String testName) throws Exception {
        // Begin transaction
        em.getTransaction().begin();
        AccountsCommon account = findAccount("john");
        em.remove(account);
        if (logger.isDebugEnabled()) {
            logger.debug("deleting account "
                    + " csid=" + id);
        }
        // Commit the transaction
        em.getTransaction().commit();
        if (logger.isDebugEnabled()) {
            logger.debug("deleted account "
                    + " csid=" + id);
        }
    }

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
}
