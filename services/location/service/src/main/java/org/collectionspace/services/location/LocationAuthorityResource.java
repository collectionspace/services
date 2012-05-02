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
package org.collectionspace.services.location;

import org.collectionspace.services.client.LocationAuthorityClient;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.location.nuxeo.LocationDocumentModelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path(LocationAuthorityClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class LocationAuthorityResource 
	extends AuthorityResource<LocationauthoritiesCommon, 
								LocationDocumentModelHandler> {

    private final static String locationAuthorityServiceName = "locationauthorities";
	private final static String LOCATIONAUTHORITIES_COMMON = "locationauthorities_common";
    
    private final static String locationServiceName = "locations";
	private final static String LOCATIONS_COMMON = "locations_common";
    
    final Logger logger = LoggerFactory.getLogger(LocationAuthorityResource.class);

    public LocationAuthorityResource() {
		super(LocationauthoritiesCommon.class, LocationAuthorityResource.class,
				LOCATIONAUTHORITIES_COMMON, LOCATIONS_COMMON);
    }

    @Override
    public String getServiceName() {
        return locationAuthorityServiceName;
    }

    public String getItemServiceName() {
        return locationServiceName;
    }

    @Override
    public Class<LocationauthoritiesCommon> getCommonPartClass() {
    	return LocationauthoritiesCommon.class;
    }

	@Override
	public String getItemTermInfoGroupXPathBase() {
        return LocationAuthorityClient.TERM_INFO_GROUP_XPATH_BASE;
	}
}
