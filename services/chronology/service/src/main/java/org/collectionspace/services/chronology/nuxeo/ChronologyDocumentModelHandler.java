/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:
 *
 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org
 *
 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.
 *
 *  You may obtain a copy of the ECL 2.0 License at
 *
 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.chronology.nuxeo;

import org.collectionspace.services.chronology.ChronologiesCommon;
import org.collectionspace.services.client.ChronologyAuthorityClient;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;

/**
 * ChronologyDocumentModelHandler
 */
public class ChronologyDocumentModelHandler extends AuthorityItemDocumentModelHandler<ChronologiesCommon> {
    public ChronologyDocumentModelHandler() {
        super(ChronologyAuthorityClient.SERVICE_COMMON_PART_NAME,
              ChronologyAuthorityClient.SERVICE_ITEM_NAME_COMMON_PART_NAME);
    }

    @Override
    public String getAuthorityServicePath() {
        return ChronologyAuthorityClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    public String getQProperty(String prop) {
        return ChronologyConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }

    @Override
    public String getParentCommonSchemaName() {
        return ChronologyAuthorityClient.SERVICE_COMMON_PART_NAME;
    }
}
