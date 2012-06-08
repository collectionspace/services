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

/**
 * StoredValuesUriTemplate.java
 *
 * Generates URI strings by combining a URI template with provided values, which
 * replace variables within the template.
 *
 * In this subclass, some of the values which will replace variables in a URI
 * template are static, and can be stored alongside the template for reuse.
 * Additional values that will replace variables within the template are also
 * dynamically accepted, and are merged with stored values when building URIs.
 */
public class StoredValuesUriTemplate extends UriTemplate {

    private Map<String, String> storedValuesMap = new HashMap<String, String>();

    public StoredValuesUriTemplate(String path, Map<String, String> storedValuesMap) {
        super(path);
        setStoredValuesMap(storedValuesMap);
    }

    private void setStoredValuesMap(Map<String, String> storedValuesMap) {
        if (storedValuesMap != null && !storedValuesMap.isEmpty()) {
            this.storedValuesMap = storedValuesMap;
        }
    }

    private Map<String, String> getStoredValuesMap() {
        return this.storedValuesMap;
    }

    /**
     * Builds a URI string from a combination of previously-stored values (such
     * as static URI path components) and additional values (such as resource
     * identifiers), both of which will replace variables within the URI
     * template.
     *
     * @param varsMap a map of values that will replace variables within the URI
     * template
     * @return a URI string
     */
    public String buildUri(Map<String, String> additionalValuesMap) {
        Map<String, String> allValuesMap = new HashMap<String, String>();
        allValuesMap.putAll(getStoredValuesMap());
        if (storedValuesMap != null && !storedValuesMap.isEmpty()) {
            allValuesMap.putAll(additionalValuesMap);
        }
        return super.buildUri(allValuesMap);
    }
}
