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
import java.util.Map;
import org.collectionspace.services.common.UriTemplate;
import org.collectionspace.services.common.UriTemplateFactory;
import org.collectionspace.services.common.api.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UriTemplateTest {
    
    final static String EXAMPLE_SERVICE_NAME = "examples";
    final static String CSID = "a87f6616-4146-4c17-a41a-048597cc12aa";

    private static final Logger logger = LoggerFactory.getLogger(UriTemplateTest.class);

    private void testBanner(String msg) {
        String BANNER = "-------------------------------------------------------";
        logger.debug("\r" + BANNER + "\r\n" + this.getClass().getName() + "\r\n" + msg + "\r\n" + BANNER);
    }

    @Test
    public void createResourceUriTemplate() {
        testBanner("createResourceUriTemplate");
        UriTemplate resourceTemplate = UriTemplateFactory.getURITemplate(UriTemplateFactory.UriTemplateType.RESOURCE);
        Assert.assertNotNull(resourceTemplate, "Resource template is null.");
        logger.debug("Resource template URI path = " + resourceTemplate.toString());
        Assert.assertNotNull(resourceTemplate.toString(), "Resource template URI path is null.");
        Assert.assertEquals(resourceTemplate.toString(), UriTemplateFactory.RESOURCE_TEMPLATE_PATTERN,
                "Resource template URI path doesn't match expected path.");
    }
    
    @Test (dependsOnMethods = {"createResourceUriTemplate"})
    public void buildResourceUri() {
        testBanner("buildResourceUri");
        UriTemplate resourceTemplate = UriTemplateFactory.getURITemplate(UriTemplateFactory.UriTemplateType.RESOURCE);
        Map<String,String> resourceUriVars = new HashMap<String,String>();
        resourceUriVars.put("servicename", EXAMPLE_SERVICE_NAME);
        resourceUriVars.put("identifier", CSID);
        String uriStr = resourceTemplate.buildUri(resourceUriVars);
        Assert.assertFalse(Tools.isBlank(uriStr), "Generated URI string is null or blank.");
        logger.debug("Generated URI string = " + uriStr);
    }
}
