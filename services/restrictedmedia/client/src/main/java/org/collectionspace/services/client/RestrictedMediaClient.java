/*
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

import javax.ws.rs.core.Response;
import org.collectionspace.services.restrictedmedia.RestrictedMediaCommon;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

/**
 * RestrictedMediaClient.java
 */
public class RestrictedMediaClient
        extends AbstractCommonListPoxServiceClientImpl<RestrictedMediaProxy, RestrictedMediaCommon> {

    public static final String SERVICE_NAME = "restrictedmedia";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
    public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
    public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";
    public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;

    public RestrictedMediaClient() throws Exception {
        super();
    }

    public RestrictedMediaClient(String clientPropertiesFilename) throws Exception {
        super(clientPropertiesFilename);
    }

    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public Class<RestrictedMediaProxy> getProxyClass() {
        return RestrictedMediaProxy.class;
    }

    /**
     * Creates a new blob resource from the form data and associates it with an existing media resource
     *
     * @param csid the media resource csid
     * @param formDataOutput
     * @return
     */
    public Response createBlobFromFormData(String csid, MultipartFormDataOutput formDataOutput) {
        return getProxy().createBlobFromFormData(csid, formDataOutput);
    }

    /**
     * Creates a new blob
     *
     * @param csid
     * @return
     */
    public Response createBlobFromUri(String csid, String blobUri) {
        // send the URI as both a query param and as content
        return getProxy().createBlobFromUri(csid, blobUri, blobUri);
    }

    /*
     * Create both a new media record
     */
    public Response createMediaAndBlobWithUri(PoxPayloadOut xmlPayload, String blobUri, boolean purgeOriginal) {
        return getProxy().createMediaAndBlobWithUri(xmlPayload.getBytes(), blobUri, purgeOriginal);
    }

    /**
     * @param csid
     * @param xmlPayload
     * @param URI
     * @return
     */
    public Response update(String csid, PoxPayloadOut xmlPayload, String URI) {
        return getProxy().update(csid, xmlPayload.getBytes());
    }
}
