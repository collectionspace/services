package org.collectionspace.services.common.api;

import java.io.File;

public interface JEEServerDeployment {
    final public static String CONFIG_DIR_NAME = "config";
    final public static String CSPACE_DIR_NAME = "cspace";
    final public static String SERVICES_DIR_NAME = "services";
    final public static String DATABASE_SCRIPTS_DIR_NAME = "db";
    final public static String CONFIG_DIR_PATH = CONFIG_DIR_NAME + File.separator + SERVICES_DIR_NAME;
    final public static String CSPACE_CONFIG_SERVICES_DIR_PATH = CSPACE_DIR_NAME + File.separator + CONFIG_DIR_PATH;
    final public static String CSPACE_SERVICES_DIR_PATH = CSPACE_DIR_NAME + File.separator + SERVICES_DIR_NAME;
    final public static String DATABASE_SCRIPTS_DIR_PATH = CSPACE_SERVICES_DIR_PATH + File.separator + DATABASE_SCRIPTS_DIR_NAME;

    final public static String TENANT_BINDINGS_FILENAME_PREFIX = "tenant-bindings";
    final public static String TENANT_BINDINGS_PROTOTYPE_FILENAME = TENANT_BINDINGS_FILENAME_PREFIX + "-proto.xml";
    final public static String TENANT_BINDINGS_ROOTDIRNAME = "tenants";
	
	public final static String NUXEO_CLIENT_DIR = "nuxeo-client";
	public final static String NUXEO_SERVER_DIR = "nuxeo-server";
	public final static String NUXEO_CONFIG_DIR = CONFIG_DIR_NAME;
	public final static String NUXEO_SERVER_CONFIG_DIR = NUXEO_SERVER_DIR + File.separator + NUXEO_CONFIG_DIR;
        public final static String NUXEO_REPO_CONFIG_FILENAME_SUFFIX = "-repo-config.xml";
        public final static String NUXEO_PROTOTYPE_CONFIG_FILENAME = "proto" + NUXEO_REPO_CONFIG_FILENAME_SUFFIX;
	public final static String NUXEO_PLUGINS_DIR = "plugins";
	public final static String NUXEO_SERVER_PLUGINS_DIR = NUXEO_SERVER_DIR + File.separator + NUXEO_PLUGINS_DIR;
        public final static String NUXEO_DB_INIT_SCRIPT_FILENAME = "init_nuxeo_db.sql";
}
