/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.authentication.test;

import java.lang.reflect.Method;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import javax.persistence.Query;
import org.collectionspace.services.authentication.User;
import org.collectionspace.services.authentication.Role;
import org.collectionspace.services.authentication.UserRole;

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
public class DefaultIdentityProviderTest {

    private final Logger logger = LoggerFactory.getLogger(DefaultIdentityProviderTest.class);
    private EntityManagerFactory emf;
    private EntityManager em;
    private String id;

    @BeforeMethod
    public void init() {

        emf = Persistence.createEntityManagerFactory("org.collectionspace.services.authentication");

        em = emf.createEntityManager();
//        if (logger.isDebugEnabled()) {
//            logger.debug("created entity manager");
//        }
    }

    @AfterMethod
    public void cleanup() {
        if (em != null) {
            em.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "testName", dataProviderClass = DefaultIdentityProviderTest.class)
    public void create(String testName) throws Exception {
        User user = new User();
        user.setUsername("sanjay");
        user.setPasswd("uiouio");
        em.getTransaction().begin();
        em.persist(user);
        // Commit the transaction
        em.getTransaction().commit();
        if (logger.isDebugEnabled()) {
            logger.debug("created user " +
                    " username=" + user.getUsername() +
                    " password=" + user.getPasswd());
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "testName", dataProviderClass = DefaultIdentityProviderTest.class,
    dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        User user = findUser("sanjay");
        Assert.assertNotNull(user);
        if (logger.isDebugEnabled()) {
            logger.debug("read user " +
                    " username=" + user.getUsername());
        }
    }

    private User findUser(String userName) throws Exception {
        Query q = em.createQuery("select a from org.collectionspace.services.authentication.User a where a.username = :username");
        q.setParameter("username", userName);
        return (User) q.getSingleResult();

    }

//    @SuppressWarnings("unchecked")
//    @Test(dataProvider = "testName", dataProviderClass = DefaultIdentityProviderTest.class,
//    dependsOnMethods = {"read"})
//    public void update(String testName) throws Exception {
//        Query q = em.createQuery("update org.collectionspace.services.authentication.User set email= :email where csid=:csid");
//        q.setParameter("email", "sanjay@berkeley.edu");
//        q.setParameter("csid", id);
//        em.getTransaction().begin();
//        int no = q.executeUpdate();
//        // Commit the transaction
//        em.getTransaction().commit();
//        Assert.assertEquals(no, 1);
//        Users account = findAccount("sanjay");
//        if (logger.isDebugEnabled()) {
//            logger.debug("updated account " +
//                    " first name=" + account.getFirstName() +
//                    " email=" + account.getEmail());
//        }
//    }
    @SuppressWarnings("unchecked")
    @Test(dataProvider = "testName", dataProviderClass = DefaultIdentityProviderTest.class,
    dependsOnMethods = {"read"}) //FIXME change to update
    public void delete(String testName) throws Exception {
        Query q = em.createQuery("delete from org.collectionspace.services.authentication.User where username=:username");
        q.setParameter("username", "sanjay");
        // Begin transaction
        em.getTransaction().begin();
        int no = q.executeUpdate();
        ;
        if (logger.isDebugEnabled()) {
            logger.debug("deleting user " +
                    " username=" + "sanjay");
        }
        // Commit the transaction
        em.getTransaction().commit();
        Assert.assertEquals(no, 1);
        if (logger.isDebugEnabled()) {
            logger.debug("deleted user " +
                    " username=" + "sanjay");
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
