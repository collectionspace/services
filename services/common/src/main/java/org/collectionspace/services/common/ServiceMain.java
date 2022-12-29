/**
 * Copyright 2009-2010 University of California at Berkeley
 */
package org.collectionspace.services.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import org.collectionspace.authentication.AuthN;

import org.collectionspace.services.client.XmlTools;

import org.collectionspace.services.common.api.JEEServerDeployment;
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorization_mgt.AuthorizationCommon;
import org.collectionspace.services.common.config.ConfigReader;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.common.config.ServicesConfigReaderImpl;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.init.AddIndices;
import org.collectionspace.services.common.init.IInitHandler;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
import org.collectionspace.services.config.service.InitHandler.Params.Field;
import org.collectionspace.services.config.ClientType;
import org.collectionspace.services.config.ServiceConfig;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.ElasticSearchIndexConfig;
import org.collectionspace.services.config.tenant.EventListenerConfig;
import org.collectionspace.services.config.tenant.EventListenerConfigurations;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.config.types.PropertyType;

import org.collectionspace.services.nuxeo.client.java.NuxeoConnectorEmbedded;
import org.collectionspace.services.nuxeo.client.java.TenantRepository;
import org.collectionspace.services.nuxeo.listener.CSEventListener;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventListenerImpl;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.nuxeo.ecm.platform.audit.service.NXAuditEventsService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.RegistrationInfo;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.tree.DefaultElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for Services layer. It reads configuration and performs service
 * level initialization. It is a singleton.
 * @author
 */
public class ServiceMain {
	final static Logger logger = LoggerFactory.getLogger(ServiceMain.class);

	public static final String VER_DISPLAY_NAME = "CollectionSpace Services v7.1";
	public static final String VER_MAJOR = "7";
	public static final String VER_MINOR = "1";
	public static final String VER_PATCH = "0";
	public static final String VER_BUILD = "1";

	private static final int PRIMARY_REPOSITORY_DOMAIN = 0;

    /**
     * For some reason, we have trouble getting logging from this class directed to
     * the tomcat/catalina console log file.  So we do it explicitly with this method.
     *
     * @param str
     */
    private static void mirrorToStdOut(String str) {
    	System.out.println(str);
    	ServiceMain.logger.info(str);
    }

    /**
     * volatile is used here to assume about ordering (post JDK 1.5)
     */
    private static volatile ServiceMain instance = null;
    private static volatile boolean initFailed = false;

    private static final String SERVER_HOME_PROPERTY = "catalina.base";
	private static final boolean USE_APP_GENERATED_CONFIG = true;

	private static ServletContext servletContext = null;

	private NuxeoConnectorEmbedded nuxeoConnector;
    private String serverRootDir = null;
    private ServicesConfigReaderImpl servicesConfigReader;
    private TenantBindingConfigReaderImpl tenantBindingConfigReader;
    private UriTemplateRegistry uriTemplateRegistry = new UriTemplateRegistry();

    private static final String DROP_DATABASE_SQL_CMD = "DROP DATABASE";
    private static final String DROP_DATABASE_IF_EXISTS_SQL_CMD = DROP_DATABASE_SQL_CMD + " IF EXISTS %s;";
    private static final String DROP_USER_SQL_CMD = "DROP USER";
    private static final String DROP_USER_IF_EXISTS_SQL_CMD = DROP_USER_SQL_CMD + " IF EXISTS %s;";
    private static final String DROP_OBJECTS_SQL_COMMENT = "-- drop all the objects before dropping roles";
	private static final String CSPACE_JEESERVER_HOME = "CSPACE_JEESERVER_HOME";
	private static final String CSPACE_UTILS_SCHEMANAME = "utils";
	private static final String RUNSQLSCRIPTS_SERVICE_NAME = "runsqlscripts";

    private ServiceMain() {
    	// Intentionally blank
    }

    /*
     *
     * Set this singletons ServletContext without any call to initialize
     */
    private static void setServletContext(ServletContext servletContext) {
		if (servletContext != null) {
	    	synchronized (ServiceMain.class) {
	    		ServiceMain.servletContext = servletContext;
	    	}
		}
    }

    public String getCspaceDatabaseName() {
    	return getServiceConfig().getDbCspaceName();
    }

    public boolean inServletContext() {
    	return ServiceMain.servletContext != null;
    }

    public static ServiceMain getInstance(ServletContext servletContext) {
    	setServletContext(servletContext);
    	return ServiceMain.getInstance();
    }

    /**
     * getInstance returns the ServiceMain singleton instance after proper
     * initialization in a thread-safe manner
     * @return
     */
    public static ServiceMain getInstance() {
        if (instance == null && initFailed == false) {
            synchronized (ServiceMain.class) {
                if (instance == null && initFailed == false) {
                    ServiceMain newInstance = new ServiceMain();
                    try {
                    	//assume the worse
                    	initFailed = true;
                    	newInstance.initialize();
                    	//celebrate success
                        initFailed = false;
                    } catch (Exception e) {
                    	newInstance.release(); // attempt to release resources acquired during initialization attempt 
                        instance = null;
                        if (e instanceof RuntimeException) {
                            throw (RuntimeException) e;
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                    instance = newInstance;
                }
            }
        }

        if (instance == null) {
        	throw new RuntimeException("Could not initialize the CollectionSpace services.  Please see the CollectionSpace services log file(s) for details.");
        }

        return instance;
    }
    
	static public boolean isAuditServiceReady() {
		boolean result = true;
		
		try {
			RegistrationInfo regInfo = Framework.getRuntime().getComponentManager().getRegistrationInfo(NXAuditEventsService.NAME);
			if (regInfo.getState() == RegistrationInfo.START_FAILURE) {
				result = false;
			}
		} catch (Throwable t) {
			result = false;
		}
		
		return result;
	}
    
	/**
	 * Use this method to check for services/features required by each tenant.
	 * @throws ConfigurationException
	 */
    private void ensureRequiredNuxeoServices() throws ConfigurationException {
        Hashtable<String, TenantBindingType> tenantBindings = tenantBindingConfigReader.getTenantBindings();
        for (String tenantId : tenantBindings.keySet()) {
	        TenantBindingType tenantBinding = tenantBindings.get(tenantId);
	        if (tenantBinding.isAuditRequired() == true && isAuditServiceReady() == false) {
	        	throw new ConfigurationException(String.format("Tenant %s:%s is configured to require auditing but the audit service could not start.  Verify the Nuxeo Audit Service is configured correctly.",
	        			tenantBinding.getDisplayName(), tenantId));
	        }
        }
    }

    private void initialize() throws Exception {
    	// set our root directory
    	setServerRootDir();

		// read in and set our Services config
    	readAndSetServicesConfig();

    	// Set our AuthN's datasource to for the cspaceDataSource
    	AuthN.setDataSource(JDBCTools.getDataSource(JDBCTools.CSPACE_DATASOURCE_NAME));

    	// In each tenant, set properties that don't already have values
    	// to their default values.
        propagateConfiguredProperties();

        // Create or update Nuxeo's per-repository configuration files.
        createOrUpdateNuxeoDatasourceConfigFiles();
        createOrUpdateNuxeoRepositoryConfigFiles();
        createOrUpdateNuxeoElasticsearchConfigFiles();

        // Create the Nuxeo-managed databases, along with the requisite
        // access rights to each.
    	HashSet<String> dbsCheckedOrCreated = createNuxeoDatabases();

        // Update the SQL script that drops databases and users,
        // to include DROP statements for each of the Nuxeo-managed
        // database names and for each relevant datasource user.
        String[] dataSourceNames = {JDBCTools.NUXEO_DATASOURCE_NAME, JDBCTools.NUXEO_READER_DATASOURCE_NAME};
        updateInitializationScript(getNuxeoDatabasesDropScriptFilename(),
                dbsCheckedOrCreated, dataSourceNames);

        //
        // Start up and initialize our embedded Nuxeo instance.
        //
        if (getClientType().equals(ClientType.JAVA)) {
            nuxeoConnector = NuxeoConnectorEmbedded.getInstance();
            mirrorToStdOut("\nStarting Nuxeo platform...");
            nuxeoConnector.initialize(
            		getServerRootDir(),
            		getServicesConfigReader().getConfiguration().getRepositoryClient(),
            		ServiceMain.servletContext);
            mirrorToStdOut("Nuxeo platform started successfully.\n");
        } else {
        	//
        	// Exit if we don't have the correct/known client type
        	//
        	throw new RuntimeException("Unknown CollectionSpace services client type: " + getClientType());
        }

        //
        // Ensure any/all tenant requirements are met --e.g., the Nuxeo Audit service is configured correctly and running
        //
        ensureRequiredNuxeoServices();
        
        //
        // Initialize the Nuxeo event listeners using the tenant-specific service bindings
        //
        initializeEventListeners();

        //
        // Mark if a tenant's bindings have changed since the last time we started, by comparing the MD5 hash of each tenant's bindings with that of
        // the bindings the last time we started/launch.
        //
        String cspaceDatabaseName = getCspaceDatabaseName();
        Hashtable<String, TenantBindingType> tenantBindings = tenantBindingConfigReader.getTenantBindings();
        for (String tenantId : tenantBindings.keySet()) {
	        TenantBindingType tenantBinding = tenantBindings.get(tenantId);
	        String persistedMD5Hash = AuthorizationCommon.getPersistedMD5Hash(tenantId, cspaceDatabaseName);
	        String currentMD5Hash = tenantBinding.getConfigMD5Hash();
	    	AuthorizationCommon.setTenantConfigMD5Hash(tenantId, currentMD5Hash); // store this for later.  We'll persist this info with the tenant record.
	        tenantBinding.setConfigChangedSinceLastStart(hasConfigChanged(tenantBinding, persistedMD5Hash, currentMD5Hash));
        }

        //
        // Create all the tenant records, default user accounts, roles, and permissions.  Since some of our "cspace" database config files
        // for Spring need to be created at build time, the "cspace" database already will be suffixed with the
        // correct 'cspaceInstanceId' so we don't need to pass it to the JDBCTools methods.
        //
		JPATransactionContext jpaTransactionContext = new JPATransactionContext();
		try {
			jpaTransactionContext.beginTransaction();
			DatabaseProductType databaseProductType = JDBCTools.getDatabaseProductType(JDBCTools.CSPACE_DATASOURCE_NAME,
					cspaceDatabaseName);
			AuthorizationCommon.createTenants(tenantBindingConfigReader, databaseProductType, cspaceDatabaseName);
			AuthorizationCommon.createDefaultWorkflowPermissions(jpaTransactionContext, tenantBindingConfigReader, databaseProductType, cspaceDatabaseName);
			AuthorizationCommon.createDefaultAccounts(tenantBindingConfigReader, databaseProductType, cspaceDatabaseName);
			AuthorizationCommon.persistTenantBindingsMD5Hash(tenantBindingConfigReader, databaseProductType, cspaceDatabaseName);
			jpaTransactionContext.commitTransaction();
		} catch (Exception e) {
			logger.error("Default create/update of tenants, accounts, roles and permissions setup failed with exception(s): " +
					e.getLocalizedMessage(), e);
			throw e;
		} finally {
			jpaTransactionContext.close();
		}
		//
		// Log tenant status -shows all tenants' info and active status.
		//
		showTenantStatus();
    }

	/**
     * Returns the primary repository name for a tenant -there's usually just one.
     * @param tenantBinding
     * @return
     * @throws InstantiationException
     */

	protected String getPrimaryRepositoryName(TenantBindingType tenantBinding) throws InstantiationException {
    	String result = "default";

    	List<RepositoryDomainType> repositoryDomainList = tenantBinding.getRepositoryDomain();
    	if (repositoryDomainList != null && repositoryDomainList.isEmpty() == false) {
    		String repositoryName = repositoryDomainList.get(PRIMARY_REPOSITORY_DOMAIN).getRepositoryName();
    		if (repositoryName != null && !repositoryName.isEmpty()) {
    			result = repositoryName;
    		}
    	} else {
    		String msg = String.format("Tenant bindings for '%s' is missing a repositoryDomain element in its bindings file.",
    				tenantBinding.getName());
    		logger.error(msg);
    		throw new InstantiationException(msg);
    	}

    	return result;
    }

    /**
     * Initialize the event listeners.  We're essentially registering listeners with tenants.  This ensures that listeners ignore events
     * caused by other tenants.
     */
    private void initializeEventListeners() {
    	Hashtable<String, TenantBindingType> tenantBindings = this.tenantBindingConfigReader.getTenantBindings();

        for (TenantBindingType tenantBinding : tenantBindings.values()) {
        	EventListenerConfigurations eventListenerConfigurations = tenantBinding.getEventListenerConfigurations();
        	if (eventListenerConfigurations != null) {
        		List<EventListenerConfig> eventListenerConfigList = eventListenerConfigurations.getEventListenerConfig();
                ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        		for (EventListenerConfig eventListenerConfig : eventListenerConfigList) {
        			String clazz = eventListenerConfig.getClassName().trim();
        			if (clazz.isEmpty() == false) {
	                    try {
	        	            Class<?> c = tccl.loadClass(clazz);
	        	            if (CSEventListener.class.isAssignableFrom(c)) {
	        	            	CSEventListener listener = (AbstractCSEventListenerImpl) c.newInstance();
	        	            	listener.register(getPrimaryRepositoryName(tenantBinding), eventListenerConfig);  // Register the listener with a tenant using its repository name
	        	            	if (logger.isInfoEnabled()) {
	        	            		String msg = String.format("Event Listener - Success: Tenant '%30s'\tActivated listener %s:%s",
	        	            				tenantBinding.getName(), eventListenerConfig.getId(), clazz);
	        	            		logger.info(msg);
	        	            	}
	        	            }
	                    } catch (ClassNotFoundException e) {
	                    	String msg = String.format("Event Listener - FAILURE: Tenant '%30s'\tFailed to find event listener %s:%s",
	                    			tenantBinding.getName(), eventListenerConfig.getId(), clazz);
	                    	logger.warn(msg);
	                    	logger.trace(msg, e);
	                    	failIfRequired(eventListenerConfig);
	                    } catch (InstantiationException e) {
	                    	String msg = String.format("Event Listener - FAILURE: Tenant '%30s'\tFailed to instantiate event listener %s:%s",
	                    			tenantBinding.getName(), eventListenerConfig.getId(), clazz);
	                    	logger.warn(msg);
	                    	logger.trace(msg, e);
	                    	failIfRequired(eventListenerConfig);
						} catch (IllegalAccessException e) {
	                    	String msg = String.format("Event Listener - FAILURE: Tenant '%30s'\tIllegal access to event listener %s:%s",
	                    			tenantBinding.getName(), eventListenerConfig.getId(), clazz);
	                    	logger.warn(msg);
	                    	logger.trace(msg, e);
	                    	failIfRequired(eventListenerConfig);
						}
        			}
        		}
        	}
        	logger.info("\n");
        }
	}

	private void failIfRequired(EventListenerConfig eventListenerConfig) {
		if (eventListenerConfig.isRequired() == true) {
			throw new RuntimeException(String.format("Required event listener '%s' missing or could not be instantiated.", eventListenerConfig.getId()));
		}

	}

	private void showTenantStatus() {
    	Hashtable<String,TenantBindingType> tenantBindingsList = tenantBindingConfigReader.getTenantBindings(true);
    	mirrorToStdOut("++++++++++++++++ Summary - CollectionSpace tenant status. ++++++++++++++++++++++++");
    	String headerTemplate = "%10s %10s %30s %60s %10s";
    	String headerUnderscore = String.format(headerTemplate,
    			"______",
    			"__",
    			"____",
    			"____________",
    			"_______");
    	String header = String.format(headerTemplate,
    			"Status",
    			"ID",
    			"Name",
    			"Display Name",
    			"Version");
    	mirrorToStdOut(header);
    	mirrorToStdOut(headerUnderscore);

    	for (String tenantId : tenantBindingsList.keySet()) {
    		TenantBindingType tenantBinding = tenantBindingsList.get(tenantId);
    		String statusLine = String.format(headerTemplate,
    				tenantBinding.isCreateDisabled() ? "Disabled" : "Active",
    				tenantBinding.getId(),
    				tenantBinding.getName(),
    				tenantBinding.getDisplayName(),
    				tenantBinding.getVersion());
    		mirrorToStdOut(statusLine);
    	}
    	// footer
    	mirrorToStdOut("++++++++++++++++ ........................................ ++++++++++++++++++++++++");
    }

    /**
     * release releases all resources occupied by service layer infrastructure
     * but not necessarily those occupied by individual services
     */
    public void release() {
        try {
            if (nuxeoConnector != null) {
                nuxeoConnector.release();
            }
            instance = null;
        } catch (Exception e) {
            e.printStackTrace();
            //gobble it
        }
    }

    private void readAndSetServicesConfig() throws Exception {
        //read service config
        servicesConfigReader = new ServicesConfigReaderImpl(getServerRootDir());
        servicesConfigReader.read(USE_APP_GENERATED_CONFIG);

        Boolean useAppGeneratedBindings = servicesConfigReader.getConfiguration().isUseAppGeneratedTenantBindings();
        tenantBindingConfigReader = new TenantBindingConfigReaderImpl(getServerRootDir());
        tenantBindingConfigReader.read(useAppGeneratedBindings);
    }

    /*
     * Returns 'true' if the tenant bindings have change since we last started up the Services layer of if the 'forceUpdate' field in the bindings
     * has been set to 'true'
     *
     */
	private static boolean hasConfigChanged(TenantBindingType tenantBinding, String persistedMD5Hash,
			String currentMD5Hash) {
		boolean result = false;

		if (persistedMD5Hash == null || tenantBinding.isForceUpdate() == true) {
			result = true;
		} else {
			result = !persistedMD5Hash.equals(currentMD5Hash);  // if the two hashes don't match, the tenant bindings have changed so we'll return 'true'
		}

		return result;
	}


    private void propagateConfiguredProperties() {
        List<PropertyType> repoPropListHolder =
                servicesConfigReader.getConfiguration().getRepositoryClient().getProperties();
        if (repoPropListHolder != null && !repoPropListHolder.isEmpty()) {
            List<PropertyItemType> propList = repoPropListHolder.get(0).getItem();
            if (propList != null && !propList.isEmpty()) {
                tenantBindingConfigReader.setDefaultPropertiesOnTenants(propList, true);
            }
        }
    }

	private String applyRepositoryUpgradeScripts(Connection conn, String dataSourceName, String repositoryName, String fromVersion, String stage) throws Exception {
		Map<String, List<File>> upgradeScriptFiles = getRepositoryUpgradeScripts(dataSourceName, repositoryName, fromVersion, stage);
		Set<String> versions = upgradeScriptFiles.keySet();

		String upgradedToVersion = null;

		if (versions.size() > 0) {
			for (String version : versions) {
				logger.info(String.format("upgrading %s repository to version %s", repositoryName, version));

				List<File> scriptFiles =  upgradeScriptFiles.get(version);

				for (File file : scriptFiles) {
					if (file.getName().endsWith(".sql")) {
						logger.info(String.format("Running %s", file.getName()));

						JDBCTools.runScript(conn, file);
					}
				}

				upgradedToVersion = version;
			}
		}

		return upgradedToVersion;
	}

	private void upgradeRepository(String dataSourceName, String repositoryName, String cspaceInstanceId) throws Exception {
		// Install the uuid-ossp extension so that the uuid_generate_v4 function will be available to
		// upgrade scripts.

		try {
			JDBCTools.executeUpdate(JDBCTools.CSADMIN_NUXEO_DATASOURCE_NAME, repositoryName, cspaceInstanceId, "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");
		}
		catch(Exception e) {
			logger.warn("Could not install uuid-ossp postgresql extension. Database upgrades may fail without this extension. On some platforms you may need to manually install this extension as a superuser.");
		}

		String stage = "post-init";
		Connection conn = null;

		try {
			conn = JDBCTools.getConnection(dataSourceName, repositoryName, cspaceInstanceId);

			conn.setAutoCommit(false);

			String version = JDBCTools.getRepositoryDatabaseVersion(conn);

			logger.info(String.format("%s repository current version is %s", repositoryName, version));

			String upgradedToVersion = applyRepositoryUpgradeScripts(conn, dataSourceName, repositoryName, version, stage);

			if (upgradedToVersion != null) {
				logger.info(String.format("%s repository upgraded to version %s", repositoryName, upgradedToVersion));

				JDBCTools.setRepositoryDatabaseVersion(conn, upgradedToVersion);
			}

			conn.commit();
		}
		catch (Exception e) {
			logger.error(String.format("Could not upgrade %s repository", repositoryName));
			logger.error(e.toString());

			if (conn != null) {
				conn.rollback();
			}
		}
		finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	void upgradeDatabase() throws Exception {
		Hashtable<String, TenantBindingType> tenantBindingTypeMap = tenantBindingConfigReader.getTenantBindings();

		// Loop through all tenants in tenant-bindings.xml

		String cspaceInstanceId = getCspaceInstanceId();

		for (TenantBindingType tbt : tenantBindingTypeMap.values()) {
			List<String> repositoryNameList = ConfigUtils.getRepositoryNameList(tbt);

			if (repositoryNameList != null && repositoryNameList.isEmpty() == false) {
				// Loop through each repo/DB defined in a tenant bindings file

				for (String repositoryName : repositoryNameList) {
					upgradeRepository(JDBCTools.NUXEO_DATASOURCE_NAME, repositoryName, cspaceInstanceId);
				}
			} else {
				String errMsg = "repositoryNameList was empty or null.";

				logger.error(errMsg);

				throw new Exception(errMsg);
			}
		}
	}

	public static Map<String, List<File>> getRepositoryUpgradeScripts(String dataSourceName, String repositoryName, String fromVersion, String stage) throws Exception {
		Map<String, List<File>> upgradeScriptFiles = new LinkedHashMap<>();

		Path upgradesPath = Paths.get(
			ServiceMain.getInstance().getServerRootDir(),
			JEEServerDeployment.DATABASE_SCRIPTS_DIR_PATH,
			JDBCTools.getDatabaseProductType(dataSourceName, repositoryName).toString(),
			"upgrade"
		);

		File upgradesDirectory = upgradesPath.toFile();

		if (!upgradesDirectory.isDirectory() || !upgradesDirectory.canRead()) {
			return upgradeScriptFiles;
		}

		File[] upgradesDirectoryFiles = upgradesDirectory.listFiles();
		List<File> versionDirectories = new ArrayList<>();
		VersionComparator versionComparator = new VersionComparator();

		for (File file : upgradesDirectoryFiles) {
			if (
				file.isDirectory()
				&& file.canRead()
				&& file.getName().matches("^\\d+\\.\\d+(\\.\\d+)?$")
				&& versionComparator.compare(fromVersion, file.getName()) < 0
			) {
				versionDirectories.add(file);
			}
		}

		versionDirectories.sort(new VersionFileNameComparator());

		for (File versionDir : versionDirectories) {
			Path versionStagePath = versionDir.toPath().resolve(stage);
			File versionStageDirectory = versionStagePath.toFile();

			if (versionStageDirectory.isDirectory()) {
				File[] versionStageFiles = versionStageDirectory.listFiles();

				Arrays.sort(versionStageFiles);

				List<File> scriptFiles = new ArrayList<>();

				for (File file : versionStageFiles) {
					if (
						file.isFile()
						&& file.canRead()
					) {
						scriptFiles.add(file);
					}
				}

				if (scriptFiles.size() > 0) {
					upgradeScriptFiles.put(versionDir.getName(), scriptFiles);
				}
			}
		}

		return upgradeScriptFiles;
	}

	/**
	 * From https://dzone.com/articles/semantically-ordering-versioned-file-names-in-java
	 */
	public static class VersionComparator implements Comparator<String> {
		private static final Pattern NUMBERS = Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

		@Override
		public final int compare(String o1, String o2) {
			// Optional "NULLS LAST" semantics:
			if (o1 == null || o2 == null) {
				return o1 == null ? o2 == null ? 0 : -1 : 1;
			}

			// Splitting both input strings by the above patterns
			String[] split1 = NUMBERS.split(o1);
			String[] split2 = NUMBERS.split(o2);
			int length = Math.min(split1.length, split2.length);

			// Looping over the individual segments
			for (int i = 0; i < length; i++) {
				char c1 = split1[i].charAt(0);
				char c2 = split2[i].charAt(0);
				int cmp = 0;

				// If both segments start with a digit, sort them
				// numerically using BigInteger to stay safe
				if (c1 >= '0' && c1 <= '9' && c2 >= 0 && c2 <= '9')
					cmp = new BigInteger(split1[i]).compareTo(
							new BigInteger(split2[i]));

				// If we haven't sorted numerically before, or if
				// numeric sorting yielded equality (e.g 007 and 7)
				// then sort lexicographically
				if (cmp == 0)
					cmp = split1[i].compareTo(split2[i]);

				// Abort once some prefix has unequal ordering
				if (cmp != 0)
					return cmp;
			}

			// If we reach this, then both strings have equally
			// ordered prefixes, but maybe one string is longer than
			// the other (i.e. has more segments)
			return split1.length - split2.length;
		}
	}

	public static class VersionFileNameComparator implements Comparator<File> {
		private final VersionComparator versionComparator = new VersionComparator();

		@Override
		public final int compare(File o1, File o2) {
			return versionComparator.compare(o1.getName(), o2.getName());
		}
	}

	/**
	 * Create required indexes (aka indices) in database tables not associated
	 * with any specific tenant.
	 *
	 * We need to loop over each repository/db declared in the tenant bindings.
	 * The assumption here is that each repo/db is a Nuxeo repo/DB.
	 *
	 * @throws Exception
	 */
	void createRequiredIndices() throws Exception {
        Hashtable<String, TenantBindingType> tenantBindingTypeMap = tenantBindingConfigReader.getTenantBindings();

        //
        //Loop through all tenants in tenant-bindings.xml
        //
        String cspaceInstanceId = getCspaceInstanceId();
        for (TenantBindingType tbt : tenantBindingTypeMap.values()) {
        	List<String> repositoryNameList = ConfigUtils.getRepositoryNameList(tbt);
			if (repositoryNameList != null && repositoryNameList.isEmpty() == false) {
				//
				// Loop through each repo/DB defined in a tenant bindings file
				//
				for (String repositoryName : repositoryNameList) {
					// Define a set of columns (fields) and their associated
					// tables, on which database indexes should always be created
					final String COLLECTIONSPACE_CORE_TABLE_NAME = "collectionspace_core";
					final String NUXEO_FULLTEXT_TABLE_NAME = "fulltext";
					final String NUXEO_HIERARCHY_TABLE_NAME = "hierarchy";

					Map<Integer, List<String>> fieldsToIndex = new HashMap<Integer, List<String>>();
					fieldsToIndex.put(1, new ArrayList<String>(Arrays.asList(COLLECTIONSPACE_CORE_TABLE_NAME, "tenantid")));
					fieldsToIndex.put(2, new ArrayList<String>(Arrays.asList(COLLECTIONSPACE_CORE_TABLE_NAME, "updatedat")));
					fieldsToIndex.put(3, new ArrayList<String>(Arrays.asList(NUXEO_FULLTEXT_TABLE_NAME, "jobid")));
					fieldsToIndex.put(4, new ArrayList<String>(Arrays.asList(NUXEO_HIERARCHY_TABLE_NAME, "name")));

					// Invoke existing post-init code to create these indexes,
					// sending in the set of values above, in contrast to
					// drawing these values from per-tenant configuration.
//					DataSource dataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_DATASOURCE_NAME);
					AddIndices addindices = new AddIndices();
					List<Field> fields = new ArrayList<Field>();
					for (Map.Entry<Integer, List<String>> entry : fieldsToIndex.entrySet()) {
						Field field = new Field();
						field.setTable(entry.getValue().get(0)); // Table name from List
																	// item 0
						field.setCol(entry.getValue().get(1)); // Column name from List item
																// 1
						fields.add(field);
					}
					addindices.onRepositoryInitialized(JDBCTools.NUXEO_DATASOURCE_NAME, repositoryName, cspaceInstanceId, tbt.getShortName(),
							null, fields, null);
				}
			} else {
				String errMsg = "repositoryNameList was empty or null.";
				logger.error(errMsg);
				throw new Exception(errMsg);
			}
        }
	}
	
	//
	// Search through the service bindings for the RUNSQLSCRIPTS_SERVICE_NAME service.  Each tenant can add a set of SQL that
	// will be run before the other Services' initHandlers.
	//
	private void firePostInitRunSQLScripts(Hashtable<String, TenantBindingType> tenantBindingTypeMap) throws Exception {
        String cspaceInstanceId = getCspaceInstanceId();
        for (TenantBindingType tbt : tenantBindingTypeMap.values()) {
        	//
        	//Loop through all the services in this tenant
        	//
            List<ServiceBindingType> sbtList = tbt.getServiceBindings();
            for (ServiceBindingType sbt: sbtList) {
            	if (sbt.getName().equalsIgnoreCase(RUNSQLSCRIPTS_SERVICE_NAME)) {
            		runInitHandler(cspaceInstanceId, tbt, sbt);
            		return;
            	}
            }
        }
	}

	private void runInitHandler(String cspaceInstanceId, TenantBindingType tbt, ServiceBindingType sbt) throws Exception {
    	String repositoryName = null;
    	if (sbt.getType().equalsIgnoreCase(ServiceBindingUtils.SERVICE_TYPE_SECURITY) == false) {
    		repositoryName = ConfigUtils.getRepositoryName(tbt, sbt.getRepositoryDomain()); // Each service can have a different repo domain
    	}
        //Get the list of InitHandler elements, extract the first one (only one supported right now) and fire it using reflection.
        List<org.collectionspace.services.config.service.InitHandler> list = sbt.getInitHandler();
        if (list != null && list.size() > 0) {
        	org.collectionspace.services.config.service.InitHandler handlerType = list.get(0);  // REM - 12/2012: We might want to think about supporting multiple post-init handlers
            String initHandlerClassname = handlerType.getClassname();
            if (Tools.isEmpty(initHandlerClassname)) {
                return;
            }
            if (ServiceMain.logger.isTraceEnabled()) {
            	ServiceMain.logger.trace(String.format("Firing post-init handler %s ...", initHandlerClassname));
            }

            List<org.collectionspace.services.config.service.InitHandler.Params.Field>
                    fields = handlerType.getParams().getField();

            List<org.collectionspace.services.config.service.InitHandler.Params.Property>
                    props = handlerType.getParams().getProperty();

            //org.collectionspace.services.common.service.InitHandler.Fields ft = handlerType.getFields();
            //List<String> fields = ft.getField();
            Object o = instantiate(initHandlerClassname, IInitHandler.class);
            if (o != null && o instanceof IInitHandler){
                IInitHandler handler = (IInitHandler)o;
                handler.onRepositoryInitialized(JDBCTools.NUXEO_DATASOURCE_NAME, repositoryName, cspaceInstanceId, tbt.getShortName(),
                		sbt, fields, props);
                // The InitHandler may be the default one,
                // or specialized classes which still implement this interface and are registered in tenant-bindings.xml.
            }
        }
	}

    public void firePostInitHandlers() throws Exception {
        Hashtable<String, TenantBindingType> tenantBindingTypeMap = tenantBindingConfigReader.getTenantBindings();

        //
        // We first need to run the init handler for the 'runsqlscripts' service to allow a tenant to perform
        // any required tenant specific SQL setup.
        //
        firePostInitRunSQLScripts(tenantBindingTypeMap);
        
        //
        // Loop through all tenants in tenant-bindings.xml and run each service's initHandler
        //
        String cspaceInstanceId = getCspaceInstanceId();
        for (TenantBindingType tbt : tenantBindingTypeMap.values()) {
        	//
        	//Loop through all the services in this tenant
        	//
            List<ServiceBindingType> sbtList = tbt.getServiceBindings();
            for (ServiceBindingType sbt: sbtList) {
            	if (sbt.getName().equalsIgnoreCase(RUNSQLSCRIPTS_SERVICE_NAME) == false) { // skip the RUNSQLSCRIPTS_SERVICE_NAME since we ran it already
            		runInitHandler(cspaceInstanceId, tbt, sbt);
            	}
            }
        }
    }

    /*
     * A generic mechanism for instantiating a instance/object from a class name.
     */
    public Object instantiate(String clazz, Class<?> castTo) throws Exception {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        clazz = clazz.trim();
        Class<?> c = tccl.loadClass(clazz);
        if (castTo.isAssignableFrom(c)) {
            return c.newInstance();
        }
        return null;
    }

    void retrieveAllWorkspaceIds() throws Exception {
        //all configs are read, connector is initialized, retrieve workspaceids
        Hashtable<String, TenantBindingType> tenantBindings =
        	tenantBindingConfigReader.getTenantBindings();
        TenantRepository.get().setup(tenantBindings);
    }

    /**
     * getWorkspaceId returns workspace id for given tenant and service name
     * @param tenantId
     * @param serviceName
     * @return
     */
    public String getWorkspaceId(String tenantId, String serviceName) {
        return TenantRepository.get().getWorkspaceId(tenantId, serviceName);
    }

    /**
     * @return the nuxeoConnector
     */
    public NuxeoConnectorEmbedded getNuxeoConnector() {
        return nuxeoConnector;
    }

    /**
     * @return the serverRootDir
     */
    public String getServerRootDir() {
        return serverRootDir;
    }

    private String getCspaceServicesConfigDir() {
        return getServerRootDir() + File.separator + JEEServerDeployment.CSPACE_CONFIG_SERVICES_DIR_PATH;
    }

    private String getNuxeoConfigDir() {
        return getServerRootDir() + File.separator + JEEServerDeployment.NUXEO_SERVER_CONFIG_DIR;
    }

    private String getNuxeoProtoConfigFilename() {
        return JEEServerDeployment.NUXEO_PROTOTYPE_REPO_CONFIG_FILENAME;
    }

    private String getNuxeoProtoDatasourceFilename() {
        return JEEServerDeployment.NUXEO_PROTOTYPE_DATASOURCE_FILENAME;
    }

    private String getNuxeoProtoElasticsearchConfigFilename() {
        return JEEServerDeployment.NUXEO_PROTO_ELASTICSEARCH_CONFIG_FILENAME;
    }

    private String getNuxeoProtoElasticsearchExtensionFilename() {
        return JEEServerDeployment.NUXEO_PROTO_ELASTICSEARCH_EXTENSION_FILENAME;
    }

    private String getDatabaseScriptsPath() {
        DatabaseProductType dbType;
        String databaseProductName;
        String databaseScriptsPath = "";
        try {
            // This call makes a connection to the database server, using a default database
            // name and retrieves the database product name from metadata provided by the server.
            dbType = JDBCTools.getDatabaseProductType(JDBCTools.CSADMIN_DATASOURCE_NAME,
    			getServiceConfig().getDbCsadminName());
            databaseProductName = dbType.getName();
            databaseScriptsPath = getServerRootDir() + File.separator
                    + JEEServerDeployment.DATABASE_SCRIPTS_DIR_PATH + File.separator + databaseProductName;
            // An Exception occurring here will cause an empty path to be returned, ultimately
            // resulting in a failure to find the Nuxeo databases initialization script file.
        } catch (Exception e) {
            logger.warn(String.format("Could not get database product type: %s", e.getMessage()));
        }
        return databaseScriptsPath;

    }

    /**
     * Returns the full filesystem path to the Nuxeo databases initialization script file.
     *
     * @return the full path to the Nuxeo databases initialization script file.
     * Returns an empty String for the path if the database scripts path is null or empty.
     */
    private String getNuxeoDatabasesDropScriptFilename() {
        return Tools.notBlank(getDatabaseScriptsPath()) ?
                getDatabaseScriptsPath() + File.separator + JEEServerDeployment.NUXEO_DB_DROP_SCRIPT_FILENAME : "";
    }

    /**
     * @return the server resources path
     */
    public String getServerResourcesPath() {
        return getServerRootDir() + File.separator + ConfigReader.RESOURCES_DIR_PATH + File.separator;
    }

    public InputStream getResourceAsStream(String resourceName) throws FileNotFoundException {
    	InputStream result = new FileInputStream(new File(getServerResourcesPath() + resourceName));
    	return result;
    }

    public String getCspaceInstanceId() {
    	String result = getServiceConfig().getCspaceInstanceId();

    	if (result == null || result.trim().isEmpty()) {
    		result = ""; //empty string
    	}

    	return result;
	}

	/*
		* Look through the tenant bindings and create the required Nuxeo databases -each tenant can declare
		* their own Nuxeo repository/database.
		* Get the NuxeoDS info and create the necessary databases.
		* Consider the tenant bindings to find and get the data sources for each tenant.
		* There may be only one, one per tenant, or something in between.
		*
		*/
	private HashSet<String> createNuxeoDatabases() throws Exception {
		String nuxeoUser = getBasicDataSourceUsername(JDBCTools.NUXEO_DATASOURCE_NAME);
		String nuxeoPW = getBasicDataSourcePassword(JDBCTools.NUXEO_DATASOURCE_NAME);

		String readerUser = getBasicDataSourceUsername(JDBCTools.NUXEO_READER_DATASOURCE_NAME);
		String readerPW = getBasicDataSourcePassword(JDBCTools.NUXEO_READER_DATASOURCE_NAME);

		DatabaseProductType dbType = JDBCTools.getDatabaseProductType(JDBCTools.CSADMIN_DATASOURCE_NAME,
				getServiceConfig().getDbCsadminName());

		Hashtable<String, TenantBindingType> tenantBindings =
				tenantBindingConfigReader.getTenantBindings();
		HashSet<String> nuxeoDBsChecked = new HashSet<String>();

		// First check and create the roles as needed. (nuxeo and reader)
		for (TenantBindingType tenantBinding : tenantBindings.values()) {
			String tId = tenantBinding.getId();
			String tName = tenantBinding.getName();

			List<RepositoryDomainType> repoDomainList = tenantBinding.getRepositoryDomain();
			for (RepositoryDomainType repoDomain : repoDomainList) {
				String repoDomainName = repoDomain.getName();
				String repositoryName = repoDomain.getRepositoryName();
				String cspaceInstanceId = getCspaceInstanceId();
				String dbName = JDBCTools.getDatabaseName(repositoryName, cspaceInstanceId);
				if (nuxeoDBsChecked.contains(dbName)) {
					if (logger.isDebugEnabled()) {
							logger.debug("Another user of db: " + dbName + ": Repo: " + repoDomainName
											+ " and tenant: " + tName + " (id:" + tId + ")");
					}
				} else {
					if (logger.isDebugEnabled()) {
							logger.debug("Need to prepare db: " + dbName + " for Repo: " + repoDomainName
											+ " and tenant: " + tName + " (id:" + tId + ")");
					}
					boolean dbExists = JDBCTools.hasDatabase(dbType, dbName);
					if (dbExists) {
						if (logger.isDebugEnabled()) {
								logger.debug("Database: " + dbName + " already exists.");
						}
					} else {
						// Create the user as needed
						JDBCTools.createNewDatabaseUser(JDBCTools.CSADMIN_DATASOURCE_NAME, repositoryName, cspaceInstanceId, dbType, nuxeoUser, nuxeoPW);
						if (readerUser != null) {
							JDBCTools.createNewDatabaseUser(JDBCTools.CSADMIN_DATASOURCE_NAME, repositoryName, cspaceInstanceId, dbType, readerUser, readerPW);
						}
						// Create the database
						createDatabaseWithRights(dbType, dbName, nuxeoUser, nuxeoPW, readerUser);
						createUtilsSchemaWithRights(dbType, nuxeoUser, repositoryName, cspaceInstanceId);
						initRepositoryDatabaseVersion(JDBCTools.NUXEO_DATASOURCE_NAME, repositoryName, cspaceInstanceId);
					}
					nuxeoDBsChecked.add(dbName);
				}
			} // Loop on repos for tenant
		} // Loop on tenants

		return nuxeoDBsChecked;

	}

	/**
	 * Creates a Nuxeo-managed database, sets up an owner for that
	 * database, and adds (at least) connection privileges to a reader
	 * of that database.
	 *
	 * @param conn
	 * @param dbType
	 * @param dbName
	 * @param ownerName
	 * @param ownerPW
	 * @param readerName
	 * @param readerPW
	 * @throws Exception
	 */
	private void createDatabaseWithRights(DatabaseProductType dbType, String dbName, String ownerName,
			String ownerPW, String readerName) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		
		String sql = null;
		try {
			DataSource csadminDataSource = JDBCTools.getDataSource(JDBCTools.CSADMIN_DATASOURCE_NAME);
			conn = csadminDataSource.getConnection();
			stmt = conn.createStatement();
			if (dbType == DatabaseProductType.POSTGRESQL) {
				// PostgreSQL does not need passwords in grant statements.
				sql = "CREATE DATABASE " + dbName + " ENCODING 'UTF8' OWNER " + ownerName;
				stmt.executeUpdate(sql);
				if (logger.isDebugEnabled()) {
					logger.debug("Created db: '" + dbName + "' with owner: '" + ownerName + "'");
				}
				if (readerName != null) {
					sql = "GRANT CONNECT ON DATABASE " + dbName + " TO " + readerName;
					stmt.executeUpdate(sql);
					if (logger.isDebugEnabled()) {
						logger.debug(" Granted connect rights on: '" + dbName + "' to reader: '" + readerName + "'");
					}
				}
				// Note that select rights for reader must be granted after
				// Nuxeo startup.
			} else {
				throw new UnsupportedOperationException(String.format("", dbType));
			}
		} catch (Exception e) {
			String errMsg = String.format("The following SQL statement failed using credentials from datasource '%s': %s",
					JDBCTools.CSADMIN_DATASOURCE_NAME, sql);
			logger.error("createDatabaseWithRights failed on exception: " + e.getLocalizedMessage());
			if (errMsg != null) {
				logger.error(errMsg);
			}
			throw e; // propagate
		} finally { // close resources
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
	
	/*
	 * For a specific repo/db, create a schema for misc SQL functions
	 */
	private void createUtilsSchemaWithRights(DatabaseProductType dbType, String ownerName,
			String repositoryName, String cspaceInstanceId) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		
		String sql = null;
		try {
			conn = JDBCTools.getConnection(JDBCTools.NUXEO_DATASOURCE_NAME, repositoryName, cspaceInstanceId);
			stmt = conn.createStatement();
			if (dbType == DatabaseProductType.POSTGRESQL) {
				sql = "CREATE SCHEMA IF NOT EXISTS " + CSPACE_UTILS_SCHEMANAME + " AUTHORIZATION " + ownerName;
				stmt.executeUpdate(sql);
				if (logger.isDebugEnabled()) {
					logger.debug("Created SCHEMA: '" + CSPACE_UTILS_SCHEMANAME + "' with owner: '" + ownerName + "'");
				}
			} else {
				throw new UnsupportedOperationException("CollectionSpace supports only PostgreSQL database servers.");
			}
		} catch (Exception e) {
			String errMsg = String.format("The following SQL statement failed using credentials from datasource '%s': %s",
					JDBCTools.NUXEO_DATASOURCE_NAME, sql);
			logger.error("createUtilsSchemaWithRights() failed with exception: " + e.getLocalizedMessage());
			if (errMsg != null) {
				logger.error(errMsg);
			}
			throw e; // propagate
		} finally { // close resources
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

	private void initRepositoryDatabaseVersion(String dataSourceName, String repositoryName, String cspaceInstanceId) throws Exception {
		String version = ServiceMain.VER_MAJOR + "." + ServiceMain.VER_MINOR + "." + ServiceMain.VER_PATCH;
		Connection conn = null;

		try {
			conn = JDBCTools.getConnection(dataSourceName, repositoryName, cspaceInstanceId);

			JDBCTools.setRepositoryDatabaseVersion(conn, version);
		}
		finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

        private BasicDataSource getBasicDataSource(String dataSourceName) {
            BasicDataSource basicDataSource = null;
            if (Tools.isBlank(dataSourceName)) {
                return basicDataSource;
            }
            try {
                DataSource dataSource = JDBCTools.getDataSource(dataSourceName);
                basicDataSource = (BasicDataSource) dataSource;
            } catch (NamingException ne) {
                logger.warn("Error attempting to retrieve basic datasource '%s': %s",
                        dataSourceName, ne.getMessage());
                return basicDataSource;
            }
            return basicDataSource;
        }

        private String getBasicDataSourceUsername(String dataSourceName) {
            String username = null;
            BasicDataSource basicDataSource = getBasicDataSource(dataSourceName);
            if (basicDataSource == null) {
                return username;
            }
            username = basicDataSource.getUsername();
            return username;
        }

        private String getBasicDataSourcePassword(String dataSourceName) {
            String password = null;
            BasicDataSource basicDataSource = getBasicDataSource(dataSourceName);
            if (basicDataSource == null) {
                return password;
            }
            password = basicDataSource.getPassword();
            return password;
        }

    /*
     * This might be useful for something, but the reader grants are better handled in the ReportPostInitHandler.
     *
     *
     */
/*
    private void handlePostNuxeoInitDBTasks() throws Exception {
    	Statement stmt = null;
		Connection conn = null;

    	try {
        	DataSource nuxeoMgrDataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_MANAGER_DATASOURCE_NAME);
        	DataSource nuxeoReaderDataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_READER_DATASOURCE_NAME);

        	if(nuxeoReaderDataSource!=null) {
	        	// We need to fetch the user name and password from the nuxeoDataSource, to do grants below
	        	org.apache.tomcat.dbcp.dbcp.BasicDataSource tomcatDataSource =
	        			(org.apache.tomcat.dbcp.dbcp.BasicDataSource)nuxeoReaderDataSource;
	        	// Get the template URL value from the JNDI datasource and substitute the databaseName
	        	String readerUser = tomcatDataSource.getUsername();
	        	DatabaseProductType dbType = JDBCTools.getDatabaseProductType(
	        			JDBCTools.CSPACE_DATASOURCE_NAME,
	        			JDBCTools.DEFAULT_CSPACE_DATABASE_NAME); // only returns PG or MYSQL

	    		conn = nuxeoMgrDataSource.getConnection();
	        	stmt = conn.createStatement();
	    		if(dbType==DatabaseProductType.POSTGRESQL) {
	    			// Note that select rights for reader must be granted after Nuxeo startup.
	    			String sql = "GRANT SELECT ON ALL TABLES IN SCHEMA public TO "+readerUser;
	    			stmt.executeUpdate(sql);
	    			if (logger.isDebugEnabled()) {
	    				logger.debug(" Granted SELECT rights on all public tables to reader: '"+readerUser+"'");
	    			}
	    		} else if(dbType==DatabaseProductType.MYSQL) {
	    		} else {
	    			throw new UnsupportedOperationException("handlePostNuxeoInitDBTasks only supports Postgres/MySQL.");
	    		}
        	}
    	} catch(Exception e) {
    		logger.error("handlePostNuxeoInitDBTasks failed on exception: " + e.getLocalizedMessage());
    		throw e;	// propagate
    	} finally {   //close resources
    		try {
    			if(stmt!=null) {
    				stmt.close();
    			}
    		} catch(SQLException se) {
    			// nothing we can do
    		}
    	}

    }
*/

    private void setServerRootDir() {
        serverRootDir = System.getProperty(SERVER_HOME_PROPERTY);
        if (serverRootDir == null) {
            serverRootDir = "."; //assume server is started from server root, e.g. server/cspace
            String msg = String.format("System property '%s' was not set.  Using '%s' instead.",
            		SERVER_HOME_PROPERTY, serverRootDir);
            mirrorToStdOut(msg);
        }
    }


    /**
     * @return the serviceConfig
     */
    public ServiceConfig getServiceConfig() {
        return getServicesConfigReader().getConfiguration();
    }

    /**
     * @return the clientType
     */
    public ClientType getClientType() {
        return getServicesConfigReader().getClientType();
    }

    /**
     * @return the servicesConfigReader
     */
    public ServicesConfigReaderImpl getServicesConfigReader() {
        return servicesConfigReader;
    }

    /**
     * @return the tenantBindingConfigReader
     */
    public TenantBindingConfigReaderImpl getTenantBindingConfigReader() {
        return tenantBindingConfigReader;
    }

    public ResourceMap getJaxRSResourceMap() {
        ResourceMap result;

        result = ResteasyProviderFactory.getContextData(ResourceMap.class);

        return result;
    }

    /**
     *  Populate a registry of URI templates by querying each resource
     *  for its own entries in the registry.
     *
     *  These entries consist of one or more URI templates associated
     *  with that resource, for building URIs to access that resource.
     */
    private synchronized void populateUriTemplateRegistry() {
       if (uriTemplateRegistry.isEmpty()) {
    	   CollectionSpaceResource<?, ?> resource = null;
            ResourceMap resourceMap = getJaxRSResourceMap();
            Set<Map.Entry<String, CollectionSpaceResource<?, ?>>> entrySet = resourceMap.entrySet();
            for (Map.Entry<String, CollectionSpaceResource<?, ?>> entry : entrySet) {
                resource = entry.getValue();
                Map<UriTemplateRegistryKey, StoredValuesUriTemplate> entries =
                        resource.getUriRegistryEntries();
                uriTemplateRegistry.putAll(entries);
            }
            // FIXME: Contacts itself should not have an entry in the URI template registry;
            // there should be a Contacts entry in that registry only for use in
            // building URIs for resources that have contacts as a sub-resource
            // (This may also fall out during implementation of CSPACE-2698.)
       }
    }

    public UriTemplateRegistry getUriTemplateRegistry() {
        if (uriTemplateRegistry.isEmpty()) {
            populateUriTemplateRegistry();
        }
        return uriTemplateRegistry;
    }

	/**
     *
     */
	private void createOrUpdateNuxeoDatasourceConfigFiles() throws Exception {
		// Get the prototype copy of the Nuxeo datasource config file.
		File prototypeNuxeoDatasourceFile = new File(getCspaceServicesConfigDir() + File.separator
				+ getNuxeoProtoDatasourceFilename());
		// FIXME: Consider checking for the presence of existing configuration files,
		// rather than always failing outright if the prototype file for creating
		// new or updated files can't be located.
		if (prototypeNuxeoDatasourceFile.canRead() == false) {
			String msg = String
					.format("Could not find and/or read the prototype Nuxeo datasource file '%s'. "
							+ "Please redeploy this file by running 'ant deploy' from the Services layer source code's '3rdparty/nuxeo' module.",
							prototypeNuxeoDatasourceFile.getCanonicalPath());
			throw new RuntimeException(msg);
		}

		if (logger.isInfoEnabled()) {
			logger.info(String.format("Using prototype Nuxeo server configuration file at path %s",
					prototypeNuxeoDatasourceFile.getAbsolutePath()));
		}
		
		//
		// If multiple active tenants, set the "default" repository for Nuxeo services to use.
		//
		setDefaultNuxeoRepository();

		//
		// For each tenant config we find, create the xml datasource config file and fill in the correct values.
		//
		Document prototypeConfigDoc = XmlTools.fileToXMLDocument(prototypeNuxeoDatasourceFile);
		Document datasourceConfigDoc = null;

		Hashtable<String, TenantBindingType> tenantBindingTypeMap = tenantBindingConfigReader.getTenantBindings();
		for (TenantBindingType tbt : tenantBindingTypeMap.values()) {
			List<String> repositoryNameList = ConfigUtils.getRepositoryNameList(tbt);
			logger.debug("Getting repository name(s) for tenant " + tbt.getName());

			if (repositoryNameList == null || repositoryNameList.isEmpty() == true) {
				logger.error(String.format("Could not get repository name(s) for tenant %s", tbt.getName()));
				continue; // break out of loop and go to the next tenant binding
			} else {
				for (String repositoryName : repositoryNameList) {
					if (Tools.isBlank(repositoryName)) {
						logger.error(String.format("Repository name(s) for tenant %s was empty.", tbt.getName()));
						continue;
					}
					logger.debug(String.format("Repository name is %s", repositoryName));
					//
					// Clone the prototype copy of the Nuxeo datasource config file,
					// thus creating a separate config file for the current repository.
					//
					datasourceConfigDoc = (Document) prototypeConfigDoc.clone();
					// Update this config file by inserting values pertinent to the
					// current repository.
					datasourceConfigDoc = updateRepositoryDatasourceDoc(datasourceConfigDoc, repositoryName, tbt.getRepositoryDomain(), this.getCspaceInstanceId());
					logger.debug("Updated Nuxeo datasource config file contents=\n" + datasourceConfigDoc.asXML());

					// Write this config file to the Nuxeo server config directory.
					File repofile = new File(getNuxeoConfigDir() + File.separator + repositoryName
							+ JEEServerDeployment.NUXEO_DATASOURCE_CONFIG_FILENAME_SUFFIX);
					logger.debug(String.format("Attempting to write Nuxeo datasource config file to %s", repofile.getAbsolutePath()));
					XmlTools.xmlDocumentToFile(datasourceConfigDoc, repofile);
				}
			}
		}
	}

	private boolean isImpliedDefaultRepository(RepositoryDomainType repositoryDomain) {
		boolean result = false;

		String repoName = repositoryDomain.getRepositoryName();
		if (repoName == null || repoName.equals(ConfigUtils.DEFAULT_NUXEO_REPOSITORY_NAME)) {
			result = true;
		}

		return result;
	}

	private void setDefaultNuxeoRepository() throws Exception {
		boolean moreThanOne = false;
		boolean defaultIsSet = false;
		String defaultRepositoryName = null;
		
		Hashtable<String, TenantBindingType> tenantBindingTypeMap = tenantBindingConfigReader.getTenantBindings();
		
		//
		// Ensure we have at least one tenant binding and at least one corresponding repository domain 
		//
		if (tenantBindingTypeMap.values().size() == 0) {
			String msg = "At least one tenant binding must be configured.";
			throw new Exception(msg);
		}
		
		//
		// If we have just one tenant, make its (or one of its) repository domain(s) the default one.
		//
		if (tenantBindingTypeMap.values().size() == 1) {
			TenantBindingType tbt = (TenantBindingType) tenantBindingTypeMap.values().toArray()[0];
			List<RepositoryDomainType> repositoryDomainList = tbt.getRepositoryDomain();
			for (RepositoryDomainType repositoryDomain : repositoryDomainList) {
    			if (repositoryDomain.isDefaultRepository() && defaultIsSet == false) {
    				defaultIsSet = true;
    				defaultRepositoryName = repositoryDomain.getRepositoryName();
    			} else if (repositoryDomain.isDefaultRepository() && defaultIsSet == true) {
    				moreThanOne = true;
    				String msg = String.format("The tenant '%s' configuration is declaring '%s' the default repository.  However, '%s' is already the default repository.  Please ensure only one tenant is configured to be the default repository.",
    						tbt.getName(), repositoryDomain.getRepositoryName(), defaultRepositoryName);
    				logger.error(msg);
    			}
			}
			
			if (moreThanOne == true) {
				String msg = String.format("The tenant '%s' has more than one repository domain configured to be the default.  Please configure only one default repository.", 
						tbt.getName());
				throw new Exception(msg);
			}
			
			//
			// If the only active tenant is not explicitly configuring a repository domain as default, do so now.
			//
			if (defaultIsSet == false) {
				RepositoryDomainType repositoryDomain = repositoryDomainList.get(0);
				repositoryDomain.setDefaultRepository(Boolean.TRUE);
				defaultIsSet = true;
				defaultRepositoryName = repositoryDomain.getRepositoryName();
			}
			
			if (logger.isDebugEnabled()) {
				String msg = String.format("The tenant '%s' has configured the default repository to be '%s'.", 
						tbt.getName(), defaultRepositoryName);
				logger.debug(msg);
			}
			
			return;
		}
		
		//
		// If we have multiple tenants, figure out which one is declaring the default repository.
		//
		for (TenantBindingType tbt : tenantBindingTypeMap.values()) {
			List<RepositoryDomainType> repositoryDomainList = tbt.getRepositoryDomain();
			for (RepositoryDomainType repositoryDomain : repositoryDomainList) {
    			if (repositoryDomain.isDefaultRepository() && defaultIsSet == false) {
    				repositoryDomain.setDefaultRepository(Boolean.TRUE);
    				defaultIsSet = true;
    				defaultRepositoryName = repositoryDomain.getRepositoryName();
    			} else if (repositoryDomain.isDefaultRepository() && defaultIsSet == true) {
    				moreThanOne = true;
    				String msg = String.format("The tenant '%s' configuration is declaring itself as the default repository domain.  However, another tenant has already declared '%s' to be the default repository.  Please ensure only one tenant is configured to have the default repository.",
    						tbt.getName(), defaultRepositoryName);
    				logger.error(msg);
    			}
			}
		}
		
		//
		// If more than one tenant has declared itself the default repository domain then
		// throw an exception
		//
		if (moreThanOne == true) {
			String msg = "More than one tenant is configured to be the repository domain.  Please configure only one default repository.";
			throw new Exception(msg);
		}
		
		//
		// If no tenant has declared itself the default repository, look for an "implied" default.  Tenants configured with
		// no repository name are inferred to be the default repository.  If more than one tenant is configured without a repository name,
		// we'll use the first one found.
		//
		if (defaultIsSet == false) {
    		for (TenantBindingType tbt : tenantBindingTypeMap.values()) {
    			List<RepositoryDomainType> repositoryDomainList = tbt.getRepositoryDomain();
    			for (RepositoryDomainType repositoryDomain : repositoryDomainList) {
        			if (isImpliedDefaultRepository(repositoryDomain) && defaultIsSet == false) {
        				repositoryDomain.setDefaultRepository(Boolean.TRUE);
        				defaultIsSet = true;
        				defaultRepositoryName = repositoryDomain.getRepositoryName();
        			} else if (isImpliedDefaultRepository(repositoryDomain) && defaultIsSet == true) {
        				moreThanOne = true;
        				String msg = String.format("The tenant '%s' configuration implies (no repository name was defined) it is the default repository domain.  However, another tenant's domain has implied '%s' is the default repository.  Please ensure only one tenant defines the default repository.",
        						tbt.getName(), defaultRepositoryName);
        				logger.error(msg);
        			}
    			}
    		}
		}

		//
		// As of v6.0, this is just a warning.  However, future versions may require a default repository.
		//
		if (defaultIsSet == false) {
			logger.warn("No tenant's configuration explicitly declared nor implied its repository to be the default.  Future versions of CollectionSpace may require a default repository.");
		} else if (logger.isDebugEnabled()) {
			String msg = String.format("The default repository has been set to '%s'.", defaultRepositoryName);
			logger.debug(msg);
		}
	}

	private File getProtoElasticsearchConfigFile() throws Exception {
		File result = new File(getCspaceServicesConfigDir() + File.separator
				+ getNuxeoProtoElasticsearchConfigFilename());
		// FIXME: Consider checking for the presence of existing configuration
		// files, rather than always failing outright if the prototype file for
		// creating new or updated files can't be located.
		if (result.canRead() == false) {
			String msg = String
					.format("Could not find and/or read the prototype Elasticsearch config file '%s'. "
							+ "Please redeploy this file by running 'ant deploy' from the Services layer source code's '3rdparty/nuxeo' module.",
							result.getCanonicalPath());
			throw new RuntimeException(msg);
		}

		if (logger.isInfoEnabled()) {
			logger.info(String.format(String.format("Using the prototype Elasticsearch configuration file at path '%s'.",
					result.getAbsolutePath())));
		}

		return result;
	}

	private File getProtoElasticsearchExtensionFile() throws Exception {
		File result = new File(getCspaceServicesConfigDir() + File.separator
				+ getNuxeoProtoElasticsearchExtensionFilename());
		// FIXME: Consider checking for the presence of existing configuration
		// files, rather than always failing outright if the prototype file for
		// creating new or updated files can't be located.
		if (result.canRead() == false) {
			String msg = String
					.format("Could not find and/or read the prototype Elasticsearch extension file '%s'. "
							+ "Please redeploy this file by running 'ant deploy' from the Services layer source code's '3rdparty/nuxeo' module.",
							result.getCanonicalPath());
			throw new RuntimeException(msg);
		}

		if (logger.isInfoEnabled()) {
			logger.info(String.format("Using the prototype Elasticsearch extension file at path %s",
					result.getAbsolutePath()));
		}

		return result;
	}

	private void createOrUpdateNuxeoElasticsearchConfigFiles() throws Exception {
		//
		// Get the prototype copy of the Nuxeo Elasticsearch extension element and
		// fill it in for each tenant
		//
		File protoElasticsearchExtensionFile = getProtoElasticsearchExtensionFile();
		StringBuffer extensionList = new StringBuffer();
		Hashtable<String, TenantBindingType> tenantBindingTypeMap = tenantBindingConfigReader.getTenantBindings();
		for (TenantBindingType tbt : tenantBindingTypeMap.values()) {
			List<String> repositoryNameList = ConfigUtils.getRepositoryNameList(tbt);
			logger.debug("Getting repository name(s) for tenant " + tbt.getName());

			if (repositoryNameList == null || repositoryNameList.isEmpty() == true) {
				logger.error(String.format("Could not get repository name(s) for tenant %s", tbt.getName()));
				continue; // break out of loop and go to the next tenant binding
			} else {
				for (String repositoryName : repositoryNameList) {
					if (Tools.isBlank(repositoryName)) {
						logger.error(String.format("Repository name(s) for tenant %s was empty.", tbt.getName()));
					} else {
						logger.debug(String.format("Repository name is %s", repositoryName));
						Document protoElasticsearchExtensionDoc = XmlTools.fileToXMLDocument(protoElasticsearchExtensionFile);

						protoElasticsearchExtensionDoc = updateElasticSearchExtensionDoc(protoElasticsearchExtensionDoc, repositoryName, this.getCspaceInstanceId(), tbt.getElasticSearchIndexConfig());
						if (logger.isDebugEnabled()) {
							String extension = protoElasticsearchExtensionDoc.asXML();
							logger.trace(String.format("Updated Elasticsearch extension for '%s' repository: contents=\n", repositoryName, extension));
						}
						//extensionList.append(protoElasticsearchExtensionDoc.asXML() + '\n');
						extensionList.append(XmlTools.asXML(protoElasticsearchExtensionDoc, true) + '\n');
					}
				}
			}
		}

		//
		// Create the final Nuxeo Elasticsearch configuration file and deploy it to the Nuxeo server.
		//
		if (extensionList.length() > 0) {
			// Get the prototype copy of the Nuxeo Elasticsearch config file.
			String str = FileUtils.readFileToString(getProtoElasticsearchConfigFile());
			str = str.replace(ConfigUtils.ELASTICSEARCH_EXTENSIONS_EXPANDER_STR, extensionList);

			//
			// Create the final xml Elasticsearch config and fill in the correct values.
			//
			File elasticSearchConfigFile = new File(getNuxeoConfigDir() + File.separator
					+ JEEServerDeployment.NUXEO_ELASTICSEARCH_CONFIG_FILENAME);
			FileUtilities.StringToFile(str, elasticSearchConfigFile);
		} else {
			logger.error("Could not create Elasticsearch configuration files.  Check that the prototype configuration files are properly formatted and in the correct location.");
		}
	}

   /**
    * Ensure that Nuxeo repository configuration files exist for each repository
    * specified in tenant bindings. Create or update these files, as needed.
    */
    private void createOrUpdateNuxeoRepositoryConfigFiles() throws Exception {

        // Get the prototype copy of the Nuxeo repository config file.
        File prototypeNuxeoConfigFile =
                new File(getCspaceServicesConfigDir() + File.separator + getNuxeoProtoConfigFilename());
        // FIXME: Consider checking for the presence of existing configuration files,
        // rather than always failing outright if the prototype file for creating
        // new or updated files can't be located.
        if (! prototypeNuxeoConfigFile.canRead()) {
            String msg = String.format("Could not find and/or read the prototype Nuxeo config file '%s'. "
                    + "Please redeploy this file by running 'ant deploy' from the Services layer source code's '3rdparty/nuxeo' module.",
                    prototypeNuxeoConfigFile.getCanonicalPath());
            throw new RuntimeException(msg);
        }
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Using prototype Nuxeo config file at path %s",
            		prototypeNuxeoConfigFile.getAbsolutePath()));
        }

        //
        // Create the xml config file and fill in the correct values.
        //
        Document prototypeConfigDoc = XmlTools.fileToXMLDocument(prototypeNuxeoConfigFile);
        Document repositoryConfigDoc = null;
        // FIXME: Can the variable below reasonably be made a class variable? Its value
        // is used in at least one other method in this class.
        Hashtable<String, TenantBindingType> tenantBindingTypeMap = tenantBindingConfigReader.getTenantBindings();
        for (TenantBindingType tbt : tenantBindingTypeMap.values()) {
            List<String> repositoryNameList = ConfigUtils.getRepositoryNameList(tbt);
            if (logger.isTraceEnabled()) {
                logger.trace("Getting repository name(s) for tenant " + tbt.getName());
            }
            if (repositoryNameList == null || repositoryNameList.isEmpty() == true) {
              logger.warn(String.format("Could not get repository name(s) for tenant %s", tbt.getName()));
              continue; //break out of loop
            } else {
                for (String repositoryName : repositoryNameList) {
                    if (Tools.isBlank(repositoryName)) {
                        continue;
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace(String.format("Repository name is %s", repositoryName));
                    }
                    // FIXME: As per above, we might check for the presence of an existing
                    // config file for this repository and, if present, not fail even if
                    // the code below fails to update that file on any given system startup.
                    //
                    // Clone the prototype copy of the Nuxeo repository config file,
                    // thus creating a separate config file for the current repository.
                    repositoryConfigDoc = (Document) prototypeConfigDoc.clone();
                    // Update this config file by inserting values pertinent to the
                    // current repository.
                    String binaryStorePath = tbt.getBinaryStorePath();
                    repositoryConfigDoc = updateRepositoryConfigDoc(repositoryConfigDoc, repositoryName,
                    		this.getCspaceInstanceId(), binaryStorePath);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Updated Nuxeo repo config file contents=\n" + repositoryConfigDoc.asXML());
                    }
                    // Write this config file to the Nuxeo server config directory.
                    File repofile = new File(getNuxeoConfigDir() + File.separator +
                            repositoryName + JEEServerDeployment.NUXEO_REPO_CONFIG_FILENAME_SUFFIX);
                    if (logger.isTraceEnabled()) {
                        logger.trace(String.format("Attempting to write Nuxeo repo config file to %s", repofile.getAbsolutePath()));
                    }
                    XmlTools.xmlDocumentToFile(repositoryConfigDoc, repofile);
                }
            }
        }
    }

    /*
     * This method is filling out the proto-repo-config.xml file with tenant specific repository information.
     */
    private Document updateRepositoryConfigDoc(Document repoConfigDoc, String repositoryName,
    		String cspaceInstanceId, String binaryStorePath) {
        String databaseName = JDBCTools.getDatabaseName(repositoryName, cspaceInstanceId);

        repoConfigDoc = XmlTools.setAttributeValue(repoConfigDoc, "/component", "name",
                String.format("config:%s-repository", repositoryName));

        repoConfigDoc = XmlTools.setAttributeValue(repoConfigDoc,
        		ConfigUtils.CONFIGURATION_EXTENSION_POINT_XPATH + "/blobprovider", "name",
                repositoryName);

        repoConfigDoc = XmlTools.setElementValue(repoConfigDoc,
        		ConfigUtils.CONFIGURATION_EXTENSION_POINT_XPATH + "/blobprovider/property[@name='path']",
        			Tools.isBlank(binaryStorePath) ? repositoryName : binaryStorePath);

        // Text substitutions within first extension point, "repository"
        repoConfigDoc = XmlTools.setAttributeValue(repoConfigDoc,
        		ConfigUtils.REPOSITORY_EXTENSION_POINT_XPATH + "/repository", "name",
                repositoryName);

//        repoConfigDoc = XmlTools.setAttributeValue(repoConfigDoc,
//        		ConfigUtils.REPOSITORY_EXTENSION_POINT_XPATH + "/repository/binaryStore", "path",
//                Tools.isBlank(binaryStorePath) ? repositoryName : binaryStorePath);  // Can be either partial or full path.  Partial path will be relative to Nuxeo's data directory

        /* Create the JDBC url options if any exist */
        String jdbcOptions = XmlTools.getElementValue(repoConfigDoc,
        		ConfigUtils.REPOSITORY_EXTENSION_POINT_XPATH + "/repository/property[@name='JDBCOptions']");
        jdbcOptions = Tools.isBlank(jdbcOptions) ? "" : "?" + jdbcOptions;

        repoConfigDoc = XmlTools.setElementValue(repoConfigDoc,
        		ConfigUtils.REPOSITORY_EXTENSION_POINT_XPATH + "/repository/property[@name='DatabaseName']",
                databaseName + jdbcOptions);

        return repoConfigDoc;
    }

    /*
     * This method is filling out the proto-datasource-config.xml file with tenant specific repository information.
     */
    private Document updateRepositoryDatasourceDoc(Document repoConfigDoc, String repositoryName, List<RepositoryDomainType> repoDomainList,
    		String cspaceInstanceId) {

    	boolean isDefaultRepository = ConfigUtils.containsDefaultRepository(repoDomainList);
        String databaseName = JDBCTools.getDatabaseName(repositoryName, cspaceInstanceId);

        repoConfigDoc = XmlTools.setAttributeValue(repoConfigDoc, "/component", "name",
                String.format("config:%s-datasource", repositoryName));

        // Set the <datasource> element's  name attribute
        String datasoureName = "jdbc/" + repositoryName;
        repoConfigDoc = XmlTools.setAttributeValue(repoConfigDoc,
        		ConfigUtils.DATASOURCE_EXTENSION_POINT_XPATH + "/datasource", "name", datasoureName);

        // Get the DB server name
        String serverName = XmlTools.getElementValue(repoConfigDoc,
        		ConfigUtils.DATASOURCE_EXTENSION_POINT_XPATH + "/property[@name='ServerName']");
        // Get the JDBC options
        String jdbcOptions = XmlTools.getElementValue(repoConfigDoc,
        		ConfigUtils.DATASOURCE_EXTENSION_POINT_XPATH + "/property[@name='JDBCOptions']");
        jdbcOptions = Tools.isBlank(jdbcOptions) ? "" : "?" + jdbcOptions;
        // Get the DB port nubmer
        String portNumber = XmlTools.getElementValue(repoConfigDoc,
        		ConfigUtils.DATASOURCE_EXTENSION_POINT_XPATH + "/property[@name='PortNumber']");
        // Build the JDBC URL from the parts
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s%s", //FIXME: 'postgresql' string should not be hard coded here
        		serverName, portNumber, databaseName, jdbcOptions);

        // Set the <datasource> element's url attribute
        repoConfigDoc = XmlTools.setAttributeValue(repoConfigDoc,
        		ConfigUtils.DATASOURCE_EXTENSION_POINT_XPATH + "/datasource", "url", jdbcUrl);
        logger.debug(String.format("Built up the following JDBC url: %s", jdbcUrl));

        // Get the DB username
        String username = XmlTools.getElementValue(repoConfigDoc,
        		ConfigUtils.DATASOURCE_EXTENSION_POINT_XPATH + "/property[@name='User']");
        // Set the <datasource> element's user attribute
        repoConfigDoc = XmlTools.setAttributeValue(repoConfigDoc,
        		ConfigUtils.DATASOURCE_EXTENSION_POINT_XPATH + "/datasource", "username", username);

        // Get the DB password
        String password = XmlTools.getElementValue(repoConfigDoc,
        		ConfigUtils.DATASOURCE_EXTENSION_POINT_XPATH + "/property[@name='Password']");
        // Set the <datasource> element's password attribute
        repoConfigDoc = XmlTools.setAttributeValue(repoConfigDoc,
        		ConfigUtils.DATASOURCE_EXTENSION_POINT_XPATH + "/datasource", "password", password);

        //
        // Adjust various Nuxeo components' datasource links to use the tenant repository.
        // In a multi-tenant deployment, it is unclear if there will be name clashes -i.e., two or more tenants declaring
        // themselves to be the datasource for a Nuxeo component.
        //
        List<DefaultElement> linkNodes = XmlTools.getElementNodes(repoConfigDoc, ConfigUtils.DATASOURCE_EXTENSION_POINT_XPATH + "/link");
        for (DefaultElement node : linkNodes) {
        	Attribute nameAttribute = node.attribute("name");
        	if (nameAttribute.getValue().equals(ConfigUtils.CS_TENANT_DATASOURCE_VALUE)) {
        		nameAttribute.setValue("jdbc/repository_" + repositoryName);
        	}
        	Attribute globalAttribute = node.attribute("global");
       		globalAttribute.setValue(datasoureName);
        }

        return repoConfigDoc;
    }

	private String getElasticsearchIndexName(Document repoConfigDoc, String repositoryName, String cspaceInstanceId) {
		String repo = repositoryName.equalsIgnoreCase(ConfigUtils.DEFAULT_NUXEO_REPOSITORY_NAME)
			? ConfigUtils.DEFAULT_ELASTICSEARCH_INDEX_NAME
			: repositoryName;

		return repo + cspaceInstanceId;
	}

    /*
     * This method is filling out the elasticsearch-config.xml file with tenant specific repository information.
     */
    private Document updateElasticSearchExtensionDoc(Document elasticsearchConfigDoc, String repositoryName,
    		String cspaceInstanceId, ElasticSearchIndexConfig elasticSearchIndexConfig) {

        // Set the <elasticSearchIndex> element's  name attribute
        String indexName = getElasticsearchIndexName(elasticsearchConfigDoc, repositoryName, cspaceInstanceId);
        elasticsearchConfigDoc = XmlTools.setAttributeValue(elasticsearchConfigDoc,
        		ConfigUtils.ELASTICSEARCH_INDEX_EXTENSION_XPATH + "/elasticSearchIndex", "name", indexName);

        // Set the <elasticSearchIndex> element's repository attribute
        elasticsearchConfigDoc = XmlTools.setAttributeValue(elasticsearchConfigDoc,
        		ConfigUtils.ELASTICSEARCH_INDEX_EXTENSION_XPATH + "/elasticSearchIndex", "repository", repositoryName);

        if (elasticSearchIndexConfig != null) {
            String settings = elasticSearchIndexConfig.getSettings();
            String mapping = elasticSearchIndexConfig.getMapping();

            if (settings != null) {
                XmlTools.setElementValue(elasticsearchConfigDoc, ConfigUtils.ELASTICSEARCH_INDEX_EXTENSION_XPATH + "/elasticSearchIndex/settings", settings);
            }

            if (mapping != null) {
                XmlTools.setElementValue(elasticsearchConfigDoc, ConfigUtils.ELASTICSEARCH_INDEX_EXTENSION_XPATH + "/elasticSearchIndex/mapping", mapping);
            }
        }

        return elasticsearchConfigDoc;
    }

    /**
     * Update the current copy of the Nuxeo databases initialization script file by
     *
     * <ul>
     *   <li>Removing all existing DROP DATABASE commands</li>
     *   <li>Removing all existing DROP USER commands</li>
     *   <li>Adding a DROP DATABASE command for each current database, at the top of that file.</li>
     *   <li>Adding DROP USER commands for each provided datasource, following the DROP DATABASE commands.</li>
     * </ul>
     *
     * @param dbInitializationScriptFilePath
     * @param dbsCheckedOrCreated
     * @throws Exception
     */
    private void updateInitializationScript(String dbInitializationScriptFilePath, HashSet<String> dbsCheckedOrCreated,
			String[] dataSourceNames) throws Exception {
    	//
    	// Get the current copy of the Nuxeo databases initialization script
		// file and read all of its lines except for those which DROP databases.
		//
    	File nuxeoDatabasesInitScriptFile = new File(dbInitializationScriptFilePath);
		List<String> lines = null;
		try {
			if (!nuxeoDatabasesInitScriptFile.canRead()) {
				String msg = String.format("Could not find and/or read the Nuxeo databases initialization script file '%s'",
						nuxeoDatabasesInitScriptFile.getCanonicalPath());
				logger.warn(msg);
			} else {
				//
				// Make a backup of the existing file
				String destFileName = String.format("%s.%s.bak", dbInitializationScriptFilePath, System.currentTimeMillis());
				if (FileTools.copyFile(dbInitializationScriptFilePath, destFileName, false) == false) {
					throw new Exception("Could not backup existing database initialization script.");
				}

				//
				// Process the existing lines
				lines = FileTools.readFileAsLines(dbInitializationScriptFilePath);
				Iterator<String> linesIterator = lines.iterator();
				String currentLine;
				while (linesIterator.hasNext()) {
					currentLine = linesIterator.next();
					// Elide all existing DROP DATABASE statements.
					if (currentLine.toLowerCase().contains(DROP_DATABASE_SQL_CMD.toLowerCase())) {
						linesIterator.remove();
					}
					// Elide a comment pertaining to the existing
					// DROP DATABASE statements.
					if (currentLine.toLowerCase().contains(DROP_OBJECTS_SQL_COMMENT.toLowerCase())) {
						linesIterator.remove();
					}
					// Elide all existing DROP USER statements.
					if (currentLine.toLowerCase().contains(DROP_USER_SQL_CMD.toLowerCase())) {
						linesIterator.remove();
					}
				}
			}

			// Add back the comment elided above
			List<String> replacementLines = new ArrayList<String>();
			replacementLines.add(DROP_OBJECTS_SQL_COMMENT);
			// Add new DROP DATABASE lines for every Nuxeo-managed database.
			for (String dbName : dbsCheckedOrCreated) {
				if (Tools.notBlank(dbName)) {
					replacementLines.add(String.format(DROP_DATABASE_IF_EXISTS_SQL_CMD, dbName));
				}
			}

			// Add new DROP USER commands for every provided datasource.
			String username;
			for (String dataSourceName : dataSourceNames) {
				username = getBasicDataSourceUsername(dataSourceName);
				if (Tools.notBlank(username)) {
					replacementLines.add(String.format(DROP_USER_IF_EXISTS_SQL_CMD, username));
				}
			}

			// Now append all existing lines from that file, except for
			// any lines that were elided above.
			if (lines != null && !lines.isEmpty()) {
				replacementLines.addAll(lines);
			}

			if (!nuxeoDatabasesInitScriptFile.canWrite()) {
				String msg = String.format(
						"Could not find and/or write the Nuxeo databases initialization script file '%s'",
						nuxeoDatabasesInitScriptFile.getCanonicalPath());
				logger.warn(msg);
			} else {
				// Note: Exceptions are written only to the console, not thrown, by
				// the writeFileFromLines() method in the common-api package.
				FileTools.writeFileFromLines(dbInitializationScriptFilePath, replacementLines);
			}
		} catch (Exception e) {
			logger.error("Could not update database initialization script.", e);
			throw e;
		}

	}

	public static String getJeeContainPath() throws Exception {
		String result = System.getenv(CSPACE_JEESERVER_HOME);

		if (result == null) {
			throw new Exception();
		}

		return result;
	}
}