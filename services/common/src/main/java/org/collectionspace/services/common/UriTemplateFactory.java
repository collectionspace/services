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
 * A factory for building instances of URITemplate classes, based on a provided
 * template type.
 */
public class UriTemplateFactory {

    public final static String RESOURCE_TEMPLATE_PATTERN =
            "/{servicename}/{identifier}";
    // FIXME: Get static strings below (e.g. "items", "contacts") from
    // already-declared constants elsewhere
    public final static String ITEM_TEMPLATE_PATTERN =
            "/{servicename}/{identifier}/items/{itemIdentifier}";
    public final static String CONTACT_TEMPLATE_PATTERN =
            "/{servicename}/{identifier}/items/{itemIdentifier}/contacts/{contactIdentifier}";
    
    private final static StoredValuesUriTemplate RESOURCE_URI_TEMPLATE =
            new StoredValuesUriTemplate(RESOURCE_TEMPLATE_PATTERN);
    private final static StoredValuesUriTemplate ITEM_URI_TEMPLATE =
            new StoredValuesUriTemplate(ITEM_TEMPLATE_PATTERN);
    private final static StoredValuesUriTemplate CONTACT_URI_TEMPLATE =
            new StoredValuesUriTemplate(CONTACT_TEMPLATE_PATTERN);

    public static StoredValuesUriTemplate getURITemplate(UriTemplateType type) {
        switch (type) {
            case RESOURCE:
                return RESOURCE_URI_TEMPLATE;

            case ITEM:
                return ITEM_URI_TEMPLATE;

            case CONTACT:
                return CONTACT_URI_TEMPLATE;

            default:
                return RESOURCE_URI_TEMPLATE;
        }
    }

    public enum UriTemplateType {

        RESOURCE, ITEM, CONTACT
    };
}