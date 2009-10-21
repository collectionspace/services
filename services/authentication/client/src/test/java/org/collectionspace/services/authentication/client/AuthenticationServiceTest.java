/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c)) 2009 Regents of the University of California
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
 * AuthenticationServiceTest uses CollectionObject service to test
 * authentication
 * 
 * $LastChangedRevision: 434 $ $LastChangedDate: 2009-07-28 14:34:15 -0700 (Tue,
 * 28 Jul 2009) $
 */
public class AuthenticationServiceTest extends AbstractServiceTest {

	/** The known resource id. */
	private String knownResourceId = null;
	
	/** The logger. */
	final Logger logger = LoggerFactory
			.getLogger(AuthenticationServiceTest.class);

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#getServicePathComponent()
	 */
    @Override
	protected String getServicePathComponent() {
		// no need to return anything but null since no auth resources are
		// accessed
		return null;
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#create()
	 */
	@Test
	@Override
	public void create() {
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		String identifier = this.createIdentifier();
		MultipartOutput multipart = createCollectionObjectInstance(
				collectionObjectClient.getCommonPartName(), identifier);

		if (!collectionObjectClient.isServerSecure()) {
			logger.warn("set -Dcspace.server.secure=true to run security tests");
			return;
		}
		collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
				"true");
		collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
				"test");
		collectionObjectClient.setProperty(
				CollectionSpaceClient.PASSWORD_PROPERTY, "test");
		try {
			collectionObjectClient.setupHttpClient();
			collectionObjectClient.setProxy();
		} catch (Exception e) {
			logger.error("create: caught " + e.getMessage());
			return;
		}
		ClientResponse<Response> res = collectionObjectClient.create(multipart);
		if(logger.isDebugEnabled()){
            logger.debug("create: status = " + res.getStatus());
        }
		Assert.assertEquals(res.getStatus(), Response.Status.CREATED
				.getStatusCode(), "expected "
				+ Response.Status.CREATED.getStatusCode());

		// Store the ID returned from this create operation for additional tests
		// below.
		knownResourceId = extractId(res);
	}

	/**
	 * Creates the collection object instance without user.
	 */
	@Test(dependsOnMethods = { "create" })
	public void createWithoutUser() {
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		String identifier = this.createIdentifier();
		MultipartOutput multipart = createCollectionObjectInstance(
				collectionObjectClient.getCommonPartName(), identifier);
		if (!collectionObjectClient.isServerSecure()) {
			logger
					.warn("set -Dcspace.server.secure=true to run security tests");
			return;
		}
		collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
				"true");
		collectionObjectClient
				.removeProperty(CollectionSpaceClient.USER_PROPERTY);
		collectionObjectClient.setProperty(
				CollectionSpaceClient.PASSWORD_PROPERTY, "test");
		try {
			collectionObjectClient.setupHttpClient();
			collectionObjectClient.setProxy();
		} catch (Exception e) {
			logger.error("createWithoutUser: caught " + e.getMessage());
			return;
		}
		ClientResponse<Response> res = collectionObjectClient.create(multipart);
		if(logger.isDebugEnabled()){
            logger.debug("createWithoutUser: status = " + res.getStatus());
        }
		Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED
				.getStatusCode(), "expected "
				+ Response.Status.UNAUTHORIZED.getStatusCode());
	}

	/**
	 * Creates the collection object instance without password.
	 */
	@Test(dependsOnMethods = { "createWithoutUser" })
	public void createWithoutPassword() {
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		String identifier = this.createIdentifier();
		MultipartOutput multipart = createCollectionObjectInstance(
				collectionObjectClient.getCommonPartName(), identifier);
		if (!collectionObjectClient.isServerSecure()) {
			logger
					.warn("set -Dcspace.server.secure=true to run security tests");
			return;
		}
		collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
				"true");
		collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
				"test");
		collectionObjectClient
				.removeProperty(CollectionSpaceClient.PASSWORD_PROPERTY);
		try {
			collectionObjectClient.setupHttpClient();
			collectionObjectClient.setProxy();
		} catch (Exception e) {
			logger.error("createWithoutPassword: caught " + e.getMessage());
			return;
		}
		ClientResponse<Response> res = collectionObjectClient.create(multipart);
		if(logger.isDebugEnabled()){
            logger.debug("createWithoutPassword: status = " + res.getStatus());
        }
		Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED
				.getStatusCode(), "expected "
				+ Response.Status.UNAUTHORIZED.getStatusCode());
	}

	/**
	 * Creates the collection object instance with incorrect password.
	 */
	@Test(dependsOnMethods = { "createWithoutPassword" })
	public void createWithIncorrectPassword() {
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		String identifier = this.createIdentifier();
		MultipartOutput multipart = createCollectionObjectInstance(
				collectionObjectClient.getCommonPartName(), identifier);
		if (!collectionObjectClient.isServerSecure()) {
			logger
					.warn("set -Dcspace.server.secure=true to run security tests");
			return;
		}
		collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
				"true");
		collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
				"test");
		collectionObjectClient.setProperty(
				CollectionSpaceClient.PASSWORD_PROPERTY, "bar");
		try {
			collectionObjectClient.setupHttpClient();
			collectionObjectClient.setProxy();
		} catch (Exception e) {
			logger.error("createWithIncorrectPassword: caught "
					+ e.getMessage());
			return;
		}
		ClientResponse<Response> res = collectionObjectClient.create(multipart);
		if(logger.isDebugEnabled()){
            logger.debug("createWithIncorrectPassword: status = " + res.getStatus());
        }
		Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED
				.getStatusCode(), "expected "
				+ Response.Status.UNAUTHORIZED.getStatusCode());
	}

	/**
	 * Creates the collection object instance without user password.
	 */
	@Test(dependsOnMethods = { "createWithoutPassword" })
	public void createWithoutUserPassword() {
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		String identifier = this.createIdentifier();
		MultipartOutput multipart = createCollectionObjectInstance(
				collectionObjectClient.getCommonPartName(), identifier);
		if (!collectionObjectClient.isServerSecure()) {
			logger.warn("set -Dcspace.server.secure=true to run security tests");
			return;
		}
		collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
				"true");
		collectionObjectClient
				.removeProperty(CollectionSpaceClient.USER_PROPERTY);
		collectionObjectClient
				.removeProperty(CollectionSpaceClient.PASSWORD_PROPERTY);
		try {
			collectionObjectClient.setupHttpClient();
			collectionObjectClient.setProxy();
		} catch (Exception e) {
			logger.error("createWithoutUserPassword: caught " + e.getMessage());
			return;
		}
		ClientResponse<Response> res = collectionObjectClient.create(multipart);
		if(logger.isDebugEnabled()){
            logger.debug("createWithoutUserPassword: status = " + res.getStatus());
        }
		Assert.assertEquals(res.getStatus(), Response.Status.FORBIDDEN
				.getStatusCode(), "expected "
				+ Response.Status.FORBIDDEN.getStatusCode());
	}

	/**
	 * Creates the collection object instance with incorrect user password.
	 */
	@Test(dependsOnMethods = { "createWithoutPassword" })
	public void createWithIncorrectUserPassword() {
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		String identifier = this.createIdentifier();
		MultipartOutput multipart = createCollectionObjectInstance(
				collectionObjectClient.getCommonPartName(), identifier);
		if (!collectionObjectClient.isServerSecure()) {
			logger.warn("set -Dcspace.server.secure=true to run security tests");
			return;
		}
		collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
				"true");
		collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
				"foo");
		collectionObjectClient.setProperty(
				CollectionSpaceClient.PASSWORD_PROPERTY, "bar");
		try {
			collectionObjectClient.setupHttpClient();
			collectionObjectClient.setProxy();
		} catch (Exception e) {
			logger.error("createWithIncorrectUserPassword: caught "
					+ e.getMessage());
			return;
		}
		ClientResponse<Response> res = collectionObjectClient.create(multipart);
		if(logger.isDebugEnabled()){
            logger.debug("createWithIncorrectUserPassword: status = " +
                res.getStatus());
        }
		Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED
				.getStatusCode(), "expected "
				+ Response.Status.UNAUTHORIZED.getStatusCode());
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#delete()
	 */
	@Override
	@Test(dependsOnMethods = { "createWithIncorrectUserPassword" })
	public void delete() {
		CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
		collectionObjectClient = new CollectionObjectClient();
		if (!collectionObjectClient.isServerSecure()) {
			logger.warn("set -Dcspace.server.secure=true to run security tests");
			return;
		}
		collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
				"true");
		collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
				"test");
		collectionObjectClient.setProperty(
				CollectionSpaceClient.PASSWORD_PROPERTY, "test");
		try {
			collectionObjectClient.setupHttpClient();
			collectionObjectClient.setProxy();
		} catch (Exception e) {
			logger.error("deleteCollectionObject: caught " + e.getMessage());
			return;
		}
		if(logger.isDebugEnabled()){
            logger.debug("Calling deleteCollectionObject:" + knownResourceId);
        }
		ClientResponse<Response> res = collectionObjectClient
				.delete(knownResourceId);
		if(logger.isDebugEnabled()){
            logger.debug("deleteCollectionObject: status = " + res.getStatus());
        }
		Assert.assertEquals(res.getStatus(),
				Response.Status.OK.getStatusCode(), "expected "
						+ Response.Status.OK.getStatusCode());
	}

	// ---------------------------------------------------------------
	// Utility methods used by tests above
	// ---------------------------------------------------------------
	/**
	 * Creates the collection object instance.
	 * 
	 * @param commonPartName the common part name
	 * @param identifier the identifier
	 * 
	 * @return the multipart output
	 */
	private MultipartOutput createCollectionObjectInstance(
			String commonPartName, String identifier) {
		return createCollectionObjectInstance(commonPartName, "objectNumber-"
				+ identifier, "objectName-" + identifier);
	}

	/**
	 * Creates the collection object instance.
	 * 
	 * @param commonPartName the common part name
	 * @param objectNumber the object number
	 * @param objectName the object name
	 * 
	 * @return the multipart output
	 */
	private MultipartOutput createCollectionObjectInstance(
			String commonPartName, String objectNumber, String objectName) {
		CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();

		collectionObject.setObjectNumber(objectNumber);
		collectionObject.setObjectName(objectName);
		MultipartOutput multipart = new MultipartOutput();
		OutputPart commonPart = multipart.addPart(collectionObject,
				MediaType.APPLICATION_XML_TYPE);
		commonPart.getHeaders().add("label", commonPartName);

		if(logger.isDebugEnabled()){
            logger.debug("to be created, collectionobject common ",
                collectionObject, CollectionobjectsCommon.class);
        }
		return multipart;
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#createList()
	 */
	@Override
	public void createList() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#createWithEmptyEntityBody()
	 */
	@Override
	public void createWithEmptyEntityBody() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#createWithMalformedXml()
	 */
	@Override
	public void createWithMalformedXml() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#createWithWrongXmlSchema()
	 */
	@Override
	public void createWithWrongXmlSchema() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#read()
	 */
	@Override
	public void read() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#readNonExistent()
	 */
	@Override
	public void readNonExistent() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#readList()
	 */
	@Override
	public void readList() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#update()
	 */
	@Override
	public void update() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#updateWithEmptyEntityBody()
	 */
	@Override
	public void updateWithEmptyEntityBody() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#updateWithMalformedXml()
	 */
	@Override
	public void updateWithMalformedXml() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#updateWithWrongXmlSchema()
	 */
	@Override
	public void updateWithWrongXmlSchema() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#updateNonExistent()
	 */
	@Override
	public void updateNonExistent() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.test.AbstractServiceTest#deleteNonExistent()
	 */
	@Override
	public void deleteNonExistent() throws Exception {
	}
}
