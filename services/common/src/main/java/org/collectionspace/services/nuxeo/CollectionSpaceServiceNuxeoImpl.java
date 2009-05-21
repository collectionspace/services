/**
 * Copyright 2009 University of California at Berkeley
 */
package org.collectionspace.services.nuxeo;

import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.client.NuxeoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author remillet
 *
 */
public abstract class CollectionSpaceServiceNuxeoImpl {

    //replace host if not running on localhost
    //static String CS_NUXEO_HOST = "173.45.234.217";
    static String CS_NUXEO_HOST = "localhost";
    static String CS_NUXEO_URI = "http://" + CS_NUXEO_HOST + ":8080/nuxeo";
    protected Logger logger = LoggerFactory.getLogger(CollectionSpaceServiceNuxeoImpl.class);

    public NuxeoRESTClient getClient() {
        NuxeoRESTClient nxClient = new NuxeoRESTClient(CS_NUXEO_URI);

        nxClient.setAuthType(NuxeoRESTClient.AUTH_TYPE_BASIC);
        nxClient.setBasicAuthentication("Administrator", "Administrator");

        return nxClient;
    }

    protected RepositoryInstance getRepositorySession() throws Exception {
        //FIXME: is it possible to reuse repository session?
        //Authentication failures happen while trying to reuse the session
        NuxeoConnector nuxeoConnector = NuxeoConnector.getInstance();
        return nuxeoConnector.getRepositorySession();
    }

    protected void releaseRepositorySession(RepositoryInstance repoSession) {
        try{
            //release session
            NuxeoConnector nuxeoConnector = NuxeoConnector.getInstance();
            nuxeoConnector.releaseRepositorySession(repoSession);
        }catch(Exception e){
            logger.error("Could not close the repository session", e);
        //no need to throw this service specific exception
        }
    }
}
