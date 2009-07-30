/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.authentication.client;

import java.util.ArrayList;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.collectionspace.services.collectionobject.CollectionObject;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthenticationServiceTest uses CollectionObject service to test authentication
 * 
 * $LastChangedRevision: 434 $
 * $LastChangedDate: 2009-07-28 14:34:15 -0700 (Tue, 28 Jul 2009) $
 */
public class AuthenticationServiceTest {

    private String knownCollectionObjectId = null;
    final Logger logger = LoggerFactory.getLogger(AuthenticationServiceTest.class);

    @Test
    public void auth_createCollectionObject() {
        if(!isServerSecure()){
            return;
        }
        String identifier = this.createIdentifier();
        CollectionObject collectionObject = createCollectionObject(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY, "test");
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY, "test");
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("auth_createCollectionObject: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.createCollectionObject(collectionObject);
        verbose("auth_createCollectionObject: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode(),
                "expected " + Response.Status.CREATED.getStatusCode());

        // Store the ID returned from this create operation for additional tests below.
        knownCollectionObjectId = extractId(res);
    }

    @Test(dependsOnMethods = {"auth_createCollectionObject"})
    public void auth_createCollectionObjectWithoutUser() {
        if(!isServerSecure()){
            return;
        }
        String identifier = this.createIdentifier();
        CollectionObject collectionObject = createCollectionObject(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.removeProperty(CollectionSpaceClient.USER_PROPERTY);
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY, "test");
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("auth_createCollectionObjectWithoutUser: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.createCollectionObject(collectionObject);
        verbose("auth_createCollectionObjectWithoutUser: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test(dependsOnMethods = {"auth_createCollectionObjectWithoutUser"})
    public void auth_createCollectionObjectWithoutPassword() {
        if(!isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        String identifier = this.createIdentifier();
        CollectionObject collectionObject = createCollectionObject(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY, "test");
        collectionObjectClient.removeProperty(CollectionSpaceClient.PASSWORD_PROPERTY);
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("auth_createCollectionObjectWithoutPassword: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.createCollectionObject(collectionObject);
        verbose("auth_createCollectionObjectWithoutPassword: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test(dependsOnMethods = {"auth_createCollectionObjectWithoutPassword"})
    public void auth_createCollectionObjectWithIncorrectPassword() {
        if(!isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        String identifier = this.createIdentifier();
        CollectionObject collectionObject = createCollectionObject(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY, "test");
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY, "bar");
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("auth_createCollectionObjectWithIncorrectPassword: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.createCollectionObject(collectionObject);
        verbose("auth_createCollectionObjectWithIncorrectPassword: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test(dependsOnMethods = {"auth_createCollectionObjectWithoutPassword"})
    public void auth_createCollectionObjectWithoutUserPassword() {
        if(!isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        String identifier = this.createIdentifier();
        CollectionObject collectionObject = createCollectionObject(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.removeProperty(CollectionSpaceClient.USER_PROPERTY);
        collectionObjectClient.removeProperty(CollectionSpaceClient.PASSWORD_PROPERTY);
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("auth_createCollectionObjectWithoutUserPassword: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.createCollectionObject(collectionObject);
        verbose("auth_createCollectionObjectWithoutUserPassword: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.FORBIDDEN.getStatusCode(),
                "expected " + Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test(dependsOnMethods = {"auth_createCollectionObjectWithoutPassword"})
    public void auth_createCollectionObjectWithIncorrectUserPassword() {
        if(!isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        String identifier = this.createIdentifier();
        CollectionObject collectionObject = createCollectionObject(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY, "foo");
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY, "bar");
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("auth_createCollectionObjectWithIncorrectUserPassword: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.createCollectionObject(collectionObject);
        verbose("auth_createCollectionObjectWithIncorrectUserPassword: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }


    @Test(dependsOnMethods = {"auth_createCollectionObjectWithIncorrectUserPassword"})
    public void auth_deleteCollectionObject() {
        if(!isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY, "test");
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY, "test");
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("auth_deleteCollectionObject: caught " + e.getMessage());
            return;
        }
        verbose("Calling deleteCollectionObject:" + knownCollectionObjectId);
        ClientResponse<Response> res = collectionObjectClient.deleteCollectionObject(knownCollectionObjectId);
        verbose("auth_deleteCollectionObject: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode(),
                "expected " + Response.Status.OK.getStatusCode());
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    private CollectionObject createCollectionObject(String identifier) {
        CollectionObject collectionObject = createCollectionObject("objectNumber-" + identifier,
                "objectName-" + identifier);

        return collectionObject;
    }

    private CollectionObject createCollectionObject(String objectNumber, String objectName) {
        CollectionObject collectionObject = new CollectionObject();

        collectionObject.setObjectNumber(objectNumber);
        collectionObject.setObjectName(objectName);

        return collectionObject;
    }

    private String extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        verbose("extractId:uri=" + uri);
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        verbose("id=" + id);
        return id;
    }

    private void verbose(String msg) {
        if(logger.isInfoEnabled()){
            logger.debug(msg);
        }
    }

    private void verbose(String msg, Object o, Class clazz) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private String createIdentifier() {
        long identifier = System.currentTimeMillis();
        return Long.toString(identifier);
    }

    private boolean isServerSecure() {
        return Boolean.getBoolean("cspace.server.secure");
    }
}
