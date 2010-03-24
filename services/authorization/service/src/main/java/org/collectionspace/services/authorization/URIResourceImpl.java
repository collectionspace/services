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
 *//**
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

/**
 * A security resource that represents URI and method invoked on it
 * @author 
 */
public class URIResourceImpl extends CSpaceResourceImpl {

    private String uri;
    private String method;
    private CSpaceAction action;

    /**
     * constructor that is usually called from service runtime
     * @param uri
     * @param method an http method
     */
    public URIResourceImpl(String uri, String method) {
        super(getParent(uri) + "#" + getAction(method).toString(), TYPE.URI);
        action = getAction(method);
        this.uri = uri;
        this.method = method;
    }

    /**
     * constructor that is usually called from administrative interface
     * @param resourceName
     * @param actionType
     */
    public URIResourceImpl(String resourceName, ActionType actionType) {
        //FIXME more validation might be needed
        super(resourceName + "#" + getAction(actionType).toString(), TYPE.URI);
        action = getAction(actionType);
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * getAction a convenience method to get action invoked on the resource
     */
    @Override
    public CSpaceAction getAction() {
        return action;
    }

    private static String getParent(String uri) {
        StringTokenizer stz = new StringTokenizer(uri, "/");
        //FIXME the following ignores sub resources as well as object instances
        return stz.nextToken();
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
        }
        throw new IllegalStateException("no method found!");
    }

    /**
     * getAction is a convenience method to get corresponding action for
     * given ActionType
     * @param action
     * @return
     */
    public static CSpaceAction getAction(ActionType action) {
        if (ActionType.CREATE.equals(action)) {
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
}
