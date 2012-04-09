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
package org.collectionspace.services.authorization;

import java.util.StringTokenizer;
import org.collectionspace.services.authorization.perms.ActionType;

/**
 * A security resource that represents URI and method invoked on it
 * @author 
 */
public class URIResourceImpl extends CSpaceResourceImpl {

    private String uri;
    private String method;

    /**
     * constructor that is usually called from service runtime
     * uses current tenant id from the context
     * @param uri
     * @param method an http method
     */
    /*
    public URIResourceImpl(String uri, String method) {
        super(buildId(uri, getAction(method)),
                getAction(method), TYPE.URI);
        this.uri = uri;
        this.method = method;
    }
    */

    /**
     * constructor that is usually called from service runtime
     * @param tenantId id of the tenant to which this resource is associated
     * @param uri
     * @param method an http method
     */
    public URIResourceImpl(String tenantId, String uri, String method) {
        super(tenantId, buildId(uri, getAction(method)),
                getAction(method), TYPE.URI);
        this.uri = uri;
        this.method = method;
    }

    /**
     * constructor that is usually called from administrative interface
     * uses current tenant id from the context
     * @param resourceName no leading / and no trailing / needed
     * @param actionType
     */
    /*
    public URIResourceImpl(String resourceName, CSpaceAction action) {
        //FIXME more validation might be needed
        super(buildId(resourceName, action),
                action, TYPE.URI);
    }
    */

    /**
     * constructor that is usually called from administrative interface
     * @param tenantId id of the tenant to which this resource is associated
     * @param resourceName no leading / and no trailing / needed
     * @param actionType
     */
    public URIResourceImpl(String tenantId, String resourceName, CSpaceAction action) {
        super(tenantId, buildId(resourceName, action),
                action, TYPE.URI);
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    private static String buildId(String resourceName, CSpaceAction action) {
        return sanitize(resourceName) + SEPARATOR_HASH + action.toString();
    }

    private static String getParent(String uri) {
        StringTokenizer stz = new StringTokenizer(uri, "/");
        //FIXME the following ignores sub resources as well as object instances
        return stz.nextToken();
    }

    private static String sanitize(String uri) {
        uri = uri.trim();
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        if (uri.endsWith("/*")) {
            uri = uri.substring(0, uri.length() - 2);
        }
        return uri;
    }

    /*
     * Map a Permission ActionType to a CSpaceAction
     */
    //FIXME: This method is duplicated in PermissionDocumentHandler.java class.  The class loader was having
    //trouble with the ActionType class file.  Not sure why?
    public static CSpaceAction getAction(ActionType action) {
        if (ActionType.CREATE.name().equals(action.name())) {
            return CSpaceAction.CREATE;
        } else if (ActionType.READ.equals(action)) {
            return CSpaceAction.READ;
        } else if (ActionType.UPDATE.equals(action)) {
            return CSpaceAction.UPDATE;
        } else if (ActionType.DELETE.equals(action)) {
            return CSpaceAction.DELETE;
        } else if (ActionType.SEARCH.equals(action)) {
            return CSpaceAction.SEARCH;
        } else if (ActionType.ADMIN.equals(action)) {
            return CSpaceAction.ADMIN;
        } else if (ActionType.START.equals(action)) {
            return CSpaceAction.START;
        } else if (ActionType.STOP.equals(action)) {
            return CSpaceAction.STOP;
        }
        throw new IllegalArgumentException("action = " + action.toString());
    }
    
    /**
     * getAction is a conveneniece method to get action
     * for given HTTP method invoked on the resource
     * @param method http method
     * @return
     */
    public static CSpaceAction getAction(String method) {

        if ("POST".equalsIgnoreCase(method)) {
            return CSpaceAction.CREATE;
        } else if ("GET".equalsIgnoreCase(method)) {
            return CSpaceAction.READ;
        } else if ("PUT".equalsIgnoreCase(method)) {
            return CSpaceAction.UPDATE;
        } else if ("DELETE".equalsIgnoreCase(method)) {
            return CSpaceAction.DELETE;
        } else {
            //for HEAD, OPTIONS, etc. return READ
            return CSpaceAction.READ;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("URIResourceImpl [");
        builder.append(", method=");
        builder.append(method);
        builder.append(", uri=");
        builder.append(uri);
        builder.append("]");
        return builder.toString() + " " + super.toString();
    }
}
