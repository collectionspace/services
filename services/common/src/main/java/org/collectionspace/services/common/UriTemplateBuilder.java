/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright 2009-2012 University of California, Berkeley
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

public class UriTemplateBuilder {

    UriTemplateType uriTemplateType = null;
    final static String RESOURCE_TEMPLATE_PATTERN =
            "/{servicename}/{identifier}";
    // FIXME: Get static strings below (e.g. "items") from already-declared
    // constants elsewhere
    final static String ITEM_TEMPLATE_PATTERN =
            "/{servicename}/{identifier}/items/{itemIdentifier}";
    final static String CONTACT_TEMPLATE_PATTERN =
            "/{servicename}/{identifier}/items/{itemIdentifier}/contacts/{contactIdentifier}";
    final static UriTemplate RESOURCE_URI_TEMPLATE =
            new UriTemplate(RESOURCE_TEMPLATE_PATTERN);
    final static UriTemplate ITEM_URI_TEMPLATE =
            new UriTemplate(ITEM_TEMPLATE_PATTERN);
    final static UriTemplate CONTACT_URI_TEMPLATE =
            new UriTemplate(CONTACT_TEMPLATE_PATTERN);

    public UriTemplateBuilder(UriTemplateType type) {
        this.uriTemplateType = type;
    }

    public UriTemplateType getType() {
        return this.uriTemplateType;
    }

    @Override
    public String toString() {
        return "URI Builder of type " + getType().toString();
    }

    public UriTemplate getURITemplate() {
        switch (uriTemplateType) {
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