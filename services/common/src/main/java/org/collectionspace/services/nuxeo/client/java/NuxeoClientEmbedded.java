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
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.login.AppConfigurationEntry;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.jboss.remoting.InvokerLocator;
import org.nuxeo.common.collections.ListenerList;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.api.repository.RepositoryInstanceHandler;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.client.ConnectionListener;
import org.nuxeo.ecm.core.client.DefaultLoginHandler;
import org.nuxeo.ecm.core.client.LoginHandler;
//import org.nuxeo.ecm.core.repository.RepositoryDescriptor;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
import org.nuxeo.ecm.core.schema.TypeProvider;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.api.ServiceDescriptor;
import org.nuxeo.runtime.api.ServiceManager;
import org.nuxeo.runtime.api.login.LoginComponent;
import org.nuxeo.runtime.api.login.LoginService;
import org.nuxeo.runtime.api.login.SecurityDomain;
import org.nuxeo.runtime.config.AutoConfigurationService;
import org.nuxeo.runtime.remoting.RemotingService;
import org.nuxeo.runtime.services.streaming.StreamingService;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public final class NuxeoClientEmbedded {

	private Logger logger = LoggerFactory.getLogger(NuxeoClientEmbedded.class);
	
    private LoginHandler loginHandler;

    private final HashMap<String, RepositoryInstance> repositoryInstances;

    private final ListenerList connectionListeners;

    private InvokerLocator locator;

    private String serverName;

    private final AutoConfigurationService cfg;

    private RepositoryManager repositoryMgr;

    private static final NuxeoClientEmbedded instance = new NuxeoClientEmbedded();

    private static final Log log = LogFactory.getLog(NuxeoClientEmbedded.class);

    /**
     * Constructs a new NuxeoClient. NOTE: Using {@link #getInstance()} instead
     * of this constructor is recommended.
     */
    public NuxeoClientEmbedded() {
        connectionListeners = new ListenerList();
        cfg = new AutoConfigurationService();
        loginHandler = loginHandler == null ? new DefaultLoginHandler()
                : loginHandler;
        repositoryInstances = new HashMap<String, RepositoryInstance>();
    }

    public static NuxeoClientEmbedded getInstance() {
        return instance;
    }

    @Deprecated
    private synchronized void disconnect() throws Exception {
        if (locator == null) {
            throw new IllegalStateException("Client is not connected");
        }
        doDisconnect();
    }

    public synchronized void tryDisconnect() throws Exception {
        if (locator == null) {
            return; // do nothing
        }
        doDisconnect();
    }

    private void doDisconnect() throws Exception {
        locator = null;
        serverName = null;
        // close repository sessions if any
        Iterator<Entry<String, RepositoryInstance>> it = repositoryInstances.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, RepositoryInstance> repo = it.next();
            try {
                repo.getValue().close();
            } catch (Exception e) {
                log.debug("Error while trying to close " + repo, e);
            }
            it.remove();
        }

        repositoryMgr = null;
    }

    public synchronized String getServerName() {
        if (locator == null) {
            throw new IllegalStateException("Client is not connected");
        }
        if (serverName == null) {
            if (cfg == null) { // compatibility
                serverName = RemotingService.ping(locator.getHost(),
                        locator.getPort());
            } else {
                serverName = cfg.getServerConfiguration().getProductInfo();
            }
        }
        return serverName;
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
    public RepositoryInstance openRepository(RepositoryDomainType repoDomain) throws Exception {
        return openRepository(repoDomain.getRepositoryName(), -1);
    }
    
    /*
     * Open a Nuxeo repo session using the passed in repoDomain and use the default tx timeout period
     */
    public RepositoryInstance openRepository(String repoName) throws Exception {
        return openRepository(repoName, -1);
    }    

    /*
     * Open a Nuxeo repo session using the default repo with the specified (passed in) tx timeout period
     */
    @Deprecated
    public RepositoryInstance openRepository(int timeoutSeconds) throws Exception {
        return openRepository(null, timeoutSeconds);
    }

    /*
     * Open a Nuxeo repo session using the default repo with the default tx timeout period
     */
    @Deprecated
    public RepositoryInstance openRepository() throws Exception {
        return openRepository(null, -1 /*default timeout period*/);
    }

    public RepositoryInstance openRepository(String repoName, int timeoutSeconds) throws Exception {
    	RepositoryInstance result = null;
    	
    	if (timeoutSeconds > 0) {
    		TransactionManager transactionMgr = TransactionHelper.lookupTransactionManager();
    		transactionMgr.setTransactionTimeout(timeoutSeconds);
    		if (logger.isInfoEnabled()) {
    			logger.info(String.format("Changing current request's transaction timeout period to %d seconds",
    					timeoutSeconds));
    		}
    	}
    	
    	//  REM 10/29/2013 - We may want to add this clause if (!TransactionHelper.isTransactionActive()) {
    	boolean startTransaction = TransactionHelper.startTransaction();
    	if (startTransaction == false) {
    		logger.warn("Could not start a Nuxeo transaction with the TransactionHelper class.");
    	}
    	
        Repository repository = null;
        if (repoName != null) {
        	repository = getRepositoryManager().getRepository(repoName);
        } else {
        	repository = getRepositoryManager().getDefaultRepository(); // Add a log info statement here stating that since no repo name was given we'll use the default repo instead
        }
        
        if (repository != null) {
	        result = newRepositoryInstance(repository);
	    	String key = result.getSessionId();
	        repositoryInstances.put(key, result);
	        if (logger.isTraceEnabled()) {
	        	logger.trace("Added a new repository instance to our repo list.  Current count is now: "
	        			+ repositoryInstances.size());
	        }
        } else {
        	String errMsg = String.format("Could not open a session to the Nuxeo repository='%s'", repoName);
        	logger.error(errMsg);
        	throw new Exception(errMsg);
        }

        return result;
    }

    public void releaseRepository(RepositoryInstance repo) throws Exception {
    	String key = repo.getSessionId();

        try {
        	repo.save();
            repo.close();
        } catch (Exception e) {
        	logger.error("Possible data loss.  Could not save and/or release the repository.", e);
        	throw e;
        } finally {
            RepositoryInstance wasRemoved = repositoryInstances.remove(key);
            if (logger.isTraceEnabled()) {
            	if (wasRemoved != null) {
	            	logger.trace("Removed a repository instance from our repo list.  Current count is now: "
	            			+ repositoryInstances.size());
            	} else {
            		logger.trace("Could not remove a repository instance from our repo list.  Current count is now: "
	            			+ repositoryInstances.size());
            	}
            }
            //if (TransactionHelper.isTransactionActiveOrMarkedRollback())
            TransactionHelper.commitOrRollbackTransaction();
        }
    }

    public static RepositoryInstance newRepositoryInstance(Repository repository) {
        return new RepositoryInstanceHandler(repository).getProxy(); // Why a proxy here?
    }

}
