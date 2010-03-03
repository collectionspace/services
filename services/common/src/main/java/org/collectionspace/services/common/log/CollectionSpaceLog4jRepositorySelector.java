/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *//**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

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
package org.collectionspace.services.common.log;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RepositorySelector;
import org.apache.log4j.spi.RootLogger;

/**
 * CollectionSpaceLog4jRepositorySelector is a CollectionSpace
 * specific log4j repository selector. See Ceki's solution
 * for more details
 * Courtsey Ceki Gulcu http://articles.qos.ch/sc.html
 */
/** JNDI based Repository selector */
public class CollectionSpaceLog4jRepositorySelector implements RepositorySelector {

    // key: name of logging context,
    // value: Hierarchy instance
    private Hashtable ht;
    private Hierarchy defaultHierarchy;

    public CollectionSpaceLog4jRepositorySelector() {
        ht = new Hashtable();
        defaultHierarchy = new Hierarchy(new RootLogger(Level.DEBUG));
    }

    // the returned value is guaranteed to be non-null
    public LoggerRepository getLoggerRepository() {
        String loggingContextName = null;

        try {
            Context ctx = new InitialContext();
            loggingContextName = (String) ctx.lookup("java:comp/env/cspace-logging-context");
        } catch (NamingException ne) {
            // we can't log here
        }

        if (loggingContextName == null) {
            return defaultHierarchy;
        } else {
            Hierarchy h = (Hierarchy) ht.get(loggingContextName);
            if (h == null) {
                h = new Hierarchy(new RootLogger(Level.DEBUG));
                ht.put(loggingContextName, h);
            }
            return h;
        }
    }

    /**
     * The Container should remove the entry when the web-application
     * is removed or restarted.
     * */
    public void remove(ClassLoader cl) {
        ht.remove(cl);
    }
}
