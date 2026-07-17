/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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
package org.collectionspace.services.account.storage.csidp;

import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.collectionspace.services.authentication.Token;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.storage.TransactionContext;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages db access for Token associated with a password reset request
 */
public class TokenStorageClient {

    private static final Logger logger = LoggerFactory.getLogger(TokenStorageClient.class);

    /**
     * create user with given userId and password
     * @param userId
     * @param password
     * @return user
     */
    public static Token create(String accountCsid, String tenantId, BigInteger expireSeconds) {
        EntityManagerFactory emf = JpaStorageUtils.getEntityManagerFactory();
	    Token token = new Token();

        try {
            EntityManager em = emf.createEntityManager();

    		token.setId(UUID.randomUUID().toString());
    		token.setAccountCsid(accountCsid);
    		token.setTenantId(tenantId);
    		token.setExpireSeconds(expireSeconds);
    		token.setEnabled(true);
    		token.setCreatedAt(new Date());

            em.getTransaction().begin();
    		em.persist(token);
            em.getTransaction().commit();

        } finally {
            if (emf != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }

        return token;
	}

    /**
     * Update a token for given an id
     * @param id
     * @param enabledFlag
     * @throws TransactionException
     */
    public static void update(TransactionContext transactionContext, String id, boolean enabledFlag) throws DocumentNotFoundException, TransactionException {
        Token tokenFound = null;

        tokenFound = get((JPATransactionContext)transactionContext, id);
        if (tokenFound != null) {
            tokenFound.setEnabled(enabledFlag);
            tokenFound.setUpdatedAt(new Date());
            if (logger.isDebugEnabled()) {
                logger.debug("Updated token=" + JaxbUtils.toString(tokenFound, Token.class));
            }
        } else {
        	String msg = String.format("Could not find token with id='%s'", id);
        	throw new DocumentNotFoundException(msg);
        }
    }

    /**
     * Get token for given ID
     * @param em EntityManager
     * @param id
     */
    public static Token get(JPATransactionContext jpaTransactionContext, String id) throws DocumentNotFoundException, TransactionException {
        Token tokenFound = null;

        tokenFound = (Token) jpaTransactionContext.find(Token.class, id);
        if (tokenFound == null) {
            String msg = "Could not find token with ID=" + id;
            logger.error(msg);
            throw new DocumentNotFoundException(msg);
        }

        return tokenFound;
    }

    public static Token get(String id) throws DocumentNotFoundException {
        Token tokenFound = null;
        EntityManagerFactory emf = JpaStorageUtils.getEntityManagerFactory();

        try {
            EntityManager em = emf.createEntityManager();
            tokenFound = (Token) em.find(Token.class, id);
            if (tokenFound == null) {
                String msg = "Could not find token with ID=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
        } finally {
            if (emf != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }

        return tokenFound;
    }

	/**
     * Deletes the token with given id
     * @param id
     * @throws Exception if user for given userId not found
     */
    public static void delete(String id) throws DocumentNotFoundException {
        EntityManagerFactory emf = JpaStorageUtils.getEntityManagerFactory();

        try {
            EntityManager em = emf.createEntityManager();

            StringBuilder tokenDelStr = new StringBuilder("DELETE FROM ");
	        tokenDelStr.append(Token.class.getCanonicalName());
	        tokenDelStr.append(" WHERE id = :id");

	        Query tokenDel = em.createQuery(tokenDelStr.toString());
	        tokenDel.setParameter("id", id);
	        int tokenDelCount = tokenDel.executeUpdate();
	        if (tokenDelCount != 1) {
	            String msg = "Could not find token with id=" + id;
	            logger.error(msg);
	            throw new DocumentNotFoundException(msg);
	        }
        } finally {
            if (emf != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }

}
