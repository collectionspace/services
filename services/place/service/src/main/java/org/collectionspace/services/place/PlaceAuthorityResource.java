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
package org.collectionspace.services.place;

import org.collectionspace.services.client.PlaceAuthorityClient;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.place.nuxeo.PlaceDocumentModelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path(PlaceAuthorityClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class PlaceAuthorityResource 
	extends AuthorityResource<PlaceauthoritiesCommon, 
								PlaceDocumentModelHandler> {

    private final static String placeAuthorityServiceName = "placeauthorities";
	private final static String PLACEAUTHORITIES_COMMON = "placeauthorities_common";
    
    private final static String placeServiceName = "places";
	private final static String PLACES_COMMON = "places_common";
    
    final Logger logger = LoggerFactory.getLogger(PlaceAuthorityResource.class);

    public PlaceAuthorityResource() {
		super(PlaceauthoritiesCommon.class, PlaceAuthorityResource.class,
				PLACEAUTHORITIES_COMMON, PLACES_COMMON);
    }

    @Override
    public String getServiceName() {
        return placeAuthorityServiceName;
    }

    public String getItemServiceName() {
        return placeServiceName;
    }

    @Override
    public Class<PlaceauthoritiesCommon> getCommonPartClass() {
    	return PlaceauthoritiesCommon.class;
    }
}
