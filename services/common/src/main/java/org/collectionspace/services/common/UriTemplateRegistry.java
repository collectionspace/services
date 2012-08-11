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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.collectionspace.services.common.UriTemplateRegistryKey;
import org.collectionspace.services.common.UriTemplateFactory.UriTemplateType;

/**
 * UriTemplateRegistry.java
 *
 * Maps document types to templates for building URIs, per tenant.
 */
public class UriTemplateRegistry extends HashMap<UriTemplateRegistryKey, Map<UriTemplateType, StoredValuesUriTemplate>> {
    
    /**
     * Get a URI template by tenant, document type, and template type.
     * 
     */
    public StoredValuesUriTemplate get(UriTemplateRegistryKey key, UriTemplateType type) {
        if (get(key) != null) {
            return get(key).get(type);
        } else {
            return null;
        }
    }

    /**
     * Dump all registry settings, For debugging purposes.
     */
    public void dump() {
        for (Map.Entry<UriTemplateRegistryKey, Map<UriTemplateType, StoredValuesUriTemplate>> uriTemplateEntry : this.entrySet()) {

            System.out.println(
                    "Tenant : DocType = "
                    + uriTemplateEntry.getKey().getTenantId()
                    + " : "
                    + uriTemplateEntry.getKey().getDocType());

            System.out.println(" Value(s) of TemplateType : Template = ");
            for (Map.Entry<UriTemplateType, StoredValuesUriTemplate> template : uriTemplateEntry.getValue().entrySet()) {
                System.out.println(
                        "  "
                        + template.getKey()
                        + " : "
                        + template.getValue().toString());
            }
        }
    }
}
