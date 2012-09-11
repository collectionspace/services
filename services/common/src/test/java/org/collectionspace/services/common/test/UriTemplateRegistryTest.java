/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright (c) 2012 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.collectionspace.services.common.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.StoredValuesUriTemplate;
import org.collectionspace.services.common.UriTemplateFactory;
import org.collectionspace.services.common.UriTemplateRegistry;
import org.collectionspace.services.common.UriTemplateRegistryKey;
import org.collectionspace.services.common.api.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class UriTemplateRegistryTest {

    private static final Logger logger = LoggerFactory.getLogger(UriTemplateRegistryTest.class);
    UriTemplateRegistry registry;
    final static String TEST_TENANT_ID = "1";
    final static String TEST_DOCTYPE_NAME = "Doctype";
    final static String TEST_TEMPATE = "/doctypes";
    final static UriTemplateFactory.UriTemplateType TEST_URI_TEMPLATE_TYPE =
            UriTemplateFactory.RESOURCE;

    private void testBanner(String msg) {
        String BANNER = "-------------------------------------------------------";
        logger.debug("\r" + BANNER + "\r\n" + this.getClass().getName() + "\r\n" + msg + "\r\n" + BANNER);
    }

    /**
     * Create a test entry in the registry.
     */
    @BeforeSuite
    private void setUp() {
        UriTemplateRegistryKey key = new UriTemplateRegistryKey(TEST_TENANT_ID, TEST_DOCTYPE_NAME);
        Map<String,String> storedValues = new HashMap<String,String>();
        StoredValuesUriTemplate template = new StoredValuesUriTemplate(TEST_URI_TEMPLATE_TYPE, TEST_TEMPATE, storedValues);
        registry = new UriTemplateRegistry();
        registry.put(key, template);
    }

    @Test
    public void registryContainsEntries() {
        testBanner("registryContainsEntries");
        Assert.assertNotNull(registry);
        Assert.assertFalse(registry.isEmpty());
    }

    /**
     * Identify a valid entry in the registry, then use its key to successfully
     * retrieve the entry once again.
     */
    @Test(dependsOnMethods = {"registryContainsEntries"})
    public void getRegistryEntryByKey() {
        testBanner("getRegistryEntryByKey");
        UriTemplateRegistryKey key;
        StoredValuesUriTemplate template;
        boolean hasValidKey = false;
        boolean hasValidTemplate = false;
        for (Map.Entry<UriTemplateRegistryKey, StoredValuesUriTemplate> entry : registry.entrySet()) {
            key = entry.getKey();
            template = entry.getValue();
            if (key != null && Tools.notBlank(key.getTenantId()) && Tools.notBlank(key.getDocType())) {
                hasValidKey = true;
            }
            if (template != null && template.getUriTemplateType() != null && Tools.notBlank(template.toString())) {
                hasValidTemplate = true;
            }
            if (hasValidKey && hasValidTemplate) {
                break;
            }
            Assert.assertTrue(hasValidKey && hasValidTemplate);
            StoredValuesUriTemplate retrievedTemplate = registry.get(key);
            Assert.assertNotNull(retrievedTemplate);
            Assert.assertEquals(template.toString(), retrievedTemplate.toString());

        }
    }
}
