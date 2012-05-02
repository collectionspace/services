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
package org.collectionspace.services.taxonomy;

import org.collectionspace.services.client.TaxonomyAuthorityClient;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.taxonomy.nuxeo.TaxonDocumentModelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path(TaxonomyAuthorityClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class TaxonomyAuthorityResource
        extends AuthorityResource<TaxonomyauthorityCommon, TaxonDocumentModelHandler> {

    private final static String taxonomyAuthorityServiceName = "taxonomyauthority";
    private final static String TAXONOMYAUTHORITY_COMMON = "taxonomyauthority_common";
    private final static String taxonomyItemServiceName = "taxon";
    private final static String TAXONOMYITEM_COMMON = "taxon_common";

    final Logger logger = LoggerFactory.getLogger(TaxonomyAuthorityResource.class);

    public TaxonomyAuthorityResource() {
        super(TaxonomyauthorityCommon.class, TaxonomyAuthorityResource.class,
                TAXONOMYAUTHORITY_COMMON, TAXONOMYITEM_COMMON);
    }

    @Override
    public String getServiceName() {
        return taxonomyAuthorityServiceName;
    }

    @Override
    public String getItemServiceName() {
        return taxonomyItemServiceName;
    }

    @Override
    public Class<TaxonomyauthorityCommon> getCommonPartClass() {
        return TaxonomyauthorityCommon.class;
    }

	@Override
	public String getItemTermInfoGroupXPathBase() {
        return TaxonomyAuthorityClient.TERM_INFO_GROUP_XPATH_BASE;
	}
}
