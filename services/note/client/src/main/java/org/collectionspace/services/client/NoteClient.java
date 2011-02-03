/**	
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.note.NotesCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * NoteClient.java
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
 
public class NoteClient extends AbstractServiceClientImpl {

    private NoteProxy noteProxy;
    public static final String SERVICE_NAME = "notes";
    public static final String SERVICE_PATH_COMPONENT = "notes";

    /**
     *
     * Default constructor for NoteClient class.
     *
     */
    public NoteClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public String getServiceName() {
            return SERVICE_NAME;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
            return SERVICE_PATH_COMPONENT;
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.noteProxy;
    }
    
    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            noteProxy = ProxyFactory.create(NoteProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            noteProxy = ProxyFactory.create(NoteProxy.class,
                    getBaseURL());
        }
    }

    /**
     * @return
     * @see org.collectionspace.services.client.Note#getNote()
     */
    public ClientResponse<NotesCommonList> readList() {
        return noteProxy.readList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.Note#getNote(java.lang.String)
     */

    public ClientResponse<String> read(String csid) {
        return noteProxy.read(csid);
    }

    /**
     * @param multipart
     * @param note
     * @return
     * @see org.collectionspace.services.client.Note#createNote(org.collectionspace.services.Note)
     */
    public ClientResponse<Response> create(PoxPayloadOut multipart) {
        String payload = multipart.toXML();
        return noteProxy.create(payload);
    }

    /**
     * @param csid
     * @param multipart
     * @return
     * @see org.collectionspace.services.client.Note#updateNote(java.lang.Long, org.collectionspace.services.Note)
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut multipart) {
        String payload = multipart.toXML();
        return noteProxy.update(csid, payload);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.Note#deleteNote(java.lang.Long)
     */
    @Override
    public ClientResponse<Response> delete(String csid) {
        return noteProxy.delete(csid);
    }
}
