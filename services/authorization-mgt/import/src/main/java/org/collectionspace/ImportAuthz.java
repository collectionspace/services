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

    public static void main(String[] args) {

        Options options = createOptions();

        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            String user = line.getOptionValue("u");
            String password = line.getOptionValue("p");
            String tenantBinding = line.getOptionValue("b");
            String importDir = line.getOptionValue("idir");
            String exportDir = line.getOptionValue("edir");
            System.out.println("user=" + user
                    + " password=" + password
                    + " tenantBinding=" + tenantBinding
                    + " importDir=" + importDir
                    + " exportDir=" + exportDir);
            AuthorizationSeedDriver driver = new AuthorizationSeedDriver(
                    user, password, tenantBinding, importDir, exportDir);
            driver.seedData();
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }

    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption("u", true, "username");
        options.addOption("p", true, "password");
        options.addOption("b", true, "tenant binding file");
        options.addOption("idir", true, "import dir");
        options.addOption("edir", true, "export dir");
        return options;
    }
}
