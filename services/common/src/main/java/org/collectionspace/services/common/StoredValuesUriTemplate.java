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
import org.collectionspace.services.common.UriTemplateFactory.UriTemplateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StoredValuesUriTemplate.java
 *
 * Generates URI strings by combining a URI template with provided values, which
 * replace variables within the template.
 *
 * In this subclass of UriTemplate, some of the values which will replace
 * variables in an internal URI template are typically stable / static, and
 * can be stored alongside the URI template for reuse each time that a URI
 * is built from the template. Additional values that will replace variables
 * within the template on a one-off basis are also dynamically accepted,
 * and will be merged with the previously-stored values when building URIs.
 */
public class StoredValuesUriTemplate extends UriTemplate {

    private static final Logger logger = LoggerFactory.getLogger(StoredValuesUriTemplate.class);
    private Map<String, String> storedValuesMap = new HashMap<String, String>();

    public StoredValuesUriTemplate(UriTemplateType type, String path) {
        super(type, path);
    }

    public StoredValuesUriTemplate(UriTemplateType type, String path, Map<String, String> storedValuesMap) {
        super(type, path);
        setStoredValues(storedValuesMap);
    }

    public void setStoredValuesMap(Map<String, String> storedValuesMap) {
        setStoredValues(storedValuesMap);
    }

    // Called by both constructor and public setter
    // Avoids having a subclass-overridable call in the constructor
    // See http://stackoverflow.com/questions/6104262/java-overridable-call-in-constructor#comment7087131_6104273
    private void setStoredValues(Map<String, String> storedValuesMap) {
        if (storedValuesMap != null && !storedValuesMap.isEmpty()) {
            this.storedValuesMap = storedValuesMap;
        }
    }

    public Map<String, String> getStoredValuesMap() {
        return this.storedValuesMap;
    }

    /**
     * Builds a URI string from a combination of values previously stored
     * along with the template, if any (typically stable URI path components,
     * such as "intakes" and "personauthorities") and additional values, if any
     * (typically CSIDs and URN-based resource identifiers), both of which will
     * replace variables within the URI template.
     *
     * @param additionalValuesMap an optional map of values that will replace
     * variables within the URI template
     * @return a URI string
     */
    @Override
    public String buildUri(Map<String, String> additionalValuesMap) {
        Map<String, String> allValuesMap = new HashMap<String, String>();
        try {
            Map<String, String> storedValsMap = getStoredValuesMap();
            if (storedValsMap != null && !storedValsMap.isEmpty()) {
                allValuesMap.putAll(storedValsMap);
            }
            if (additionalValuesMap != null && !additionalValuesMap.isEmpty()) {
                allValuesMap.putAll(additionalValuesMap);
            }
        } catch (Exception e) {
            logger.warn("Some values could not be added to values map when building URI string: " + e.getMessage());
        }
        return super.buildUri(allValuesMap);
    }
}
