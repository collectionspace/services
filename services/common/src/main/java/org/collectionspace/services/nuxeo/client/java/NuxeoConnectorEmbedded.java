package org.collectionspace.services.nuxeo.client.java;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.collectionspace.services.common.api.JEEServerDeployment;
import org.collectionspace.services.config.RepositoryClientConfigType;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.nuxeo.ecm.core.NXCore;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.model.Repository;
import org.nuxeo.osgi.application.FrameworkBootstrap;
import org.nuxeo.ecm.core.repository.RepositoryDescriptor;
import org.nuxeo.ecm.core.repository.RepositoryFactory;
import org.nuxeo.ecm.core.storage.sql.ra.ConnectionFactoryImpl;
import org.nuxeo.ecm.core.storage.sql.ra.ManagedConnectionFactoryImpl;

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
		
	private final static String CSPACE_JEESERVER_HOME = "CSPACE_CONTAINER";
	private final static String CSPACE_NUXEO_HOME = "CSPACE_NUXEO_HOME";
	
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
					client = NuxeoClientEmbedded.getInstance();
					initialized = true;
				}
			}
		}
	}
	
	public String getDatabaseName(String repoName) {
		String result = null;
		
		try {
			this.getRepositoryDescriptor(repoName);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Repository repository = null;
		try {
			repository = this.lookupRepository(repoName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ConnectionFactoryImpl connectionFactory = (ConnectionFactoryImpl)repository;
		ManagedConnectionFactoryImpl managedConnectionFactory = connectionFactory.getManagedConnectionFactory();
		String serverUrl = managedConnectionFactory.getServerURL();
		
		return result;
	}

	/**
	 * releaseRepositorySession releases given repository session
	 * 
	 * @param repoSession
	 * @throws java.lang.Exception
	 */
	public void releaseRepositorySession(RepositoryInstanceInterface repoSession)
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
	public RepositoryInstanceInterface getRepositorySession(RepositoryDomainType repoDomain) throws Exception {
		RepositoryInstanceInterface repoSession = getClient().openRepository(repoDomain);
		
		if (logger.isDebugEnabled() && repoSession != null) {
			logger.debug("getRepositorySession() opened repository session");
			String repoName = repoDomain.getRepositoryName();
			String databaseName = this.getDatabaseName(repoName); // For debugging purposes only
		}
		
		return repoSession;
	}

    public Repository lookupRepository(String name) throws Exception {
        Repository repo;
        try {
            // needed by glassfish
            repo = (Repository) new InitialContext().lookup("NXRepository/"
                    + name);
        } catch (NamingException e) {
            try {
                // needed by jboss
                repo = (Repository) new InitialContext().lookup("java:NXRepository/"
                        + name);
            } catch (NamingException ee) {
                repo = (Repository) NXCore.getRepositoryService().getRepositoryManager().getRepository(
                        name);
            }
        }
        if (repo == null) {
            throw new IllegalArgumentException("Repository not found: " + name);
        }
        return repo;
    }
    
    public RepositoryDescriptor getRepositoryDescriptor(String name) throws Exception {
    	RepositoryDescriptor repo = null;
        Iterable<RepositoryDescriptor> descriptorsList = NXCore.getRepositoryService().getRepositoryManager().getDescriptors();
        for (RepositoryDescriptor descriptor : descriptorsList) {
        	String homeDir = descriptor.getHomeDirectory();
        	String config = descriptor.getConfigurationFile();
        	RepositoryFactory factor = descriptor.getFactory();
        }

        return repo;
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
		RepositoryInstanceInterface repoSession = null;
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
