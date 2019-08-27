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

import java.util.Date;
import java.util.UUID;

import javax.persistence.Query;

import org.collectionspace.services.authentication.User;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UserStorageClient manages persistence for CS IdP
 * Note: this class is always used by the AccountStorageClient which provides
 * access to entity manager
 * @author
 */
public class UserStorageClient {

    private final Logger logger = LoggerFactory.getLogger(UserStorageClient.class);

    /**
     * create user with given userId and password
     * @param userId
     * @param password
     * @return user
     */
    public User create(String userId, byte[] password) throws Exception {
        User user = new User();
        user.setUsername(userId);
        String salt = UUID.randomUUID().toString();
        user.setPasswd(getEncPassword(userId, password, salt));
        user.setSalt(salt);
        user.setCreatedAtItem(new Date());
        return user;
    }

    /**
     * getUser get user for given userId
     * @param em EntityManager
     * @param userId
     */
    public User get(JPATransactionContext jpaTransactionContext, String userId) throws DocumentNotFoundException {
        User userFound = (User) jpaTransactionContext.find(User.class, userId);
        if (userFound == null) {
            String msg = "Could not find user with userId=" + userId;
            logger.error(msg);
            throw new DocumentNotFoundException(msg);
        }
        
        return userFound;
    }
    
    @SuppressWarnings("rawtypes")
	public User get(ServiceContext ctx, String userId) throws DocumentNotFoundException, TransactionException {
    	User userFound = null;
    	
    	JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();
    	try {
	        userFound = (User) jpaConnectionContext.find(User.class, userId);
	        if (userFound == null) {
	            String msg = "could not find user with userId=" + userId;
	            logger.error(msg);
	            throw new DocumentNotFoundException(msg);
	        }
    	} finally {
    		ctx.closeConnection();
    	}
        
        return userFound;
    }    

    /**
     * updateUser for given userId
     * @param entity manager
     * @param userId
     * @param password
     */
    public void update(JPATransactionContext jpaTransactionContext, String userId, byte[] password)
            throws DocumentNotFoundException, Exception {
        User userFound = get(jpaTransactionContext, userId);
        if (userFound != null) {
            userFound.setPasswd(getEncPassword(userId, password, userFound.getSalt()));
            userFound.setUpdatedAtItem(new Date());
            if (logger.isDebugEnabled()) {
                logger.debug("updated user=" + JaxbUtils.toString(userFound, User.class));
            }
            jpaTransactionContext.persist(userFound);
        }
    }

    /**
     * delete deletes user with given userId
     * @param em entity manager
     * @param userId
     * @throws Exception if user for given userId not found
     */
    public void delete(JPATransactionContext jpaTransactionContext, String userId)
            throws DocumentNotFoundException, Exception {
        //if userid gives any indication about the id provider, it should
        //be used to avoid the following approach
        StringBuilder usrDelStr = new StringBuilder("DELETE FROM ");
        usrDelStr.append(User.class.getCanonicalName());
        usrDelStr.append(" WHERE username = :username");
        //TODO: add tenant id
        Query usrDel = jpaTransactionContext.createQuery(usrDelStr.toString());
        usrDel.setParameter("username", userId);
        int usrDelCount = usrDel.executeUpdate();
        if (usrDelCount != 1) {
            String msg = "could not find user with username=" + userId;
            logger.error(msg);
            throw new DocumentNotFoundException(msg);
        }
    }

    private String getEncPassword(String userId, byte[] password, String salt) throws BadRequestException {
        //jaxb unmarshaller already unmarshal xs:base64Binary, no need to b64 decode
        //byte[] bpass = Base64.decodeBase64(accountReceived.getPassword());
        try {
            SecurityUtils.validatePassword(new String(password));
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
        String secEncPasswd = SecurityUtils.createPasswordHash(
                userId, new String(password), salt);
        return secEncPasswd;
    }
}
