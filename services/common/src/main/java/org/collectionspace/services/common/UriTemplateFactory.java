/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright © 2009-2012 University of California, Berkeley
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.collectionspace.services.common;

import java.util.Map;
import org.collectionspace.services.client.AuthorityClient;

/**
 * UriTemplateFactory.java
 *
 * A factory for building instances of subclasses of URITemplate, based on a
 * provided template type.
 */
public class UriTemplateFactory {
    
    // For coding convenience, each item in the enum is also available as a field
    // in the enclosing class.
    public final static UriTemplateType RESOURCE = UriTemplateType.RESOURCE;
    public final static UriTemplateType ITEM = UriTemplateType.ITEM;
    public final static UriTemplateType CONTACT = UriTemplateType.CONTACT;

    public enum UriTemplateType {

        RESOURCE, ITEM, CONTACT
    };
    
    public final static String SERVICENAME_VAR = "servicename";
    public final static String IDENTIFIER_VAR = "identifier";
    public final static String ITEM_IDENTIFIER_VAR = "itemIdentifier";
    public final static String CONTACT_IDENTIFIER_VAR = "contactIdentifier";
    
    public final static String AUTHORITY_ITEM_PATH_COMPONENT = AuthorityClient.ITEMS;
    // FIXME: Get this currently hard-coded value from an external authoritative source.
    // The only candidate so far identified is ContactClient.SERVICE_PATH_COMPONENT;
    // is this appropriate?
    public final static String CONTACT_PATH_COMPONENT = "contacts";

    public final static String RESOURCE_PATH_PATTERN =
            "/"
            + "{" + SERVICENAME_VAR + "}"
            + "/"
            + "{" + IDENTIFIER_VAR + "}";
    public final static String ITEM_PATH_PATTERN =
            RESOURCE_PATH_PATTERN
            + "/"
            + AUTHORITY_ITEM_PATH_COMPONENT
            + "/"
            + "{" + ITEM_IDENTIFIER_VAR + "}";
    public final static String CONTACT_PATH_PATTERN =
            ITEM_PATH_PATTERN
            + "/"
            + CONTACT_PATH_COMPONENT
            + "/"
            + "{" + CONTACT_IDENTIFIER_VAR + "}";

    public static StoredValuesUriTemplate getURITemplate(UriTemplateType type) {
        return new StoredValuesUriTemplate(type, getUriPathPattern(type));
    }
    
    public static StoredValuesUriTemplate getURITemplate(UriTemplateType type, Map<String,String> storedValuesMap) {
        return new StoredValuesUriTemplate(type, getUriPathPattern(type), storedValuesMap);
    }

    private static String getUriPathPattern(UriTemplateType type) {
        switch (type) {
            case RESOURCE:
                return RESOURCE_PATH_PATTERN;

            case ITEM:
                return ITEM_PATH_PATTERN;

            case CONTACT:
                return CONTACT_PATH_PATTERN;

            default:
                return RESOURCE_PATH_PATTERN;
        }
    }

}