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
package org.collectionspace.services.relation.nuxeo;

import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.CollectionSpaceHandlerFactory;

/**
 * CollectionObjectHandlerFactory creates handlers for collectionobject based
 * on type of Nuxeo client used
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RelationHandlerFactory implements CollectionSpaceHandlerFactory {

    private static final RelationHandlerFactory self = new RelationHandlerFactory();

    private RelationHandlerFactory() {
    }

    public static RelationHandlerFactory getInstance() {
        return self;
    }

    public DocumentHandler getHandler(String clientType) {
        if(ClientType.JAVA.toString().equals(clientType)){
            return new RelationDocumentModelHandler();
//        } else if(ClientType.REST.toString().equals(clientType)) {
//            return new RelationRepresenationHandler();
        }
        throw new IllegalArgumentException("Not supported client=" + clientType);
    }
}
