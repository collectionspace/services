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

import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.loanout.LoansoutCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * LoanoutClient.java
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class LoanoutClient extends AbstractServiceClientImpl {
	public static final String SERVICE_NAME = "loansout";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";	
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;

	private LoanoutProxy loanoutProxy;

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
    
    /**
     *
     * Default constructor for LoanoutClient class.
     *
     */
    public LoanoutClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.loanoutProxy;
    }    

    /**
     * allow to reset proxy as per security needs
     */
    @Override
    public void setProxy() {
        if (useAuth()) {
            loanoutProxy = ProxyFactory.create(LoanoutProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            loanoutProxy = ProxyFactory.create(LoanoutProxy.class,
                    getBaseURL());
        }
    }

    /**
     * @return
     * @see org.collectionspace.services.client.LoanoutProxy#getLoanout()
     */
    public ClientResponse<LoansoutCommonList> readList() {
        return loanoutProxy.readList();
    }
    
    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.LoanoutProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid) {
        return loanoutProxy.getAuthorityRefs(csid);
    }


    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.LoanoutProxy#getLoanout(java.lang.String)
     */
    public ClientResponse<String> read(String csid) {
        return loanoutProxy.read(csid);
    }

    /**
     * @param multipart
     * @return
     * @see org.collectionspace.services.client.LoanoutProxy#createLoanout(org.collectionspace.hello.Loanout)
     */
    public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return loanoutProxy.create(xmlPayload.getBytes());
    }

    /**
     * @param csid
     * @param multipart
     * @return
     * @see org.collectionspace.services.client.LoanoutProxy#updateLoanout(java.lang.Long, org.collectionspace.hello.Loanout)
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return loanoutProxy.update(csid, xmlPayload.getBytes());

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.LoanoutProxy#deleteLoanout(java.lang.Long)
     */
    @Override
    public ClientResponse<Response> delete(String csid) {
        return loanoutProxy.delete(csid);
    }
}
