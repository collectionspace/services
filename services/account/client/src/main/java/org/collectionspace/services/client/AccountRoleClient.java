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

//import org.collectionspace.services.authorization.AccountRolesList;
import org.collectionspace.services.authorization.AccountRole;
import org.jboss.resteasy.client.ClientResponse;

/**
 * A AccountRoleClient.

 * @version $Revision:$
 */
public class AccountRoleClient extends AbstractServiceClientImpl<AccountRoleProxy> {
	public static final String SERVICE_NAME = "accountroles";

	@Override
	public String getServiceName() {
		return AccountRoleClient.SERVICE_NAME;
	}

    /* (non-Javadoc)
     * @see 
     */
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
    public ClientResponse<AccountRole> read(String csid, String arcsid) {
        return getProxy().read(csid, arcsid);
    }

    /**
     * Read.
     *
     * @param csid the csid
     * @param arcsid the arcsid
     * @return the client response
     */
    public ClientResponse<AccountRole> read(String csid) {
        return getProxy().read(csid);
    }

    /**
     * @param csid
     * @param accRole relationships to create
     * @return
     * @see 
     */
    public ClientResponse<Response> create(String csid, AccountRole accRole) {
        return getProxy().create(csid, accRole);
    }


    /**
     * @param csid
     * @param accRole relationship to delete
     * @return
     * @see 
     */
    public ClientResponse<Response> delete(String csid, AccountRole accRole) {
        return getProxy().delete(csid, "delete", accRole);
    }
}
