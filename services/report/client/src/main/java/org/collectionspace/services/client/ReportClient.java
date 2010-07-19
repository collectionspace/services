/**	
 * ReportClient.java
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
 * Copyright (c) 2009 {Contributing Institution}
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
import org.collectionspace.services.report.ReportsCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A ReportClient.

 * @version $Revision:$
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1684
 */
public class ReportClient extends AbstractServiceClientImpl {

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    public String getServicePathComponent() {
        return "reports";
    }
    /**
     *
     */
//    private static final ReportClient instance = new ReportClient();
    /**
     *
     */
    private ReportProxy reportProxy;

    /**
     *
     * Default constructor for ReportClient class.
     *
     */
    public ReportClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.reportProxy;
    }
    
    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            reportProxy = ProxyFactory.create(ReportProxy.class,
                    getBaseURL(), getHttpClient());
        } else {
            reportProxy = ProxyFactory.create(ReportProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
//    public static ReportClient getInstance() {
//        return instance;
//    }

    /**
     * @return
     * @see org.collectionspace.services.client.ReportProxy#getReport()
     */
    public ClientResponse<ReportsCommonList> readList() {
        return reportProxy.readList();
    }
    
    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.ReportProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid) {
        return reportProxy.getAuthorityRefs(csid);
    }


    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.ReportProxy#getReport(java.lang.String)
     */
    public ClientResponse<MultipartInput> read(String csid) {
        return reportProxy.read(csid);
    }

    /**
     * @param report
     * @return
     * @see org.collectionspace.services.client.ReportProxy#createReport(org.collectionspace.hello.Report)
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return reportProxy.create(multipart);
    }

    /**
     * @param csid
     * @param report
     * @return
     * @see org.collectionspace.services.client.ReportProxy#updateReport(java.lang.Long, org.collectionspace.hello.Report)
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return reportProxy.update(csid, multipart);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.ReportProxy#deleteReport(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return reportProxy.delete(csid);
    }
}
