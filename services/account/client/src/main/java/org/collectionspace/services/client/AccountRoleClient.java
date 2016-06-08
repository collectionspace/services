/**	
 * AccountRoleClient.java
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
 * Copyright (C) 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.description.ServiceDescription;
import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * A AccountRoleClient.

 * @version $Revision:$
 */
public class AccountRoleClient extends AbstractServiceClientImpl<AbstractCommonList, AccountRole, AccountRole, AccountRoleProxy> {
	public static final String SERVICE_NAME = "accountroles";

	public AccountRoleClient() throws Exception {
		super();
	}
	
	public AccountRoleClient(String clientPropertiesFilename) throws Exception {
		super(clientPropertiesFilename);
	}

	@Override
	public String getServiceName() {
		return AccountRoleClient.SERVICE_NAME;
	}

    /* (non-Javadoc)
     * @see 
     */
	@Override
    public String getServicePathComponent() {
        return "accounts";
    }

	@Override
	public Class<AccountRoleProxy> getProxyClass() {
		return AccountRoleProxy.class;
	}

	/*
	 * CRUD+L Methods
	 */
	
    /**
     * @param csid
     * @param arcsid relationship does not have an id, junk is fine
     * @return
     * @see 
     */
    public Response read(String csid, String arcsid) {
        return getProxy().read(csid, arcsid);
    }

    /**
     * Read.
     *
     * @param csid the csid
     * @param arcsid the arcsid
     * @return the client response
     */
    @Override
    public Response read(String csid) {
        return getProxy().read(csid);
    }

    /**
     * @param csid
     * @param accRole relationships to create
     * @return
     * @see 
     */
    public Response create(String csid, AccountRole accRole) {
        return getProxy().create(csid, accRole);
    }
    
    /*
     * 
     * Updates on AccountRole entities is not supported.
     * 
     */
	@Override
	public Response update(String csid, AccountRole payload) {
		throw new RuntimeException("You cannot update an AccountRole object.  You must delete and recreate it instead.");
	}

    /**
     * @param csid
     * @param accRole relationship to delete
     * @return
     * @see 
     */
    public Response delete(String csid, AccountRole accRole) {
        return getProxy().delete(csid, "delete", accRole);
    }
    
    /**
     * 
     */
	@Override
	public ServiceDescription getServiceDescription() {
		ServiceDescription result = null;
		
        Response res = getProxy().getServiceDescription();
        if (res.getStatus() == HttpStatus.SC_OK) {
        	result = (ServiceDescription) res.readEntity(ServiceDescription.class);
        }
        
        return result;
	}

    /*
     * Not supported.
     */
	@Override
	public Response create(AccountRole payload) {
		// Use the create(String csid, AccountRole accRole) method instead
		throw new UnsupportedOperationException();
	}

	@Override
	public Response readList() {
		throw new UnsupportedOperationException();
	}
}
