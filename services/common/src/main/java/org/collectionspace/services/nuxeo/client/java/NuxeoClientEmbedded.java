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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.security.auth.login.AppConfigurationEntry;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.remoting.InvokerLocator;
import org.nuxeo.common.collections.ListenerList;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.api.repository.RepositoryInstanceHandler;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.client.ConnectionListener;
import org.nuxeo.ecm.core.client.DefaultLoginHandler;
import org.nuxeo.ecm.core.client.LoginHandler;
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

    private boolean multiThreadedLogin = false;

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

    public void setMultiThreadedLogin(boolean useMultiThreadedLogin) {
        multiThreadedLogin = useMultiThreadedLogin;
    }

    public boolean getMultiThreadedLogin() {
        return multiThreadedLogin;
    }

    public synchronized void connect(String locator) throws Exception {
        if (this.locator != null) {
            throw new IllegalStateException("Client is already connected");
        }
        doConnect(AutoConfigurationService.createLocator(locator));
    }

    public synchronized void connect(InvokerLocator locator) throws Exception {
        if (this.locator != null) {
            throw new IllegalStateException("Client is already connected");
        }
        doConnect(locator);
    }

    public synchronized void connect(String host, int port) throws Exception {
        if (locator != null) {
            throw new IllegalStateException("Client is already connected");
        }
        doConnect(AutoConfigurationService.createLocator(host, port));
    }

    public synchronized void forceConnect(InvokerLocator locator)
            throws Exception {
        if (this.locator != null) {
            disconnect();
        }
        doConnect(locator);
    }

    public synchronized void forceConnect(String locator) throws Exception {
        if (this.locator != null) {
            disconnect();
        }
        doConnect(AutoConfigurationService.createLocator(locator));
    }

    public synchronized void forceConnect(String host, int port)
            throws Exception {
        if (locator != null) {
            disconnect();
        }
        doConnect(AutoConfigurationService.createLocator(host, port));
    }

    public synchronized void tryConnect(String host, int port) throws Exception {
        if (locator != null) {
            return; // do nothing
        }
        doConnect(AutoConfigurationService.createLocator(host, port));
    }

    public synchronized void tryConnect(String url) throws Exception {
        if (locator != null) {
            return; // do nothing
        }
        doConnect(AutoConfigurationService.createLocator(url));
    }

    public synchronized void tryConnect(InvokerLocator locator)
            throws Exception {
        if (this.locator != null) {
            return; // do nothing
        }
        doConnect(locator);
    }

    @Deprecated
    private void doConnect(InvokerLocator locator) throws Exception {
        this.locator = locator;
        try {
            cfg.load(locator);
            // FIXME TODO workaround to work with nxruntime core 1.3.3
            // --------------
            String newPort = Framework.getProperty("org.nuxeo.runtime.1.3.3.streaming.port");
            if (newPort != null) {
                StreamingService streamingService = (StreamingService) Framework.getRuntime().getComponent(
                        StreamingService.NAME);
                // streaming config
                String oldLocator = streamingService.getServerLocator();
                int p = oldLocator.lastIndexOf(':');
                if (p > -1) {
                    String withoutPort = oldLocator.substring(0, p);
                    String serverLocator = withoutPort + ":" + newPort;
                    streamingService.stopManager();
                    streamingService.setServerLocator(serverLocator);
                    streamingService.setServer(false);
                    streamingService.startManager();
                }
            }
            // FIXME TODO workaround for remote services
            // -------------------------------
            schemaRemotingWorkaround(locator.getHost());
            // workaround for client login configuration - we need to make it
            // not multi threaded
            // TODO put an option for this in NuxeoClient
            if (!multiThreadedLogin) {
                LoginService ls = Framework.getService(LoginService.class);
                SecurityDomain sysDomain = ls.getSecurityDomain(LoginComponent.SYSTEM_LOGIN);
                SecurityDomain clientDomain = ls.getSecurityDomain(LoginComponent.CLIENT_LOGIN);
                adaptClientSecurityDomain(sysDomain);
                adaptClientSecurityDomain(clientDomain);
            }
            // ----------------
//            login();
            if (!multiThreadedLogin) {
            	throw new RuntimeException("This coode is dead and should never be called?"); //FIXME: REM - If you're reading this, this was left here by mistake and should be removed.
            }
        } catch (Exception e) {
            this.locator = null;
            throw e;
        }
        fireConnected(this);
    }

    public static void adaptClientSecurityDomain(SecurityDomain sd) {
        AppConfigurationEntry[] entries = sd.getAppConfigurationEntries();
        if (entries != null) {
            for (int i = 0; i < entries.length; i++) {
                AppConfigurationEntry entry = entries[i];
                if ("org.jboss.security.ClientLoginModule".equals(entry.getLoginModuleName())) {
                    Map<String, ?> opts = entry.getOptions();
                    Map<String, Object> newOpts = new HashMap<String, Object>(
                            opts);
                    newOpts.put("multi-threaded", "false");
                    entries[i] = new AppConfigurationEntry(
                            entry.getLoginModuleName(), entry.getControlFlag(),
                            entry.getOptions());
                }
            }
        }
    }

    /**
     * Workaround for being able to load schemas from remote
     * TODO integrate this in core
     */
    private static void schemaRemotingWorkaround(String host) throws Exception {
        ServiceManager serviceManager = Framework.getLocalService(ServiceManager.class);
        ServiceDescriptor sd = new ServiceDescriptor(TypeProvider.class, "core");
        sd.setLocator("%TypeProviderBean");
        serviceManager.registerService(sd);
        TypeProvider typeProvider = Framework.getService(TypeProvider.class);
        SchemaManager schemaMgr = Framework.getLocalService(SchemaManager.class);
        ((SchemaManagerImpl) schemaMgr).importTypes(typeProvider);
    }

    public synchronized void disconnect() throws Exception {
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

    @Deprecated
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
        // logout
//        logout();
        if (!multiThreadedLogin) {
        	throw new RuntimeException("This coode is dead and should never be called?"); //FIXME: REM - If you're reading this, this was left here by mistake and should be removed.
        }
        repositoryMgr = null;
        fireDisconnected(this);
    }

    public synchronized void reconnect() throws Exception {
        if (locator == null) {
            throw new IllegalStateException("Client is not connected");
        }
        InvokerLocator locator = this.locator;
        disconnect();
        connect(locator);
    }

    public AutoConfigurationService getConfigurationService() {
        return cfg;
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

    public String getServerHost() {
        if (locator == null) {
            throw new IllegalStateException("Client is not connected");
        }
        return locator.getHost();
    }

    public int getServerPort() {
        if (locator == null) {
            throw new IllegalStateException("Client is not connected");
        }
        return locator.getPort();
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

    public RepositoryInstance openRepository(int timeoutSeconds) throws Exception {
        return openRepository(null, timeoutSeconds);
    }

    public RepositoryInstance openRepository() throws Exception {
        return openRepository(-1 /*default timeout period*/);
    }

    public RepositoryInstance openRepository(String name, int timeoutSeconds) throws Exception {
    	if (timeoutSeconds > 0) {
    		TransactionManager transactionMgr = TransactionHelper.lookupTransactionManager();
    		transactionMgr.setTransactionTimeout(timeoutSeconds);
    		if (logger.isInfoEnabled()) {
    			logger.info(String.format("Changing current request's transaction timeout period to %d seconds",
    					timeoutSeconds));
    		}
    	}
    	
    	boolean startTransaction = TransactionHelper.startTransaction();
    	if (startTransaction == false) {
    		logger.warn("Could not start a Nuxeo transaction with the TransactionHelper class.");
    	}
    	
        Repository repository = null;
        if (name != null) {
        	repository = getRepositoryManager().getRepository(name);
        } else {
        	repository = getRepositoryManager().getDefaultRepository();
        }
        RepositoryInstance repo = newRepositoryInstance(repository);
    	String key = repo.getSessionId();
        repositoryInstances.put(key, repo);
        if (logger.isTraceEnabled()) {
        	logger.trace("Added a new repository instance to our repo list.  Current count is now: "
        			+ repositoryInstances.size());
        }

        return repo;
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
            TransactionHelper.commitOrRollbackTransaction();
        }
    }

//    public RepositoryInstance[] getRepositoryInstances() {
//        return repositoryInstances.toArray(new RepositoryInstance[repositoryInstances.size()]);
//    }

    public static RepositoryInstance newRepositoryInstance(Repository repository) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = NuxeoClientEmbedded.class.getClassLoader();
        }
        return new RepositoryInstanceHandler(repository).getProxy();
    }

    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    private void fireDisconnected(NuxeoClientEmbedded client) {
        Object[] listeners = connectionListeners.getListeners();
        for (Object listener : listeners) {
//            ((ConnectionListener) listener).disconnected(client);
        }
    }

    private void fireConnected(NuxeoClientEmbedded client) {
        Object[] listeners = connectionListeners.getListeners();
        for (Object listener : listeners) {
//            ((ConnectionListener) listener).connected(client);
        }
    }    
}
