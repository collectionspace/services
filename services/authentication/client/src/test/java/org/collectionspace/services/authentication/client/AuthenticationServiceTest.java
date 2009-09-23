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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.test.AbstractServiceTest;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthenticationServiceTest uses CollectionObject service to test authentication
 * 
 * $LastChangedRevision: 434 $
 * $LastChangedDate: 2009-07-28 14:34:15 -0700 (Tue, 28 Jul 2009) $
 */
public class AuthenticationServiceTest extends AbstractServiceTest {

    final String SERVICE_PATH_COMPONENT = "collectionobjects";
    private String knownResourceId = null;
    final Logger logger = LoggerFactory.getLogger(AuthenticationServiceTest.class);

    @Override
    public String getServicePathComponent() {
        // @TODO Determine if it is possible to obtain this
        // value programmatically.
        //
        // We set this in an annotation in the CollectionObjectProxy
        // interface, for instance.  We also set service-specific
        // constants in each service module, which might also
        // return this value.
        return SERVICE_PATH_COMPONENT;
    }

    @Test
    @Override
    public void create() {
        String identifier = this.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        if(!collectionObjectClient.isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY, "test");
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY, "test");
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("create: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        verbose("create: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode(),
                "expected " + Response.Status.CREATED.getStatusCode());

        // Store the ID returned from this create operation for additional tests below.
        knownResourceId = extractId(res);
    }

    @Test(dependsOnMethods = {"create"})
    public void createWithoutUser() {
        String identifier = this.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        if(!collectionObjectClient.isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.removeProperty(CollectionSpaceClient.USER_PROPERTY);
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY, "test");
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("createWithoutUser: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        verbose("createWithoutUser: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test(dependsOnMethods = {"createWithoutUser"})
    public void createWithoutPassword() {
        String identifier = this.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        if(!collectionObjectClient.isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY, "test");
        collectionObjectClient.removeProperty(CollectionSpaceClient.PASSWORD_PROPERTY);
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("createWithoutPassword: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        verbose("createWithoutPassword: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test(dependsOnMethods = {"createWithoutPassword"})
    public void createWithIncorrectPassword() {
        String identifier = this.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        if(!collectionObjectClient.isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY, "test");
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY, "bar");
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("createWithIncorrectPassword: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        verbose("createWithIncorrectPassword: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test(dependsOnMethods = {"createWithoutPassword"})
    public void createWithoutUserPassword() {
        String identifier = this.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        if(!collectionObjectClient.isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.removeProperty(CollectionSpaceClient.USER_PROPERTY);
        collectionObjectClient.removeProperty(CollectionSpaceClient.PASSWORD_PROPERTY);
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("createWithoutUserPassword: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        verbose("createWithoutUserPassword: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.FORBIDDEN.getStatusCode(),
                "expected " + Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test(dependsOnMethods = {"createWithoutPassword"})
    public void createWithIncorrectUserPassword() {
        String identifier = this.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(identifier);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        if(!collectionObjectClient.isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY, "foo");
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY, "bar");
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("createWithIncorrectUserPassword: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        verbose("createWithIncorrectUserPassword: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(),
                "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Override
    @Test(dependsOnMethods = {"createWithIncorrectUserPassword"})
    public void delete() {
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient = new CollectionObjectClient();
        if(!collectionObjectClient.isServerSecure()){
            logger.warn("set -Dcspace.server.secure=true to run security tests");
            return;
        }
        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY, "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY, "test");
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY, "test");
        try{
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        }catch(Exception e){
            logger.error("deleteCollectionObject: caught " + e.getMessage());
            return;
        }
        verbose("Calling deleteCollectionObject:" + knownResourceId);
        ClientResponse<Response> res = collectionObjectClient.delete(knownResourceId);
        verbose("deleteCollectionObject: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode(),
                "expected " + Response.Status.OK.getStatusCode());
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    private MultipartOutput createCollectionObjectInstance(String identifier) {
        return createCollectionObjectInstance("objectNumber-" + identifier,
                "objectName-" + identifier);
    }

    private MultipartOutput createCollectionObjectInstance(String objectNumber, String objectName) {
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();

        collectionObject.setObjectNumber(objectNumber);
        collectionObject.setObjectName(objectName);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(collectionObject, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", getCommonPartName());

        verbose("to be created, collectionobject common ", collectionObject, CollectionobjectsCommon.class);
        return multipart;
    }

    @Override
    public void createList() {
    }

    @Override
    public void createWithEmptyEntityBody() {
    }

    @Override
    public void createWithMalformedXml() {
    }

    @Override
    public void createWithWrongXmlSchema() {
    }

    @Override
    public void read() {
    }

    @Override
    public void readNonExistent() {
    }

    @Override
    public void readList() {
    }

    @Override
    public void update() {
    }

    @Override
    public void updateWithEmptyEntityBody() {
    }

    @Override
    public void updateWithMalformedXml() {
    }

    @Override
    public void updateWithWrongXmlSchema() {
    }

    @Override
    public void updateNonExistent() {
    }

    @Override
    public void deleteNonExistent() {
    }
}
