/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import org.collectionspace.services.chronology.ChronologiesCommon;
import org.collectionspace.services.chronology.ChronologyauthoritiesCommon;

/**
 * ChronologyClient.java
 */
public class ChronologyAuthorityClient
    extends AuthorityClientImpl<ChronologyauthoritiesCommon, ChronologiesCommon, ChronologyAuthorityProxy> {

    public static final String SERVICE_NAME = "chronologyauthorities";
    public static final String SERVICE_ITEM_NAME = "chronologies";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
    public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
    public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";
    public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;
    public static final String SERVICE_COMMON_PART_NAME = SERVICE_NAME + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
    public static final String SERVICE_ITEM_NAME_COMMON_PART_NAME =
        SERVICE_ITEM_NAME + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
    public static final String TERM_INFO_GROUP_XPATH_BASE = "chronologyTermGroupList";
    public static final String SERVICE_BINDING_NAME = "ChronologyAuthority";

    public ChronologyAuthorityClient() throws Exception {
        super();
    }

    public ChronologyAuthorityClient(String clientPropertiesFilename) throws Exception {
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
    public Class<ChronologyAuthorityProxy> getProxyClass() {
        return ChronologyAuthorityProxy.class;
    }

    @Override
    public String getInAuthority(ChronologiesCommon item) {
        return item.getInAuthority();
    }

    @Override
    public void setInAuthority(ChronologiesCommon item, String inAuthorityCsid) {
        item.setInAuthority(inAuthorityCsid);
    }

    @Override
    public String createAuthorityInstance(String shortIdentifier, String displayName) {
        final PoxPayloadOut poxPayloadOut = ChronologyAuthorityClientUtils.createChronologyAuthorityInstance(
            displayName, shortIdentifier, SERVICE_COMMON_PART_NAME);
        return poxPayloadOut.asXML();
    }

    @Override
    public String createAuthorityItemInstance(String shortIdentifier, String displayName) {
        return null;
    }
}
