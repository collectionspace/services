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

/**
 * UriTemplateFactory.java
 *
 * A factory for building instances of subclasses of URITemplate, based on a
 * provided template type.
 */
public class UriTemplateFactory {

    public final static String RESOURCE_PATH_PATTERN =
            "/{servicename}/{identifier}";
    // FIXME: Get static strings below (e.g. "items", "contacts") from
    // already-declared constants elsewhere
    public final static String ITEM_PATH_PATTERN =
            "/{servicename}/{identifier}/items/{itemIdentifier}";
    public final static String CONTACT_PATH_PATTERN =
            "/{servicename}/{identifier}/items/{itemIdentifier}/contacts/{contactIdentifier}";

    public static StoredValuesUriTemplate getURITemplate(UriTemplateType type) {
        return new StoredValuesUriTemplate(getUriPathPattern(type));
    }
    
    public static StoredValuesUriTemplate getURITemplate(UriTemplateType type, Map<String,String> storedValuesMap) {
        StoredValuesUriTemplate template = new StoredValuesUriTemplate(getUriPathPattern(type));
        template.setStoredValuesMap(storedValuesMap);
        return template;
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

    public enum UriTemplateType {

        RESOURCE, ITEM, CONTACT
    };
}