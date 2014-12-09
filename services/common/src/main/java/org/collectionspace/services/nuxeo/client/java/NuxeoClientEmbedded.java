/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu, jcarsique
 *
 * $Id$
 */

package org.collectionspace.services.nuxeo.client.java;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.collectionspace.services.common.repository.RepositoryInstanceWrapperAdvice;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.jboss.remoting.InvokerLocator;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.api.repository.RepositoryInstanceHandler;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.client.DefaultLoginHandler;
import org.nuxeo.ecm.core.client.LoginHandler;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public final class NuxeoClientEmbedded {

	private Logger logger = LoggerFactory.getLogger(NuxeoClientEmbedded.class);
	
    private LoginHandler loginHandler;

    private final HashMap<String, RepositoryInstanceInterface> repositoryInstances;

    private InvokerLocator locator;

    private RepositoryManager repositoryMgr;

    private static final NuxeoClientEmbedded instance = new NuxeoClientEmbedded();
        
    /**
     * Constructs a new NuxeoClient. NOTE: Using {@link #getInstance()} instead
     * of this constructor is recommended.
     */
    private NuxeoClientEmbedded() {
        loginHandler = loginHandler == null ? new DefaultLoginHandler()
                : loginHandler;
        repositoryInstances = new HashMap<String, RepositoryInstanceInterface>();
    }
    
    public static NuxeoClientEmbedded getInstance() {
        return instance;
    }

    public synchronized void tryDisconnect() throws Exception {
        if (locator == null) {
            return; // do nothing
        }
        doDisconnect();
    }

    private void doDisconnect() throws Exception {
        locator = null;
        // close repository sessions if any
        Iterator<Entry<String, RepositoryInstanceInterface>> it = repositoryInstances.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, RepositoryInstanceInterface> repo = it.next();
            try {
                repo.getValue().close();
            } catch (Exception e) {
                logger.debug("Error while trying to close " + repo, e);
            }
            it.remove();
        }

        repositoryMgr = null;
    }

    public synchronized boolean isConnected() {
        return true;
    }

    public InvokerLocator getLocator() {
        return locator;
    }

    public synchronized LoginHandler getLoginHandler() {
        return loginHandler;
    }

    public synchronized void setLoginHandler(LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
    }

    public RepositoryManager getRepositoryManager() throws Exception {
        if (repositoryMgr == null) {
            repositoryMgr = Framework.getService(RepositoryManager.class);
        }
        return repositoryMgr;
    }

    /**
     * Gets the repositories available on the connected server.
     *
     * @return the repositories
     */
    public Repository[] getRepositories() throws Exception {
        Collection<Repository> repos = getRepositoryManager().getRepositories();
        return repos.toArray(new Repository[repos.size()]);
    }

    public Repository getDefaultRepository() throws Exception {
        return getRepositoryManager().getDefaultRepository();
    }

    public Repository getRepository(String name) throws Exception {
        return getRepositoryManager().getRepository(name);
    }

    /*
     * Open a Nuxeo repo session using the passed in repoDomain and use the default tx timeout period
     */
    public RepositoryInstanceInterface openRepository(RepositoryDomainType repoDomain) throws Exception {
        return openRepository(repoDomain.getRepositoryName(), -1);
    }
    
    /*
     * Open a Nuxeo repo session using the passed in repoDomain and use the default tx timeout period
     */
    public RepositoryInstanceInterface openRepository(String repoName) throws Exception {
        return openRepository(repoName, -1);
    }    

    public RepositoryInstanceInterface openRepository(String repoName, int timeoutSeconds) throws Exception {
    	RepositoryInstanceInterface result = null;
    	
    	//
    	// If the called passed in a custom timeout setting, use it to configure Nuxeo's transaction manager.
    	//
    	if (timeoutSeconds > 0) {
    		TransactionManager transactionMgr = TransactionHelper.lookupTransactionManager();
    		transactionMgr.setTransactionTimeout(timeoutSeconds);
    		if (logger.isInfoEnabled()) {
    			logger.info(String.format("Changing current request's transaction timeout period to %d seconds",
    					timeoutSeconds));
    		}
    	}
    	
    	//
    	// Start a new Nuxeo transaction
    	//
    	boolean startedTransaction = false;
    	if (TransactionHelper.isTransactionActive() == false) {
	    	startedTransaction = TransactionHelper.startTransaction();
	    	if (startedTransaction == false) {
	    		String errMsg = "Could not start a Nuxeo transaction with the TransactionHelper class.";
	    		logger.error(errMsg);
	    		throw new Exception(errMsg);
	    	}
    	} else {
    		logger.warn("A request to start a new transaction was made, but a transaction is already open.");
    	}
    	
    	//
    	// From the repository name that the caller passed in, get an instance of Nuxeo's Repository class.
    	// The Repository class is just a metadata description of the repository.
    	//
        Repository repository;
        if (repoName != null) {
        	repository = getRepositoryManager().getRepository(repoName);
        } else {
        	repository = getRepositoryManager().getDefaultRepository();
        	logger.warn(String.format("Using default repository '%s' because no name was specified.", repository.getName()));
        }
        
        //
        // Using the Repository class, get a Spring AOP proxied instance.  We use Spring AOP to "wrap" all calls to the
        // Nuxeo repository so we can check for network related failures and perform a series of retries.
        //
        if (repository != null) {
            result = getRepositoryInstanceWrapper(repository);
        	logger.trace(String.format("A new transaction was started on thread '%d' : %s.",
        			Thread.currentThread().getId(), startedTransaction ? "true" : "false"));
        	logger.trace(String.format("Added a new repository instance to our repo list.  Current count is now: %d",
        			repositoryInstances.size()));
        } else {
        	String errMsg = String.format("Could not open a session to the Nuxeo repository='%s'", repoName);
        	logger.error(errMsg);
        	throw new Exception(errMsg);
        }    	
    	
        
        return result;
    }
    
    //
    // Returns a proxied interface to a Nuxeo repository instance.  Our proxy uses Spring AOP to
    // wrap each call to the Nuxeo repo with code that catches network related errors/exceptions and
    // re-attempts the calls to see if it recovers.
    //
    private RepositoryInstanceInterface getAOPProxy(RepositoryInstance repositoryInstance) {
    	RepositoryInstanceInterface result = null;
    	
    	try {
			ProxyFactory factory = new ProxyFactory(new RepositoryInstanceWrapper(repositoryInstance));
			factory.addAdvice(new RepositoryInstanceWrapperAdvice());
			factory.setExposeProxy(true);
			result = (RepositoryInstanceInterface)factory.getProxy();
    	} catch (Exception e) {
    		logger.error("Could not create AOP proxy for: " + RepositoryInstanceWrapper.class.getName(), e);
    	}
    	
    	return result;
    }
    
    /*
     * From the Repository object (a description of the repository), get repository instance wrapper.  Our wrapper
     * will using the Spring AOP mechanism to intercept all calls to the repository.  We will wrap all the calls to the
     * Nuxeo repository and check for network related failures.  We will retry all calls to the Nuxeo repo that fail because
     * of network erros.
     */
    private RepositoryInstanceInterface getRepositoryInstanceWrapper(Repository repository) throws Exception {
    	RepositoryInstanceInterface result = null;
    	    	
    	RepositoryInstance repositoryInstance = new RepositoryInstanceHandler(repository).getProxy();  // A Nuxeo repo instance handler proxy
        if (repositoryInstance != null) {
        	result = this.getAOPProxy(repositoryInstance);  // This is our AOP proxy
        	if (result != null) {
		    	String key = result.getSessionId();
		        repositoryInstances.put(key, result);
        	} else {
        		String errMsg = String.format("Could not instantiate a Spring AOP proxy for class '%s'.",
        				RepositoryInstanceWrapper.class.getName());
        		logger.error(errMsg);
        		throw new Exception(errMsg);
        	}
        } else {
        	String errMsg = String.format("Could not create a new repository instance for '%s' repository.", repository.getName());
        	logger.error(errMsg);
        	throw new Exception(errMsg);
        }
    	
    	return result;
    }

    public void releaseRepository(RepositoryInstanceInterface repo) throws Exception {
    	String key = repo.getSessionId();

        try {
        	repo.save();
            repo.close();
        } catch (Exception e) {
        	logger.error("Possible data loss.  Could not save and/or release the repository.", e);
        	throw e;
        } finally {
            RepositoryInstanceInterface wasRemoved = repositoryInstances.remove(key);
            if (logger.isTraceEnabled()) {
            	if (wasRemoved != null) {
	            	logger.trace("Removed a repository instance from our repo list.  Current count is now: "
	            			+ repositoryInstances.size());
            	} else {
            		logger.trace("Could not remove a repository instance from our repo list.  Current count is now: "
	            			+ repositoryInstances.size());
            	}
            }
            if (TransactionHelper.isTransactionActiveOrMarkedRollback() == true) {
            	TransactionHelper.commitOrRollbackTransaction();
            	logger.trace(String.format("Transaction closed on thread '%d'", Thread.currentThread().getId()));
            }
        }
    }    
}
