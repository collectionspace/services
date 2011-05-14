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
package org.collectionspace.services.organization;

import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.contact.AuthorityResourceWithContacts;
import org.collectionspace.services.organization.nuxeo.OrganizationDocumentModelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/" + OrgAuthorityClient.SERVICE_PATH_COMPONENT)
@Consumes("application/xml")
@Produces("application/xml")
public class OrgAuthorityResource extends
	AuthorityResourceWithContacts<OrgauthoritiesCommon, OrgauthoritiesCommonList, OrganizationsCommon,
	OrganizationDocumentModelHandler> {

    private final static String orgAuthorityServiceName = "orgauthorities";
	private final static String ORGAUTHORITIES_COMMON = "orgauthorities_common";
    
    private final static String organizationServiceName = "organizations";
	private final static String ORGANIZATIONS_COMMON = "organizations_common";
    
    final Logger logger = LoggerFactory.getLogger(OrgAuthorityResource.class);
    
    public OrgAuthorityResource() {
		super(OrgauthoritiesCommon.class, OrgAuthorityResource.class,
				ORGAUTHORITIES_COMMON, ORGANIZATIONS_COMMON);
    }

    @Override
    public String getServiceName() {
        return orgAuthorityServiceName;
    }
    
    public String getItemServiceName() {
        return organizationServiceName;
    }

    @Override
    public Class<OrgauthoritiesCommon> getCommonPartClass() {
    	return OrgauthoritiesCommon.class;
    }    
}
