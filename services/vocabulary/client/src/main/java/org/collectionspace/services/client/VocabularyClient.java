/**	
 * VocabularyClient.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import org.collectionspace.services.vocabulary.VocabularyitemsCommon;

/**
 * The Class VocabularyClient.
 */
public class VocabularyClient extends AuthorityClientImpl<VocabularyitemsCommon, VocabularyProxy> {
	public static final String SERVICE_NAME = "vocabularies";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;
	//
	// Subitem constants
	//
	public static final String SERVICE_ITEM_NAME = "vocabularyitems";
	public static final String SERVICE_ITEM_PAYLOAD_NAME = SERVICE_ITEM_NAME;
	
	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
	
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }
    
    public String getPayloadName() {
    	return SERVICE_PAYLOAD_NAME;
    }

	@Override
	public Class<VocabularyProxy> getProxyClass() {
		return VocabularyProxy.class;
	}    
    
    /**
     * Gets the item common part name.
     *
     * @return the item common part name
     */
	@Override
    public String getItemCommonPartName() {
        return getCommonPartName(SERVICE_ITEM_PAYLOAD_NAME);
    }
    
	@Deprecated // Use getItemCommonPartName() instead
    public String getCommonPartItemName() {
        return getCommonPartName(SERVICE_ITEM_PAYLOAD_NAME);
    }

	@Override
    public void setInAuthority(VocabularyitemsCommon item, String inAuthorityCsid) {
		item.setInAuthority(inAuthorityCsid);
	}
	
	@Override
	public String getInAuthority(VocabularyitemsCommon item) {
		return item.getInAuthority();
	}
}
