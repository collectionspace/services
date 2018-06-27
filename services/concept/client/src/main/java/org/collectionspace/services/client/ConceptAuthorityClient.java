/**
 * ConceptAuthorityClient.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $ $LastChangedRevision: $ $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import org.collectionspace.services.concept.ConceptauthoritiesCommon;
import org.collectionspace.services.concept.ConceptsCommon;

/**
 * The Class ConceptAuthorityClient.
 */
public class ConceptAuthorityClient extends AuthorityClientImpl<ConceptauthoritiesCommon, ConceptsCommon, ConceptAuthorityProxy> {

    public static final String SERVICE_NAME = "conceptauthorities";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
    public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
    public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;
    public static final String TERM_INFO_GROUP_XPATH_BASE = "conceptTermGroupList";
    //
    // Subitem constants
    //
    public static final String SERVICE_ITEM_NAME = "concepts";
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
    public ConceptAuthorityClient() throws Exception {
    	super();
    }
    
    public ConceptAuthorityClient(String clientPropertiesFilename) throws Exception {
		super(clientPropertiesFilename);
	}
    
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
        return getCommonPartName(SERVICE_ITEM_NAME);
    }

    @Override
    public Class<ConceptAuthorityProxy> getProxyClass() {
        return ConceptAuthorityProxy.class;
    }

    @Override
    public String getInAuthority(ConceptsCommon item) {
        return item.getInAuthority();
    }

    @Override
    public void setInAuthority(ConceptsCommon item, String inAuthorityCsid) {
        item.setInAuthority(inAuthorityCsid);
    }

	@Override
	public String createAuthorityInstance(String shortIdentifier, String displayName) {
		PoxPayloadOut poxPayloadout = ConceptAuthorityClientUtils.createConceptAuthorityInstance(displayName, shortIdentifier, SERVICE_COMMON_PART_NAME);
		return poxPayloadout.asXML();
	}

	@Override
	public String createAuthorityItemInstance(String shortIdentifier, String displayName) {
	    PoxPayloadOut conceptItemInstance = ConceptAuthorityClientUtils.createConceptInstance(shortIdentifier, displayName, SERVICE_ITEM_COMMON_PART_NAME);
        return conceptItemInstance.asXML();
	}
}
