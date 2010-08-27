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
package org.collectionspace.services.person;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.collectionspace.services.contact.AuthorityResourceWithContacts;
import org.collectionspace.services.person.nuxeo.PersonDocumentModelHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PersonAuthorityResource.
 */
@Path("/personauthorities")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class PersonAuthorityResource extends
AuthorityResourceWithContacts<PersonauthoritiesCommon, PersonauthoritiesCommonList, PersonsCommon,
		PersonDocumentModelHandler> {

    private final static String personAuthorityServiceName = "personauthorities";
	private final static String PERSONAUTHORITIES_COMMON = "personauthorities_common";
	
    private final static String personServiceName = "persons";
	private final static String PERSONS_COMMON = "persons_common";
    
    final Logger logger = LoggerFactory.getLogger(PersonAuthorityResource.class);

    /**
     * Instantiates a new person authority resource.
     */
    public PersonAuthorityResource() {
		super(PersonauthoritiesCommon.class, PersonAuthorityResource.class,
				PERSONAUTHORITIES_COMMON, PERSONS_COMMON);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return personAuthorityServiceName;
    }

    /**
     * Gets the item service name.
     * 
     * @return the item service name
     */
    @Override
    public String getItemServiceName() {
        return personServiceName;
    }

    @Override
    public Class<PersonauthoritiesCommon> getCommonPartClass() {
    	return PersonauthoritiesCommon.class;
    }
    
}
