/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.common.security;

import java.util.HashMap;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RESTeasy interceptor for access control
 * @version $Revision: 1 $
 */
@SecurityPrecedence
@ServerInterceptor
@Provider
public class SecurityInterceptor implements PreProcessInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method)
            throws Failure, WebApplicationException {
        String httpMethod = request.getHttpMethod();
        String uriPath = request.getUri().getPath();
        if (logger.isDebugEnabled()) {
            logger.debug("received " + httpMethod + " on " + uriPath);
        }
        checkActive();
        if (uriPath.startsWith("dimensions")) {
             AuthZ authZ = AuthZ.get();
            CSpaceResource res = new URIResourceImpl(uriPath, httpMethod);
            if (!authZ.isAccessAllowed(res)) {
                logger.error("Access to " + res.getId() + " is NOT allowed to "
                        + " user=" + AuthN.get().getUserId());
                Response response = Response.status(
                        Response.Status.FORBIDDEN).entity(uriPath + " " + httpMethod).type("text/plain").build();
                throw new WebApplicationException(response);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Access to " + res.getId() + " is allowed to "
                        + " user=" + AuthN.get().getUserId());
            }
        }
        return null;
    }

    /**
     * checkActive check if account is active
     * @throws WebApplicationException
     */
    private void checkActive() throws WebApplicationException {
        String userId = AuthN.get().getUserId();

        try {
            //can't use JAXB here as this runs from the common jar which cannot
            //depend upon the account service
            String whereClause = "where userId = :userId";
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("userId", userId);

            Object account = JpaStorageUtils.getEntity(
                    "org.collectionspace.services.account.AccountsCommon", whereClause, params);
            if (account == null) {
                String msg = "User's account not found, userId=" + userId;
                Response response = Response.status(
                        Response.Status.FORBIDDEN).entity(msg).type("text/plain").build();
                throw new WebApplicationException(response);
            }
            Object status = JaxbUtils.getValue(account, "getStatus");
            if (status != null) {
                String value = (String) JaxbUtils.getValue(status, "value");
                if ("INACTIVE".equalsIgnoreCase(value)) {
                    String msg = "User's account is inactive, userId=" + userId;
                    Response response = Response.status(
                            Response.Status.FORBIDDEN).entity(msg).type("text/plain").build();
                    throw new WebApplicationException(response);
                }
            }

        } catch (Exception e) {
            String msg = "User's account is in invalid state, userId=" + userId;
            Response response = Response.status(
                    Response.Status.FORBIDDEN).entity(msg).type("text/plain").build();
            throw new WebApplicationException(response);
        }
    }
}
