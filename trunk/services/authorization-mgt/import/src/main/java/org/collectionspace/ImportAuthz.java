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

    final private static String OPTIONS_USERNAME = "username";
    final private static String OPTIONS_PASSWORD = "password";
    final private static String OPTIONS_TENANT_BINDING = "tenant binding file";
    final private static String OPTIONS_IMPORT_DIR = "importdir";
    final private static String OPTIONS_EXPORT_DIR = "exportdir";
    final private static String OPTIONS_HELP = "help";

    final private static String MSG_SEPARATOR = "--";

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
            String user = line.getOptionValue("u");
            String password = line.getOptionValue("p");
            String tenantBinding = line.getOptionValue("b");
            String exportDir = line.getOptionValue("edir");
            System.out.println("user=" + user
                    + " password=" + password
                    + " tenantBinding=" + tenantBinding
                    + " exportDir=" + exportDir);
            AuthorizationSeedDriver driver = new AuthorizationSeedDriver(
                    user, password, tenantBinding, exportDir);
            driver.generate();
            driver.seed();
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            System.out.println(MSG_SEPARATOR);
            printUsage();
            System.out.println(MSG_SEPARATOR);
            System.out.println("Import failed: ");
            printInitialErrorCauseMsg(e);
            System.exit(1);
        }

    }

    private static void printInitialErrorCauseMsg(Throwable t) {
        if (t != null) {
            if (t.getCause() != null) {
                printInitialErrorCauseMsg(t.getCause());
            } else {
               System.out.println(t.getMessage());
            }
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption("u", true, OPTIONS_USERNAME);
        options.addOption("p", true, OPTIONS_PASSWORD);
        options.addOption("b", true, OPTIONS_TENANT_BINDING);
        options.addOption("edir", true, OPTIONS_EXPORT_DIR);
        options.addOption("h", true, OPTIONS_HELP);
        return options;
    }

    private static void printUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nUsage : java -cp <classpath> " + ImportAuthz.class.getName() + " <options>");
        sb.append("\nOptions :");
        sb.append("\n   -u  <" + OPTIONS_USERNAME + "> cspace username");
        sb.append("\n   -p  <" + OPTIONS_PASSWORD + "> password");
        sb.append("\n   -b  <" + OPTIONS_TENANT_BINDING + "> tenant binding file (fully qualified path)");
        sb.append("\n   -edir  <" + OPTIONS_EXPORT_DIR + "> directory to export authz data into");
        System.out.println(sb.toString());
    }
}
