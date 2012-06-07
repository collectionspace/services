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

// import org.jboss.resteasy.spi.touri;

public class UriBuilder {

    UriBuilderType uriBuilderType = null;

    public UriBuilder(UriBuilderType type) {
        this.uriBuilderType = type;
    }

    public UriBuilderType getType() {
        return this.uriBuilderType;
    }

    @Override
    public String toString() {
        return "URI Builder of type " + getType().toString();
    }
    // Placeholder
    String uriTemplate = "replace with true URITemplate object";

    public String getURITemplate() {
        switch (uriBuilderType) {
            case RESOURCE:
                return uriTemplate;

            case ITEM:
                return uriTemplate;

            case CONTACT:
                return uriTemplate;

            default:
                return uriTemplate;
        }
    }

    public enum UriBuilderType {
        RESOURCE, ITEM, CONTACT
    };
}