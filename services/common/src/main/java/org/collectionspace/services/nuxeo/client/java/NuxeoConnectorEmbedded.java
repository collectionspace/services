package org.collectionspace.services.nuxeo.client.java;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.ServletContext;

import org.collectionspace.services.config.RepositoryClientConfigType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.osgi.application.FrameworkBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuxeoConnectorEmbedded {
	/*
    <host>127.0.0.1</host>
    <port>62474</port> <!-- java -->
 */
	private Logger logger = LoggerFactory.getLogger(NuxeoConnectorEmbedded.class);

	public final static String NUXEO_CLIENT_DIR = "nuxeo-client";
	public final static String NUXEO_SERVER_DIR = "nuxeo-server";
	private final static String ERROR_CONNECTOR_NOT_INITIALIZED = "NuxeoConnector is not initialized!";


	private static final String HOST = "127.0.0.1";
	private static final int PORT = 62474;
		
	private final static String CSPACE_JEESERVER_HOME = "CSPACE_CONTAINER";
	private final static String CSPACE_NUXEO_HOME = "CSPACE_NUXEO_HOME";
	
	private static final String NUXEO_CLIENT_USERNAME = "NUXEO_CLIENT_USERNAME";
	private static final String NUXEO_CLIENT_PASSWORD = "NUXEO_CLIENT_PASSWORD";

	private static final NuxeoConnectorEmbedded self = new NuxeoConnectorEmbedded();
	private NuxeoClientEmbedded client;
	private ServletContext servletContext = null;
	private volatile boolean initialized = false; // use volatile for lazy
													// initialization in
													// singleton
	private RepositoryClientConfigType repositoryClientConfig;
	public FrameworkBootstrap fb;

	private NuxeoConnectorEmbedded() {
	}

	public final static NuxeoConnectorEmbedded getInstance() {
		return self;
	}

	private String getClientUserName() {
		String username = System.getenv(NUXEO_CLIENT_USERNAME);
		if (username == null) {
			username = "Administrator";
		}
		return username;
	}

	private String getClientPassword() {
		String password = System.getenv(NUXEO_CLIENT_PASSWORD);
		if (password == null) {
			password = "Administrator";
		}
		return password;
	}
	
	private String getServerRootPath() {
		String result = null;
		
		String prop = System.getenv(CSPACE_JEESERVER_HOME);
		if (prop == null || prop.isEmpty()) {
			logger.error("The following CollectionSpace services' environment variable needs to be set: " + CSPACE_JEESERVER_HOME);
		}
		
		return result;
	}
	
	private String getNuxeoServerPath(String serverRootPath) throws IOException {
		String result = null;
		//
		// Look for the CSPACE_NUXEO_HOME environment variable that might contain the fully qualified path of the
		// Nuxeo EP configuration directory.
		//
		String prop = System.getenv(CSPACE_NUXEO_HOME);
		if (prop != null && !prop.isEmpty()) {
			result = prop;
		} else {
			//
			// Could not find the 'CSPACE_NUXEO_HOME' environment variable, so using the default location instead.
			//
			result = serverRootPath + "/" + NUXEO_SERVER_DIR;
		}
		
		return result;
	}
	
	private File getNuxeoServerDir(String serverRootPath) throws IOException {
		File result = null;
		String errMsg = null;
		
		String path = getNuxeoServerPath(serverRootPath);
		if (path != null) {
			File temp = new File(path);
			if (temp.exists() == true) {
				result = temp;
			} else {
				errMsg = "The Nuxeo EP configuration directory is missing or inaccessible at: '" + path + "'.";
			}
		}
		
		if (result == null) {
			if (errMsg == null) {
				path = path != null ? path : "<empty>";
				errMsg = "Unknown error trying to find Nuxeo configuration: '" +
						CSPACE_NUXEO_HOME + "' = " +
						path;
			}
			throw new IOException(errMsg);
		}
		
		return result;
	}

	//
	// Start/boot the Nuxeo EP server instance
	//
	private void startNuxeoEP(String serverRootPath) throws Exception {
		File nuxeoHomeDir = getNuxeoServerDir(serverRootPath);

		if (logger.isInfoEnabled() == true) {
			logger.info("Starting Nuxeo EP server from configuration at: "
					+ nuxeoHomeDir.getCanonicalPath());
		}
		
		fb = new FrameworkBootstrap(NuxeoConnectorEmbedded.class.getClassLoader(),
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

	public void initialize(String serverRootPath,
			RepositoryClientConfigType repositoryClientConfig,
			ServletContext servletContext) throws Exception {
		if (initialized == false) {
			synchronized (this) {
				if (initialized == false) {
					this.servletContext = servletContext;
					this.repositoryClientConfig = repositoryClientConfig;
					startNuxeoEP(serverRootPath);
					client = new NuxeoClientEmbedded();
					initialized = true;
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
	}

	/**
	 * getClient get Nuxeo client for accessing Nuxeo services remotely using
	 * Nuxeo Java (EJB) Remote APIS
	 * 
	 * @return NuxeoClient
	 * @throws java.lang.Exception
	 */
	public NuxeoClientEmbedded getClient() throws Exception {
		if (initialized == true) {
			if (client.isConnected()) {
//				client.login();
				return client;
			} else {
				client.forceConnect(this.HOST,
						this.PORT);
				if (logger.isDebugEnabled()) {
					logger.debug("getClient(): connection successful port="
							+ this.PORT);
				}
				return client;
			}
		}
		//
		// Nuxeo connection was not initialized
		//
		logger.error(ERROR_CONNECTOR_NOT_INITIALIZED);
		throw new IllegalStateException(ERROR_CONNECTOR_NOT_INITIALIZED);
	}
	
	void releaseClient() throws Exception {
		if (initialized == true) {
//			client.logout();
		} else {
			//
			// Nuxeo connection was not initialized
			//
			logger.error(ERROR_CONNECTOR_NOT_INITIALIZED);
			throw new IllegalStateException(ERROR_CONNECTOR_NOT_INITIALIZED);
		}
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
					if (NuxeoUtils.Workspaces.equalsIgnoreCase(childNode.getName())) { 
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
