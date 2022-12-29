package org.collectionspace.services.nuxeo.client.java;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.management.JMException;
import javax.servlet.ServletContext;

import org.apache.catalina.util.ServerInfo;
import org.collectionspace.services.common.api.JEEServerDeployment;
import org.collectionspace.services.config.RepositoryClientConfigType;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.SystemPrincipal;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.osgi.application.MutableClassLoaderDelegate;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuxeoConnectorEmbedded {
	/*
    <host>127.0.0.1</host>
    <port>62474</port> <!-- java -->
 */
	private Logger logger = LoggerFactory.getLogger(NuxeoConnectorEmbedded.class);

	public final static String NUXEO_CLIENT_DIR = JEEServerDeployment.NUXEO_CLIENT_DIR;
	public final static String NUXEO_SERVER_DIR = JEEServerDeployment.NUXEO_SERVER_DIR;
	private final static String ERROR_CONNECTOR_NOT_INITIALIZED = "NuxeoConnector is not initialized!";

	private final static String CSPACE_NUXEO_HOME = "CSPACE_NUXEO_HOME";

	private static final NuxeoConnectorEmbedded self = new NuxeoConnectorEmbedded();
	private NuxeoClientEmbedded client;
	private volatile boolean initialized = false; // use volatile for lazy
													// initialization in
													// singleton
	ClassLoader classLoader;
	public NuxeoFrameworkBootstrap fb;
	private ServletContext servletContext;
	private RepositoryClientConfigType repositoryClientConfig;

	private NuxeoConnectorEmbedded() {
		// empty constructor
	}

	public final static NuxeoConnectorEmbedded getInstance() {
		return self;
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
	// For testing of CC-https://jira.ets.berkeley.edu/jira/browse/CC-1268
	// FIXME: CC-1268 - Not for production
	//
	private void runCCdash1268(CoreSession coreSession) throws Exception {
		final String PICTURES_TO_MIGRATE_QUERY = "SELECT ecm:uuid FROM Document "
	            + "WHERE ecm:mixinType = 'Picture' AND ecm:isProxy = 0 AND views/*/title = 'Original' "
	            + "AND content/data IS NULL";

		String msg = String.format("Checking for candidate Pictures that Nuxeo needs to migrate.  Using this query: %s", PICTURES_TO_MIGRATE_QUERY);
		logger.info(msg);

		DocumentModelList queryResult = coreSession.query(PICTURES_TO_MIGRATE_QUERY);
		if (queryResult != null && queryResult.isEmpty() == false) {
			msg = String.format("Found %d candidate Pictures for migration by Nuxeo.", queryResult.size());
			logger.info(msg);
			for (DocumentModel docModel : queryResult) {
				msg = String.format("Candidate for Nuxeo migration: ID='%s'\tname='%s'\tType='%s'",
						docModel.getId(), docModel.getName(), docModel.getType());
				logger.info(msg);
			}
		} else {
			logger.info("No candidate Pictures found.");
		}
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

		classLoader = NuxeoConnectorEmbedded.class.getClassLoader();

		fb = new NuxeoFrameworkBootstrap(classLoader, nuxeoHomeDir);
		fb.setHostName("Tomcat");
		fb.setHostVersion(ServerInfo.getServerNumber());

		fb.initialize();
		fb.start(new MutableClassLoaderDelegate(classLoader));

		// Test to see if we can connect to the default repository
		boolean transactionStarted = false;

		if (!TransactionHelper.isTransactionActiveOrMarkedRollback()) {
				transactionStarted = TransactionHelper.startTransaction();
		}

		CoreSession coreSession = null;

		try {
			Repository defaultRepo = Framework.getService(RepositoryManager.class).getDefaultRepository();
			coreSession = CoreInstance.openCoreSession(defaultRepo.getName(), new SystemPrincipal(null));
			//
			// CC-1268 - Not for production
			//
			//runCCdash1268(coreSession); // FIXME: CC-1268 - Not for production
			//
			// CC-1268 - Not for production
			//
			} catch (Throwable t) {
			logger.error(t.getMessage());
			throw new RuntimeException("Could not start the Nuxeo EP Framework", t);
		} finally {
			if (coreSession != null) {
				CoreInstance.closeCoreSession(coreSession);
			}

			if (transactionStarted) {
				TransactionHelper.commitOrRollbackTransaction();
			}
		}
	}
	
	private void stopNuxeoEP() {
		boolean success = true;
		
		try {
			fb.stop(new MutableClassLoaderDelegate(classLoader));
	        Enumeration<Driver> drivers = DriverManager.getDrivers();
	        while (drivers.hasMoreElements()) {
	            Driver driver = drivers.nextElement();
	            try {
	                DriverManager.deregisterDriver(driver);
	                logger.info(String.format("Deregister JDBC driver: %s", driver));
	            } catch (SQLException e) {
	            	logger.error(String.format("Error deregistering JDBC driver %s", driver), e);
	            }
	        }

		} catch (IllegalArgumentException e) {
			success = false;
		} catch (ReflectiveOperationException e) {
			success = false;
		} catch (JMException e) {
			success = false;
		}
		
		if (!success) {
			logger.error("CollectionSpace was unable to shutdown Nuxeo cleanly.");
		}
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
				stopNuxeoEP();
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
					client = NuxeoClientEmbedded.getInstance();
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
	public void releaseRepositorySession(CoreSessionInterface repoSession)
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
	public CoreSessionInterface getRepositorySession(RepositoryDomainType repoDomain) throws Exception {
		CoreSessionInterface repoSession = getClient().openRepository(repoDomain);

		if (logger.isDebugEnabled() && repoSession != null) {
			String repoName = repoDomain.getRepositoryName();
			logger.debug("getRepositorySession() opened repository session on: %s repo",
					repoName != null ? repoName : "unknown");
		}

		return repoSession;
	}


// TODO: Remove after CSPACE-6375 issue is resolved.
//    public List<RepositoryDescriptor> getRepositoryDescriptor(String name) throws Exception {
//    	RepositoryDescriptor repo = null;
//		RepositoryManager repositoryManager = Framework.getService(RepositoryManager.class);
//        Iterable<RepositoryDescriptor> descriptorsList = repositoryManager.getDescriptors();
//        for (RepositoryDescriptor descriptor : descriptorsList) {
//        	String homeDir = descriptor.getHomeDirectory();
//        	String config = descriptor.getConfigurationFile();
//        	RepositoryFactory factor = descriptor.getFactory();
//        }
//
//        return repo;
//    }

	/**
	 * getClient get Nuxeo client for accessing Nuxeo services remotely using
	 * Nuxeo Java (EJB) Remote APIS
	 *
	 * @return NuxeoClient
	 * @throws java.lang.Exception
	 */
	public NuxeoClientEmbedded getClient() throws Exception {
		if (initialized == true) {
			return client;
		}
		//
		// Nuxeo connection was not initialized
		//
		logger.error(ERROR_CONNECTOR_NOT_INITIALIZED);
		throw new IllegalStateException(ERROR_CONNECTOR_NOT_INITIALIZED);
	}

	void releaseClient() throws Exception {
		if (initialized == true) {
			// Do nothing.
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
	 * @param repoDomain
	 *            a repository domain for a given tenant - see the tenant bindings XML file for details
	 * @return
	 * @throws java.lang.Exception
	 */
	public Hashtable<String, String> retrieveWorkspaceIds(RepositoryDomainType repoDomain)
			throws Exception {
		CoreSessionInterface repoSession = null;
		Hashtable<String, String> workspaceIds = new Hashtable<String, String>();
		try {
			repoSession = getRepositorySession(repoDomain);
			DocumentModel rootDoc = repoSession.getRootDocument();
			DocumentModelList rootChildrenList = repoSession.getChildren(rootDoc.getRef());
			Iterator<DocumentModel> diter = rootChildrenList.iterator();
			while (diter.hasNext()) {
				DocumentModel domain = diter.next();
				String domainPath = "/" + repoDomain.getStorageName();
				if (!domain.getPathAsString().equalsIgnoreCase(domainPath)) {
					continue; // If it's not our domain folder/directory then skip it
				}
				if (logger.isDebugEnabled()) {
					logger.debug("domain=" + domain.toString());
				}
				DocumentModelList domainChildrenList = repoSession.getChildren(domain.getRef());
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
}
