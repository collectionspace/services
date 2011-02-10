/**	
 * IntakeClient.java
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

//import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.authorityref.AuthorityRefList;
//import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.intake.IntakesCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A IntakeClient.

 * @version $Revision:$
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1684
 */
public class IntakeClient extends AbstractServiceClientImpl {
	public static final String SERVICE_NAME = "intakes";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";	
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
	
	@Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

	/**
     *
     */
    private IntakeProxy intakeProxy;

    /**
     *
     * Default constructor for IntakeClient class.
     *
     */
    public IntakeClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.intakeProxy;
    }
    
    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            intakeProxy = ProxyFactory.create(IntakeProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            intakeProxy = ProxyFactory.create(IntakeProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
//    public static IntakeClient getInstance() {
//        return instance;
//    }

    /**
     * @return
     * @see org.collectionspace.services.client.IntakeProxy#getIntake()
     */
    public ClientResponse<IntakesCommonList> readList() {
        return intakeProxy.readList();
    }
    
    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.IntakeProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid) {
        return intakeProxy.getAuthorityRefs(csid);
    }


    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.IntakeProxy#getIntake(java.lang.String)
     */
    public ClientResponse<String> read(String csid) {
        return intakeProxy.read(csid);
    }

    /**
     * @param intake
     * @return
     * @see org.collectionspace.services.client.IntakeProxy#createIntake(org.collectionspace.hello.Intake)
     */
    public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return intakeProxy.create(xmlPayload.getBytes());
    }

    /**
     * @param csid
     * @param intake
     * @return
     * @see org.collectionspace.services.client.IntakeProxy#updateIntake(java.lang.Long, org.collectionspace.hello.Intake)
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return intakeProxy.update(csid, xmlPayload.getBytes());

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.IntakeProxy#deleteIntake(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return intakeProxy.delete(csid);
    }
}
