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
package org.collectionspace.services.concept;

import org.collectionspace.services.client.ConceptAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.concept.nuxeo.ConceptDocumentModelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path(ConceptAuthorityClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class ConceptAuthorityResource 
	extends AuthorityResource<ConceptauthoritiesCommon, 
								ConceptDocumentModelHandler> {

    private final static String conceptAuthorityServiceName = "conceptauthorities";
	private final static String CONCEPTAUTHORITIES_COMMON = "conceptauthorities_common";
    
    private final static String conceptServiceName = "concepts";
	private final static String CONCEPTS_COMMON = "concepts_common";
    
    final Logger logger = LoggerFactory.getLogger(ConceptAuthorityResource.class);

    public ConceptAuthorityResource() {
		super(ConceptauthoritiesCommon.class, ConceptAuthorityResource.class,
				CONCEPTAUTHORITIES_COMMON, CONCEPTS_COMMON);
    }

    @Override
    public String getServiceName() {
        return conceptAuthorityServiceName;
    }

    public String getItemServiceName() {
        return conceptServiceName;
    }

    @Override
    public Class<ConceptauthoritiesCommon> getCommonPartClass() {
    	return ConceptauthoritiesCommon.class;
    }

	@Override
	public String getItemTermInfoGroupXPathBase() {
        return ConceptAuthorityClient.TERM_INFO_GROUP_XPATH_BASE;
	}
}
