/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.chronology;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.collectionspace.services.chronology.nuxeo.ChronologyDocumentModelHandler;
import org.collectionspace.services.client.ChronologyAuthorityClient;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(ChronologyAuthorityClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class ChronologyAuthorityResource
    extends AuthorityResource<ChronologyauthoritiesCommon, ChronologyDocumentModelHandler> {
    final Logger logger = LoggerFactory.getLogger(ChronologyAuthorityResource.class);

    /**
     * Instantiates a new Authority resource.
     *
     */
    public ChronologyAuthorityResource() {
        super(ChronologyauthoritiesCommon.class,
              ChronologyAuthorityResource.class,
              ChronologyAuthorityClient.SERVICE_COMMON_PART_NAME,
              ChronologyAuthorityClient.SERVICE_ITEM_NAME_COMMON_PART_NAME);
    }

    @Override
    public String getItemServiceName() {
        return ChronologyAuthorityClient.SERVICE_ITEM_NAME;
    }

    @Override
    public String getItemTermInfoGroupXPathBase() {
        return ChronologyAuthorityClient.TERM_INFO_GROUP_XPATH_BASE;
    }

    @Override
    public String getServiceName() {
        return ChronologyAuthorityClient.SERVICE_NAME;
    }

}

