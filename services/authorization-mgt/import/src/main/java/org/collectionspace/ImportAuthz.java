/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace;

import java.io.PrintStream;

import net.sf.ehcache.CacheException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.collectionspace.services.authorization.driver.AuthorizationSeedDriver;

/**
 * ImportAuthz imports default permissions and roles for a tenant(s)
 * @authorF
 */
public class ImportAuthz {

	final private static String OPTIONS_GENERATE_ONLY = "generate only";
    final private static String OPTIONS_USERNAME = "username";
    final private static String OPTIONS_PASSWORD = "password";
    final private static String OPTIONS_TENANT_BINDING = "tenant binding file";
    final private static String OPTIONS_IMPORT_DIR = "importdir";
    final private static String OPTIONS_EXPORT_DIR = "exportdir";
    final private static String OPTIONS_HELP = "help";

    final private static String MSG_SEPARATOR = "--";
    final private static String LOGGING_SEPARATOR_HEAD = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>";
    final private static String LOGGING_SEPARATOR_TAIL = "<<<<<<<<<<<<<<<<<<<<<<<<<<<<";
    final private static String LOGGING_INFO_PREFIX = "[INFO] ";
    final private static String LOGGING_ERROR_PREFIX = "[ERROR] ";

    final private static boolean generateOnly(String param) {
    	boolean result = false;
    	if (param != null) {
    		result = Boolean.parseBoolean(param);
    	}
    	return result;
    }
    
    //
    // Private logging methods.  We should try to get this code to use a logging utility like Log4j, Slf4j, etc.
    // I'm not sure why we are not using a logging util?  But at least we're consolidating all calls to System.out and Sytem.err.
    //
    private static void logError(String errMessage) {
    	System.out.println(LOGGING_ERROR_PREFIX + errMessage);
    }
    
    private static void logInfo(PrintStream outStream, String infoMessage) {
    	outStream.println(LOGGING_INFO_PREFIX + infoMessage);
    }
    
    private static void logInfo(String infoMessage) {
    	logInfo(System.out, infoMessage);
    }
    
    private static void logConfiguration(String user,
    		String password,
    		String tenantBinding,
    		String exportDir) {
    	logInfo(LOGGING_SEPARATOR_HEAD);
    	logInfo("Creating CollectionSpace authorization metadata using the following settings:");
    	logInfo("\tuser=" + user);
    	logInfo("\tpassword=" + password);
    	logInfo("\ttenantBinding=" + tenantBinding);
    	logInfo("\texportDir=" + exportDir);
    	logInfo(LOGGING_SEPARATOR_TAIL);
    }
    
    private static void printUsage(PrintStream outStream) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nUsage : java -cp <classpath> " + ImportAuthz.class.getName() + " <options>");
        sb.append("\nOptions :");
        sb.append("\n   -g  <" + OPTIONS_GENERATE_ONLY + "> generate only, do not seed AuthZ values in the security tables");        
        sb.append("\n   -u  <" + OPTIONS_USERNAME + "> cspace username");
        sb.append("\n   -p  <" + OPTIONS_PASSWORD + "> password");
        sb.append("\n   -b  <" + OPTIONS_TENANT_BINDING + "> tenant binding file (fully qualified path)");
        sb.append("\n   -edir  <" + OPTIONS_EXPORT_DIR + "> directory to export authz data into");
        logInfo(sb.toString());
    }
    
    private static void printUsage() {
    	printUsage(System.out);
    }
    
    private static void logInitialErrorCauseMsg(Throwable t) {
        if (t != null) {
            if (t.getCause() != null) {
                logInitialErrorCauseMsg(t.getCause());
            } else {
            	logError(t.getMessage());
            }
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption("g", true, OPTIONS_GENERATE_ONLY);
        options.addOption("u", true, OPTIONS_USERNAME);
        options.addOption("p", true, OPTIONS_PASSWORD);
        options.addOption("b", true, OPTIONS_TENANT_BINDING);
        options.addOption("edir", true, OPTIONS_EXPORT_DIR);
        options.addOption("h", true, OPTIONS_HELP);
        return options;
    }
    //
    // End of logging methods.
    //
    
    //
    // Create our AuthZ metadata
    //
    public static void main(String[] args) {

        Options options = createOptions();

        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h")) {
                printUsage();
                System.exit(1);
            }
            String generate_only = line.getOptionValue("g");
            String user = line.getOptionValue("u");
            String password = line.getOptionValue("p");
            String tenantBinding = line.getOptionValue("b");
            String exportDir = line.getOptionValue("edir");
            logConfiguration(user, password, tenantBinding, exportDir);
            //
            // Instantiate an AuthZ seed driver and ask it to generate our AuthZ metadata
            //
            AuthorizationSeedDriver driver = new AuthorizationSeedDriver(
                    user, password, tenantBinding, exportDir);
            driver.generate();
            //
            // If the "-g" option was set, then we will NOT seed the AuthZ tables.  Instead, we'll
            // just merge the prototypical tenant bindings and generate the permissions XML output
            //
            if (generateOnly(generate_only) == false) {
            	driver.seed();
            } else {
            	logError("WARNING: '-g' was set to 'true' so AuthZ tables were ***NOT*** seeded.");
            }
        } catch (ParseException exp) {
        	logError("Parsing failed.  Reason: " + exp.getMessage());
        } catch (Exception e) {
        	logError("Error : " + e.getMessage());
        	logError(MSG_SEPARATOR);
            printUsage(System.err);
            logError(MSG_SEPARATOR);
            logError("Import failed: ");
            logInitialErrorCauseMsg(e);
            System.exit(1);
        }
    }
}
