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
 
public class NoteClient extends AbstractPoxServiceClientImpl<NotesCommonList, NoteProxy> {

    public static final String SERVICE_NAME = "notes";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
    public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;

    @Override
    public String getServiceName() {
            return SERVICE_NAME;
    }

    @Override
    public String getServicePathComponent() {
            return SERVICE_PATH_COMPONENT;
    }

	@Override
	public Class<NoteProxy> getProxyClass() {
		return NoteProxy.class;
	}

	/*
	 * Proxied service calls
	 */
	
    /**
     * @return
     * @see org.collectionspace.services.client.Note#getNote()
     */
    public ClientResponse<NotesCommonList> readList() {
        return getProxy().readList();
    }
}
