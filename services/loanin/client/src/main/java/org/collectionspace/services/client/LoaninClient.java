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

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.authorityref.AuthorityRefList;
//import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.loanin.LoansinCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * LoaninClient.java
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 */
public class LoaninClient extends AbstractServiceClientImpl {

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    public String getServicePathComponent() {
        return "loansin";
    }
    /**
     *
     */
    private static final LoaninClient instance = new LoaninClient();
    /**
     *
     */
    private LoaninProxy loaninProxy;

    /**
     *
     * Default constructor for LoaninClient class.
     *
     */
    public LoaninClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }
    
    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.loaninProxy;
    }    

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            loaninProxy = ProxyFactory.create(LoaninProxy.class,
                    getBaseURL(), getHttpClient());
        } else {
            loaninProxy = ProxyFactory.create(LoaninProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static LoaninClient getInstance() {
        return instance;
    }

    /**
     * @return
     * @see org.collectionspace.services.client.LoaninProxy#getLoanin()
     */
    public ClientResponse<LoansinCommonList> readList() {
        return loaninProxy.readList();
    }
    
    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.LoaninProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid) {
        return loaninProxy.getAuthorityRefs(csid);
    }


    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.LoaninProxy#getLoanin(java.lang.String)
     */
    public ClientResponse<MultipartInput> read(String csid) {
        return loaninProxy.read(csid);
    }

    /**
     * @param loanin
     * @return
     * @see org.collectionspace.services.client.LoaninProxy#createLoanin(org.collectionspace.hello.Loanin)
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return loaninProxy.create(multipart);
    }

    /**
     * @param csid
     * @param loanin
     * @return
     * @see org.collectionspace.services.client.LoaninProxy#updateLoanin(java.lang.Long, org.collectionspace.hello.Loanin)
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return loaninProxy.update(csid, multipart);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.LoaninProxy#deleteLoanin(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return loaninProxy.delete(csid);
    }
}
