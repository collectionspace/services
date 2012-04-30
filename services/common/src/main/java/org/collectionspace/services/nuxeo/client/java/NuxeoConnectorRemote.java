package org.collectionspace.services.nuxeo.client.java;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.collectionspace.services.config.RepositoryClientConfigType;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.client.DefaultLoginHandler;
import org.nuxeo.ecm.core.client.NuxeoApp;
import org.nuxeo.ecm.core.client.NuxeoClient;
import org.nuxeo.osgi.application.FrameworkBootstrap;
import org.nuxeo.runtime.api.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuxeoConnectorRemote {
	public FrameworkBootstrap fb;
	public final static String NUXEO_CLIENT_DIR = "nuxeo-client";
	private Logger logger = LoggerFactory.getLogger(NuxeoConnectorRemote.class);
	private static final NuxeoConnectorRemote self = new NuxeoConnectorRemote();
	private static final String NUXEO_CLIENT_USERNAME = "NUXEO_CLIENT_USERNAME";
	private static final String NUXEO_CLIENT_PASSWORD = "NUXEO_CLIENT_PASSWORD";
	private NuxeoClient client;
	private volatile boolean initialized = false; // use volatile for lazy
													// initialization in
													// singleton
	private RepositoryClientConfigType repositoryClientConfig;

	private NuxeoConnectorRemote() {
	}

	public final static NuxeoConnectorRemote getInstance() {
		return self;
	}

	private String getClientUserName(
			RepositoryClientConfigType repositoryClientConfig) {
		String username = System.getenv(NUXEO_CLIENT_USERNAME);
		if (username == null) {
			username = repositoryClientConfig.getUser();
		}
		return username;
	}

	private String getClientPassword(
			RepositoryClientConfigType repositoryClientConfig) {
		String password = System.getenv(NUXEO_CLIENT_PASSWORD);
		if (password == null) {
			password = repositoryClientConfig.getPassword();
		}
		return password;
	}

	private void startNuxeoEP() throws Exception {
		String nuxeoHome = "nuxeo-client/";
		String serverRootDir = System.getProperty("jboss.server.home.dir");
		if (serverRootDir == null) {
			serverRootDir = "."; // assume server is started from server root,
									// e.g. server/cspace
		}
		File nuxeoHomeDir = new File(serverRootDir + File.separator + nuxeoHome);
		logger.info("Loading Nuxeo configuration from: "
				+ nuxeoHomeDir.getAbsolutePath());
		if (nuxeoHomeDir.exists() == false) {
			String msg = "Library bundles requried to deploy Nuxeo client not found: "
					+ " directory named nuxeo-client with bundles does not exist in "
					+ serverRootDir;
			logger.error(msg);
			throw new IllegalStateException(msg);
		}
		fb = new FrameworkBootstrap(NuxeoConnectorRemote.class.getClassLoader(),
				nuxeoHomeDir);
		fb.initialize();
		fb.start();
	}

	/**
	 * release releases resources occupied by Nuxeo remoting client runtime
	 * 
	 * @throws java.lang.Exception
	 */
	public void release() throws Exception {
		if (initialized == true) {
			try {
				client.tryDisconnect();
			} catch (Exception e) {
				logger.error("Failed to disconnect Nuxeo connection.", e);
				throw e;
			}
		}
	}

	public void initialize(RepositoryClientConfigType repositoryClientConfig)
			throws Exception {
		if (initialized == false) {
			synchronized (this) {
				if (initialized == false) {
					try {
						this.repositoryClientConfig = repositoryClientConfig;
						startNuxeoEP();
						// client = NuxeoClient.getInstance();
						client = new NuxeoClient();
						String username = getClientUserName(repositoryClientConfig);
						String password = getClientPassword(repositoryClientConfig);
						DefaultLoginHandler loginHandler = new DefaultLoginHandler(
								username, password);
						client.setLoginHandler(loginHandler);
						client.tryConnect(repositoryClientConfig.getHost(),
								repositoryClientConfig.getPort());
						if (logger.isDebugEnabled()) {
							logger.debug("initialize(): connection successful port="
									+ repositoryClientConfig.getPort());
						}
						initialized = true;
					} catch (Exception e) {
						if (logger.isDebugEnabled()) {
							logger.debug("Caught exception while initializing",
									e);
						}
					}
				}
			}
		}
	}

	/**
	 * releaseRepositorySession releases given repository session
	 * 
	 * @param repoSession
	 * @throws java.lang.Exception
	 */
	public void releaseRepositorySession(RepositoryInstance repoSession)
			throws Exception {
		if (repoSession != null) {
			getClient().releaseRepository(repoSession);

			if (logger.isDebugEnabled()) {
				logger.debug("releaseRepositorySession() released repository session");
			}
		}
	}

	/**
	 * getRepositorySession get session to default repository
	 * 
	 * @return RepositoryInstance
	 * @throws java.lang.Exception
	 */
	public RepositoryInstance getRepositorySession() throws Exception {
		RepositoryInstance repoSession = getClient().openRepository();
		if (logger.isDebugEnabled()) {
			logger.debug("getRepositorySession() opened repository session");
		}
		return repoSession;

		// Repository repository =
		// Framework.getService(RepositoryManager.class).getDefaultRepository();
		// session = repository.open();

	}

	/**
	 * getClient get Nuxeo client for accessing Nuxeo services remotely using
	 * Nuxeo Java (EJB) Remote APIS
	 * 
	 * @return NuxeoClient
	 * @throws java.lang.Exception
	 */
	public NuxeoClient getClient() throws Exception {
		if (initialized == true) {
			if (client.isConnected()) {
				client.login();
				return client;
			} else {
				client.forceConnect(repositoryClientConfig.getHost(),
						repositoryClientConfig.getPort());
				if (logger.isDebugEnabled()) {
					logger.debug("getClient(): connection successful port="
							+ repositoryClientConfig.getPort());
				}
				return client;
			}
		}
		String msg = "NuxeoConnector is not initialized!";
		logger.error(msg);
		throw new IllegalStateException(msg);
	}

	/**
	 * retrieveWorkspaceIds retrieves all workspace ids from default repository
	 * 
	 * @param tenantDomain
	 *            domain representing tenant
	 * @return
	 * @throws java.lang.Exception
	 */
	public Hashtable<String, String> retrieveWorkspaceIds(String tenantDomain)
			throws Exception {
		RepositoryInstance repoSession = null;
		Hashtable<String, String> workspaceIds = new Hashtable<String, String>();
		try {
			repoSession = getRepositorySession();
			DocumentModel rootDoc = repoSession.getRootDocument();
			DocumentModelList rootChildrenList = repoSession
					.getChildren(rootDoc.getRef());
			Iterator<DocumentModel> diter = rootChildrenList.iterator();
			while (diter.hasNext()) {
				DocumentModel domain = diter.next();
				String domainPath = "/" + tenantDomain;
				if (!domain.getPathAsString().equalsIgnoreCase(domainPath)) {
					continue;
				}
				if (logger.isDebugEnabled()) {
					logger.debug("domain=" + domain.toString());
				}
				DocumentModelList domainChildrenList = repoSession
						.getChildren(domain.getRef());
				Iterator<DocumentModel> witer = domainChildrenList.iterator();
				while (witer.hasNext()) {
					DocumentModel childNode = witer.next();
					if ("Workspaces".equalsIgnoreCase(childNode.getName())) {
						DocumentModelList workspaceList = repoSession
								.getChildren(childNode.getRef());
						Iterator<DocumentModel> wsiter = workspaceList
								.iterator();
						while (wsiter.hasNext()) {
							DocumentModel workspace = wsiter.next();
							if (logger.isDebugEnabled()) {
								logger.debug("workspace name="
										+ workspace.getName() + " id="
										+ workspace.getId());
							}
							workspaceIds.put(workspace.getName().toLowerCase(),
									workspace.getId());
						}
					}
				}
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("retrieveWorkspaceIds() caught exception ", e);
			}
			throw e;
		} finally {
			if (repoSession != null) {
				releaseRepositorySession(repoSession);
			}
		}
		return workspaceIds;
	}
	
	@Deprecated
    private void loadBundles() throws Exception {
    }
    	
}
