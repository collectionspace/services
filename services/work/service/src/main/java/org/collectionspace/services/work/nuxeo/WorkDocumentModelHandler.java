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
package org.collectionspace.services.work.nuxeo;

import org.collectionspace.services.WorkJAXBSchema;
import org.collectionspace.services.client.WorkAuthorityClient;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.work.WorksCommon;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * WorkDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
/**
 * @author 
 *
 */
public class WorkDocumentModelHandler
        extends AuthorityItemDocumentModelHandler<WorksCommon> {

    /**
      * Common part schema label
      */
    private static final String COMMON_PART_LABEL = "works_common";
   
    public WorkDocumentModelHandler() {
        super(COMMON_PART_LABEL);
    }

    @Override
    public String getAuthorityServicePath(){
        return WorkAuthorityClient.SERVICE_PATH_COMPONENT;    //  CSPACE-3932
    }
    
   /**
     * Produces a default displayName from one or more supplied fields.
     * @see WorkAuthorityClientUtils.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param Name
     * @return the default display name
     * @throws Exception
     */
    private static String prepareDefaultDisplayName(String name) throws Exception {
        StringBuilder newStr = new StringBuilder();
        newStr.append(name);
        return newStr.toString();
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return WorkConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}