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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.config.service.InitHandler.Params.Field;
import org.collectionspace.services.config.service.InitHandler.Params.Property;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunSqlScripts extends InitHandler implements IInitHandler {

    private final Logger logger = LoggerFactory.getLogger(RunSqlScripts.class);
    private final static String SQL_SCRIPT_NAME_PROPERTY = "sqlScriptName";
    private final static String DATABASE_RESOURCE_DIRECTORY_NAME = "db";
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    private final static String CANNOT_PERFORM_TASKS_MESSAGE =
            "Will not be able to perform tasks within the RunSqlScript init handler.";

    /**
     * See the class javadoc for this class: it shows the syntax supported in
     * the configuration params.
     */
    @Override
    public void onRepositoryInitialized(String dataSourceName,
            String repositoryName,
            ServiceBindingType sbt,
            List<Field> fields,
            List<Property> properties) throws Exception {

        if (properties == null || properties.isEmpty()) {
            logger.warn("No properties were provided to the RunSqlScript init handler.");
            logger.warn(CANNOT_PERFORM_TASKS_MESSAGE);
            return;
        }
        List<String> scriptNames = getSqlScriptNames(properties);
        if (scriptNames == null || scriptNames.isEmpty()) {
            logger.warn("Could not obtain the name of any SQL script to run.");
            logger.warn(CANNOT_PERFORM_TASKS_MESSAGE);
            return;
        }
        for (String scriptName : scriptNames) {
            String scriptPath = getSqlScriptPath(dataSourceName, repositoryName, scriptName);
            if (Tools.isBlank(scriptPath)) {
                logger.warn("Could not get path to SQL script.");
                logger.warn(CANNOT_PERFORM_TASKS_MESSAGE);
                continue;
            }
            String scriptContents = getSqlScriptContents(scriptPath);
            if (Tools.isBlank(scriptContents)) {
                logger.warn("Could not get contents of SQL script.");
                logger.warn(CANNOT_PERFORM_TASKS_MESSAGE);
                continue;
            }
            runScript(dataSourceName, repositoryName, scriptContents, scriptPath);
        }
    }

    private List<String> getSqlScriptNames(List<Property> properties) {
        String scriptName = "";
        List<String> scriptNames = new ArrayList<String>();
        for (Property property : properties) {
            if (property.getKey().equals(SQL_SCRIPT_NAME_PROPERTY)) {
                scriptName = property.getValue();
                if (Tools.notBlank(scriptName)) {
                    scriptNames.add(scriptName);
                }
            }
        }
        return scriptNames;
    }

    private String getSqlScriptPath(String dataSourceName, String repositoryName, String scriptName) throws Exception {
        String scriptPath =
                DATABASE_RESOURCE_DIRECTORY_NAME
                + "/"
                + JDBCTools.getDatabaseProductType(dataSourceName, repositoryName)
                + "/"
                + scriptName;
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
        ClassLoader classloader = getClass().getClassLoader();
        if (classloader == null) {
            return str;
        }
        if (logger.isTraceEnabled()) {
            URL resourceurl = classloader.getResource(resourcePath);
            logger.trace("URL=" + resourceurl.toString());
        }
        InputStream instream = classloader.getResourceAsStream(resourcePath);
        if (instream == null) {
            logger.warn("Could not read resource from path " + resourcePath);
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
        StringBuilder sb = new StringBuilder("");
        if (instream == null) {
            logger.warn("Input stream is null.");
            return sb.toString();
        }
        BufferedReader bufreader = new BufferedReader(new InputStreamReader(instream));
        String line = "";
        while ((line = bufreader.readLine()) != null) {
            sb.append(line);
            sb.append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private void runScript(String dataSourceName, String repositoryName, String scriptContents, String scriptPath) {
        int rows = 0;
        try {
            rows = JDBCTools.executeUpdate(dataSourceName, repositoryName, scriptContents);
        } catch (Throwable e) {
            logger.warn("Running SQL script " + scriptPath + " resulted in error: ", e.getMessage());
            rows = -1;
        }
        // FIXME: Verify which row values represent failure; should there always
        // be one and only one row returned in a successful response from executeUpdate?
        if (rows < 0) {
            logger.warn("Running SQL script " + scriptPath + " failed to return expected results.");
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Successfully ran SQL script: " + scriptPath);
            }
        }
    }
}
