/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright 2009 University of California at Berkeley
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.collectionspace.services.citation;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.collectionspace.services.client.CitationAuthorityClient;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.citation.nuxeo.CitationDocumentModelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CitationAuthorityResource
 *
 * Handles, dispatches, and returns responses to RESTful requests related to
 * Citation authority-related resources.
 */
@Path(CitationAuthorityClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class CitationAuthorityResource extends AuthorityResource<CitationauthoritiesCommon, CitationDocumentModelHandler> {

    final Logger logger = LoggerFactory.getLogger(CitationAuthorityResource.class);

    public CitationAuthorityResource() {
        super(CitationauthoritiesCommon.class, CitationAuthorityResource.class,
                CitationAuthorityClient.SERVICE_COMMON_PART_NAME, CitationAuthorityClient.SERVICE_ITEM_COMMON_PART_NAME);
    }

    @Override
    public String getServiceName() {
        return CitationAuthorityClient.SERVICE_NAME;
    }

    @Override
    public String getItemServiceName() {
        return CitationAuthorityClient.SERVICE_ITEM_NAME;
    }

    @Override
    public String getItemTermInfoGroupXPathBase() {
        return CitationAuthorityClient.TERM_INFO_GROUP_XPATH_BASE;
    }

    @Override
    public Class<CitationauthoritiesCommon> getCommonPartClass() {
        return CitationauthoritiesCommon.class;
    }
}
