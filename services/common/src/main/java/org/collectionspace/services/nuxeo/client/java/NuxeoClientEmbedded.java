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
import java.security.Principal;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.repository.RepositoryInstanceWrapperAdvice;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.SystemPrincipal;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.jtajca.NuxeoContainer;
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public final class NuxeoClientEmbedded {

	private Logger logger = LoggerFactory.getLogger(NuxeoClientEmbedded.class);
	
    private final HashMap<String, CoreSessionInterface> repositoryInstances;

    private RepositoryManager repositoryMgr;

    private static final NuxeoClientEmbedded instance = new NuxeoClientEmbedded();

	private static final int MAX_CREATE_TRANSACTION_ATTEMPTS = 5;
        
    /**
     * Constructs a new NuxeoClient. NOTE: Using {@link #getInstance()} instead
     * of this constructor is recommended.
     */
    private NuxeoClientEmbedded() {
        repositoryInstances = new HashMap<String, CoreSessionInterface>();
    }
    
    public static NuxeoClientEmbedded getInstance() {
        return instance;
    }

    public synchronized void tryDisconnect() throws Exception {
        doDisconnect();
    }

    private void doDisconnect() throws Exception {
        // close the open Nuxeo repository sessions if any
        Iterator<Entry<String, CoreSessionInterface>> it = repositoryInstances.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, CoreSessionInterface> repo = it.next();
            try {
                repo.getValue().close();
            } catch (Exception e) {
                logger.debug("Error while trying to close " + repo, e);
            }
            it.remove();
        }

        repositoryMgr = null;
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
    public CoreSessionInterface openRepository(RepositoryDomainType repoDomain) throws Exception {
        return openRepository(repoDomain.getRepositoryName(), ServiceContext.DEFAULT_TX_TIMEOUT);
    }
    
    /*
     * Open a Nuxeo repo session using the passed in repoDomain and use the default tx timeout period
     */
    public CoreSessionInterface openRepository(String repoName) throws Exception {
        return openRepository(repoName, ServiceContext.DEFAULT_TX_TIMEOUT);
    }
    
    private boolean startTransaction() {
    	boolean startedTransaction = false;
    	int attempts = 0;
    	
    	if (TransactionHelper.isTransactionActive() == false) {
        	while (startedTransaction == false && attempts <= MAX_CREATE_TRANSACTION_ATTEMPTS) {        		
        		try {
        			startedTransaction = TransactionHelper.startTransaction();
        		} catch (Exception e) {
        			String traceMsg = String.format("Could not start a new transaction on thread '%d'", Thread.currentThread().getId());
        			logger.trace(traceMsg);
        			boolean txState = TransactionHelper.isTransactionActive();
        			txState = TransactionHelper.isNoTransaction();
        			txState = TransactionHelper.isTransactionActiveOrMarkedRollback();
        			txState = TransactionHelper.isTransactionMarkedRollback();
        		}
        		
    	    	if (startedTransaction == false) {
    	    		long currentThreadId = Thread.currentThread().getId();
        			boolean txState = TransactionHelper.isTransactionActive();
        			txState = TransactionHelper.isNoTransaction();
        			txState = TransactionHelper.isTransactionActiveOrMarkedRollback();
        			txState = TransactionHelper.isTransactionMarkedRollback();
        			
        			if (TransactionHelper.isTransactionActiveOrMarkedRollback() == true) {
        				try {
        					TransactionHelper.commitOrRollbackTransaction();
        				} catch (Exception e) {
        					logger.error("Could not commit or rollback transaction.", e);
        				}
        			}
    	    	}
    			attempts++;
        	}
    	} else {
    		logger.warn("A request to start a new transaction was made, but a transaction is already open.");
    		startedTransaction = true;
    	}
    		
		if (startedTransaction == false) {
			String errMsg = String.format("Attempted %d time(s) to start a new transaction, but failed.", attempts);
    		logger.error(errMsg);
        }

		return startedTransaction;
    }

    public CoreSessionInterface openRepository(String repoName, int timeoutSeconds) throws Exception {
    	CoreSessionInterface result = null;
    	
    	//
    	// If the called passed in a custom timeout setting, use it to configure Nuxeo's transaction manager.
    	//
    	if (timeoutSeconds > 0) {
    		TransactionManager transactionMgr = TransactionHelper.lookupTransactionManager();
            TransactionManager tm = NuxeoContainer.getTransactionManager();
            if (logger.isDebugEnabled()) {
            	if (tm != transactionMgr) {
            		logger.debug("TransactionHelper's manager is different than NuxeoContainer's.");
            	}
            }
    		
    		transactionMgr.setTransactionTimeout(timeoutSeconds); // For the current thread only
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
    		startedTransaction = startTransaction();
	    	if (startedTransaction == false) {
	    		String errMsg = String.format("Could not start a Nuxeo transaction with the TransactionHelper class on thread '%d'.",
	    				Thread.currentThread().getId());
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
            result = getCoreSessionWrapper(repository);
            if (result != null) {
	        	logger.trace(String.format("A new transaction was started on thread '%d' : %s.",
	        			Thread.currentThread().getId(), startedTransaction ? "true" : "false"));
	        	logger.trace(String.format("Added a new repository instance to our repo list.  Current count is now: %d",
	        			repositoryInstances.size()));
            }
        }
        
        if (repository == null || result == null) {
        	//
        	// If we couldn't open a repo session, we need to close the transaction we started.
        	//
        	if (startedTransaction == true) {
        		TransactionHelper.commitOrRollbackTransaction();
        	}
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
    private CoreSessionInterface getAOPProxy(CoreSession repositoryInstance) {
    	CoreSessionInterface result = null;
    	
    	try {
			ProxyFactory factory = new ProxyFactory(new CoreSessionWrapper(repositoryInstance));
			factory.addAdvice(new RepositoryInstanceWrapperAdvice());
			factory.setExposeProxy(true);
			result = (CoreSessionInterface)factory.getProxy();
    	} catch (Exception e) {
    		logger.error("Could not create AOP proxy for: " + CoreSessionWrapper.class.getName(), e);
    	}
    	
    	return result;
    }
    
	private Principal getSystemPrincipal() {
		NuxeoPrincipal principal = new SystemPrincipal(null);
		return principal;
	}

    /*
     * From the Repository object (a description of the repository), get repository instance wrapper.  Our wrapper
     * will using the Spring AOP mechanism to intercept all calls to the repository.  We will wrap all the calls to the
     * Nuxeo repository and check for network related failures.  We will retry all calls to the Nuxeo repo that fail because
     * of network erros.
     */
    private CoreSessionInterface getCoreSessionWrapper(Repository repository) {
    	CoreSessionInterface result = null;
    	    	
    	CoreSession coreSession = null;
    	try {
    		coreSession = CoreInstance.openCoreSession(repository.getName(), getSystemPrincipal());  // A Nuxeo repo instance handler proxy
    	} catch (Exception e) {
    		logger.warn(String.format("Could not open a session to the '%s' repository.  The current request to the CollectionSpace services API will fail.",
    				repository != null ? repository.getName() : "not specified"), e);
    	}
    	
        if (coreSession != null) {
        	result = this.getAOPProxy(coreSession);  // This is our AOP proxy
        	if (result != null) {
		    	String key = result.getSessionId();
		        repositoryInstances.put(key, result);
        	} else {
        		//
        		// Since we couldn't get an AOP proxy, we need to close the core session.
        		//
        		CoreInstance.closeCoreSession(coreSession);
        		String errMsg = String.format("Could not instantiate a Spring AOP proxy for class '%s'.",
        				CoreSessionWrapper.class.getName());
        		logger.error(errMsg);
        	}
        }
    	
    	return result;
    }

    public void releaseRepository(CoreSessionInterface repoSession) throws Exception {
    	String key = repoSession.getSessionId();
    	String name = repoSession.getRepositoryName();

    	//
    	// The caller should have already called the .save() method, but just in
    	// case they didn't, let's try calling it again.
    	//
        try {
        	repoSession.save();
        } catch (Exception e) {
        	String errMsg = String.format("Possible data loss.  Could not save and/or close the Nuxeo repository name = '%s'.", name);
        	logger.trace(errMsg, e);
        	throw e;
        } finally {
        	repoSession.close();
        	CoreSessionInterface wasRemoved = repositoryInstances.remove(key);
            if (logger.isTraceEnabled()) {
            	if (wasRemoved != null) {
	            	logger.trace("Removed a repository instance from our repo list.  Current count is now: "
	            			+ repositoryInstances.size());
            	} else {
            		logger.trace("Could not remove a repository instance from our repo list.  Current count is now: "
	            			+ repositoryInstances.size());
            	}
            }            
            //
            // Last but not least, try to commit the current Nuxeo-related transaction.
            //
            if (TransactionHelper.isTransactionActiveOrMarkedRollback() == true) {
            	TransactionHelper.commitOrRollbackTransaction();
            	logger.trace(String.format("Transaction closed on thread '%d'", Thread.currentThread().getId()));
            } else {
            	String warnMsg = String.format("Closed a Nuxeo repository session on thread '%d' without closing the containing transaction.",
            			Thread.currentThread().getId());
            	logger.warn(warnMsg);
            }
        }
    }    
}
