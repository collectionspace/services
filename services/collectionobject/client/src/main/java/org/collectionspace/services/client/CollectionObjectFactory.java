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

package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.domain.naturalhistory.CollectionobjectsNaturalhistory;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class CollectionObjectFactory {
    static private final Logger logger = LoggerFactory.getLogger(CollectionObjectFactory.class);

    /**
     * create account instance
     * @param screenName
     * @param userName
     * @param passwd
     * @param email
     * @param useScreenName
     * @param addTenant
     * @param invalidTenant
     * @param useUser
     * @param usePassword
     * @return
     */

    /**
     * Creates the collection object instance.
     *
     * @param commonPartName the common part name
     * @param collectionObject the collection object
     * @param nhPartName 
     * @param nhPartname natural history part name
     * @param conh the conh
     * @return the multipart output
     */
    public static MultipartOutput createCollectionObjectInstance(String commonPartName,
            CollectionobjectsCommon collectionObject,
            String nhPartName, CollectionobjectsNaturalhistory conh) {

        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(collectionObject,
                MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", commonPartName);

        if (conh != null) {
            OutputPart nhPart = multipart.addPart(conh, MediaType.APPLICATION_XML_TYPE);
            nhPart.getHeaders().add("label", nhPartName);
        }
        return multipart;
    }


}
