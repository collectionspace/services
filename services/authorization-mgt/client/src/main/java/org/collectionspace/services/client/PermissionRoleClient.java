/**	
 * PermissionRoleClient.java
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
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.description.ServiceDescription;

/**
 * A PermissionRoleClient.

 * @version $Revision:$
 */
public class PermissionRoleClient extends AbstractServiceClientImpl<PermissionRole, PermissionRole, PermissionRole, PermissionRoleProxy> {
	
	public PermissionRoleClient() throws Exception {
		super();
	}

	public PermissionRoleClient(String clientPropertiesFilename) throws Exception {
		super(clientPropertiesFilename);
	}

	@Override
	public String getServiceName() {
		throw new UnsupportedOperationException(); //FIXME: REM - http://issues.collectionspace.org/browse/CSPACE-3497
	}
    
    /* (non-Javadoc)
     * @see 
     */
    @Override
	public String getServicePathComponent() {
        return "authorization/permissions";
    }

	@Override
	public Class<PermissionRoleProxy> getProxyClass() {
		return PermissionRoleProxy.class;
	}

	/*
	 * CRUD+L Methods
	 */
	
    /**
     * Read.
     *
     * @param csid the csid
     * @param prcsid relationship does not have an id, junk is fine
     * @return the client response
     * @see
     */
    public Response read(String csid, String prcsid) {
        return getProxy().read(csid, prcsid);
    }

    /**
     * Read.
     *
     * @param csid the csid
     * @param prcsid relationship does not have an id, junk is fine
     * @return the client response
     * @see
     */
    @Override
    public Response read(String csid) {
        return getProxy().read(csid);
    }

    /**
     * Creates the.
     *
     * @param csid the csid
     * @param permRole the perm role
     * @return the client response
     * @see
     */
    public Response create(String csid, PermissionRole permRole) {
        return getProxy().create(csid, permRole);
    }
    
    /**
     * Delete with payload
     *
     * @param csid the csid
     * @param permRole the perm role
     * @return the client response
     */
    public Response delete(String csid, PermissionRole permRole) {
    	return getProxy().delete(csid, "delete", permRole);
    }

	@Override
	public Response create(PermissionRole payload) {
		throw new UnsupportedOperationException(); //method not supported nor needed
	}

	@Override
	public Response update(String csid,
			PermissionRole payload) {
		throw new UnsupportedOperationException(); //method not supported nor needed
	}

	@Override
	public Response readList() {
		throw new UnsupportedOperationException(); //method not supported nor needed
	}
	
	@Override
	public ServiceDescription getServiceDescription() {
		ServiceDescription result = null;
		
        Response res = getProxy().getServiceDescription();
        if (res.getStatus() == HttpStatus.SC_OK) {
        	result = (ServiceDescription) res.readEntity(ServiceDescription.class);
        }
        
        return result;
	}
}
