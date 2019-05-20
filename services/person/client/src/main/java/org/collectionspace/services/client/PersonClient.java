/**
 * PersonAuthorityClient.java
 *
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright © 2009 University of California, Berkeley
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.collectionspace.services.person.PersonsCommon;

/**
 * The Class PersonAuthorityClient.
 */
public class PersonClient extends AuthorityWithContactsClientImpl<PersonauthoritiesCommon, PersonsCommon, PersonAuthorityProxy> {

    public static final String SERVICE_NAME = "personauthorities";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
    public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
    public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;
    public static final String TERM_INFO_GROUP_XPATH_BASE = "personTermGroupList";
    //
    // Subitem constants
    //
    public static final String SERVICE_ITEM_NAME = "persons";
    public static final String SERVICE_ITEM_PAYLOAD_NAME = SERVICE_ITEM_NAME;
    //
    // Payload Part/Schema part names
    //
    public static final String SERVICE_COMMON_PART_NAME = SERVICE_NAME
            + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
    public static final String SERVICE_ITEM_COMMON_PART_NAME = SERVICE_ITEM_NAME
            + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;

    //
    // Constructors
    //
    public PersonClient() throws Exception {
    	super();
    }
    
    public PersonClient(String clientPropertiesFilename) throws Exception {
		super(clientPropertiesFilename);
	}

    //
    // Overrides
    //
    
	@Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    @Override
    public String getItemCommonPartName() {
        return getCommonPartName(SERVICE_ITEM_PAYLOAD_NAME);
    }

    @Deprecated // Use getItemCommonPartName() instead
    public String getCommonPartItemName() {
        return getCommonPartName(SERVICE_ITEM_PAYLOAD_NAME);
    }

    @Override
    public Class<PersonAuthorityProxy> getProxyClass() {
        return PersonAuthorityProxy.class;
    }

    @Override
    public String getInAuthority(PersonsCommon item) {
        return item.getInAuthority();
    }

    @Override
    public void setInAuthority(PersonsCommon item, String inAuthorityCsid) {
        item.setInAuthority(inAuthorityCsid);
    }

    //
    // Should return a valid XML payload for creating an authority instance
    //
	@Override
	public String createAuthorityInstance(String shortIdentifier, String displayName) {
		PoxPayloadOut personAuthorityInstance = PersonAuthorityClientUtils.createPersonAuthorityInstance(displayName, shortIdentifier, SERVICE_COMMON_PART_NAME);
		return personAuthorityInstance.asXML();
	}

	@Override
	public String createAuthorityItemInstance(String shortIdentifier, String displayName) {
		PoxPayloadOut personAuthorityInstance = PersonAuthorityClientUtils.createPersonInstance(shortIdentifier, displayName, SERVICE_ITEM_COMMON_PART_NAME);
		return personAuthorityInstance.asXML();
	}
}
