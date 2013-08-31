package org.collectionspace.services.common.api;

import java.io.File;

public interface JEEServerDeployment {
    final public static String CSPACE_DIR_NAME = "cspace";
    final public static String CONFIG_DIR_PATH = "config" + File.separator + "services";

    final public static String TENANT_BINDINGS_FILENAME_PREFIX = "tenant-bindings";
    final public static String TENANT_BINDINGS_PROTOTYPE_FILENAME = TENANT_BINDINGS_FILENAME_PREFIX + "-proto.xml";
    final public static String TENANT_BINDINGS_ROOTDIRNAME = "tenants";
	
	public final static String NUXEO_CLIENT_DIR = "nuxeo-client";
	public final static String NUXEO_SERVER_DIR = "nuxeo-server";
	public final static String NUXEO_PLUGINS_DIR = "plugins";
	public final static String NUXEO_SERVER_PLUGINS_DIR = NUXEO_SERVER_DIR + File.separator + NUXEO_PLUGINS_DIR;
}
