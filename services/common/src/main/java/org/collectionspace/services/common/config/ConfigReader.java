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
 */

package org.collectionspace.services.common.config;

import java.io.File;

/**
 * ConfigReader is an interface for a configuration reader
 */
public interface ConfigReader<T> {

    final public static String CSPACE_DIR_NAME = "cspace";
    final public static String CONFIG_DIR_NAME = "config" + File.separator + "services";

    /**
     * getFileName - get configuration file name
     * @return
     */
    public String getFileName();

    /**
     * read parse and read the configruation file.
     * @throws Exception
     */
    public void read() throws Exception;

    /**
     * getConfig get configuration binding
     * @return
     */
    public T getConfiguration();
}
