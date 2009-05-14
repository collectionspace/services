/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.nuxeo;

import java.io.File;
import java.util.Collection;
import org.nuxeo.ecm.core.client.NuxeoApp;
import org.nuxeo.ecm.core.client.NuxeoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NuxeoConnector is a facade to Nuxeo remoting client
 * @author 
 */
public class NuxeoConnector {
    //FIXME: get port and host from configuration
    public static int NUXEO_PORT = 62474;
    public static String NUXEO_HOST = "localhost";
    private Logger logger = LoggerFactory.getLogger(NuxeoConnector.class);
    private static final NuxeoConnector self = new NuxeoConnector();
    private NuxeoApp app;
    private NuxeoClient client;
    volatile boolean initialized = false; //use volatile for lazy initialization in singleton

    private NuxeoConnector() {
    }

    public final static NuxeoConnector getInstance() {
        return self;
    }

    public void initialize() throws Exception {

        try{
            if(initialized == false){
                setProperties();
                app = new NuxeoApp();
                app.start();
                if(logger.isDebugEnabled()) {
                    logger.debug("initialize() NuxeoApp started");
                }
                loadBundles();
                client = NuxeoClient.getInstance();
                initialized = true;
            }
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                e.printStackTrace();
            }
        }
    }

    private void loadBundles() throws Exception {
        String bundles = "nuxeo-client/lib/nuxeo-runtime-*:nuxeo-client/lib/nuxeo-*";
        Collection<File> files = null;
        if(bundles != null){
            files = NuxeoApp.getBundleFiles(new File("."), bundles, ":");
        }
        if(logger.isDebugEnabled()){
            logger.debug("loadBundles(): deploying bundles: " + files);
        }
        if(files != null){
            app.deployBundles(files);
        }
    }

    private void setProperties() {
        System.setProperty("org.nuxeo.runtime.server.enabled", Boolean.FALSE.toString());
        System.setProperty("org.nuxeo.runtime.server.port", "" + NUXEO_PORT);
        System.setProperty("org.nuxeo.runtime.server.host", "127.0.0.1");
        //System.setProperty("org.nuxeo.runtime.1.3.3.streaming.port", "3233");
        System.setProperty("org.nuxeo.runtime.streaming.serverLocator", "socket://127.0.0.1:3233");
        System.setProperty("org.nuxeo.runtime.streaming.isServer", Boolean.FALSE.toString());
        System.setProperty("org.nuxeo.client.remote", Boolean.TRUE.toString());
    }

    public NuxeoClient getClient() throws Exception {
        if(initialized == true){
//            if(client.isConnected()){
//                return client;
//            }else{
            //authentication failure error comes when reusing the client
            //fore connect for now
                client.forceConnect(NUXEO_HOST, NUXEO_PORT);
                if(logger.isDebugEnabled()){
                    logger.debug("getClient(): connection successful port=" + NUXEO_PORT);
                }
                return client;
//            }
        }
        String msg = "NuxeoConnector is not initialized!";
        logger.error(msg);
        throw new IllegalStateException(msg);
    }
}
