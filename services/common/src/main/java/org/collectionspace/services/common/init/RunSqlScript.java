/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright 2009 University of California at Berkeley
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.init;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import javax.sql.DataSource;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.config.service.InitHandler.Params.Field;
import org.collectionspace.services.config.service.InitHandler.Params.Property;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunSqlScript extends InitHandler implements IInitHandler {

    private final Logger logger = LoggerFactory.getLogger(RunSqlScript.class);
    private final static String SQL_SCRIPT_NAME_PROPERTY = "sqlScriptName";
    private final String DATABASE_RESOURCE_DIRECTORY_NAME = "db";
    private final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * See the class javadoc for this class: it shows the syntax supported in
     * the configuration params.
     */
    @Override
    public void onRepositoryInitialized(DataSource dataSource,
            ServiceBindingType sbt,
            List<Field> fields,
            List<Property> properties) throws Exception {

        if (properties == null || properties.isEmpty()) {
            logger.debug("No properties were provided to this init handler.");
            return;
        }
        String scriptResourceName = getSqlScriptName(properties);
        logger.debug("SQL script resource name=" + scriptResourceName);
        String scriptPath = getSqlScriptPath(scriptResourceName);
        if (Tools.isBlank(scriptPath)) {
            logger.warn("Could not get path to SQL script.");
            return;
        }
        String scriptContents = getSqlScriptContents(scriptPath);
        if (Tools.isBlank(scriptContents)) {
            logger.warn("Could not get contents of SQL script.");
            return;
        }
        runScript(dataSource, scriptContents);
    }

    private String getSqlScriptName(List<Property> properties) {
        String scriptResourceName = "";
        for (Property property : properties) {
            if (property.getKey().equals(SQL_SCRIPT_NAME_PROPERTY)) {
                scriptResourceName = property.getValue();
                if (Tools.notBlank(scriptResourceName)) {
                    break;
                }
            }
        }
        return scriptResourceName;
    }

    private String getSqlScriptPath(String scriptResourceName) throws Exception {
        String scriptPath = "";
        String sqlResourcePath =
                DATABASE_RESOURCE_DIRECTORY_NAME
                + "/"
                + JDBCTools.getDatabaseProductType()
                + "/";
        logger.debug("SQL script resource path=" + sqlResourcePath);
        return scriptPath;
    }

    private String getSqlScriptContents(String scriptPath) throws Exception {
        return getStringFromResource(scriptPath);
    }

    /**
     * Returns a string representation of a resource available to the current
     * class.
     *
     * @param resourcePath a path to the resource.
     * @return a string representation of the resource. Returns null if the
     * resource cannot be read, or if it cannot be successfully represented as a
     * string.
     */
    private String getStringFromResource(String resourcePath) {
        String str = "";
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream instream = classLoader.getResourceAsStream(resourcePath);
        if (instream == null) {
            logger.warn("Could not read from resource from path " + resourcePath);
            return null;
        }
        try {
            str = stringFromInputStream(instream);
        } catch (IOException ioe) {
            logger.warn("Could not create string from stream: ", ioe);
            return null;
        }
        return str;
    }

    /**
     * Returns a string representation of the contents of an input stream.
     *
     * @param instream an input stream.
     * @return a string representation of the contents of the input stream.
     * @throws an IOException if an error occurs when reading the input stream.
     */
    private String stringFromInputStream(InputStream instream) throws IOException {
        if (instream == null) {
        }
        BufferedReader bufreader = new BufferedReader(new InputStreamReader(instream));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while (line != null) {
            sb.append(line);
            line = bufreader.readLine();
            sb.append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private void runScript(DataSource dataSource, String scriptContents) {
        int rows = 0;
        try {
            rows = JDBCTools.executeUpdate(dataSource, scriptContents);
        } catch (Throwable e) {
            logger.warn("Running SQL script resulted in error: ", e);
            rows = -1;
        }
        // FIXME: Verify which row values represent failure; should there always
        // be one and only one row returned from executeUpdate?
        if (rows < 0) {
           logger.warn("Running SQL script failed to return expected results.");
        }
    }
}
