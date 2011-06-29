/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2011 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.test;

import org.collectionspace.services.common.IFragmentHandler;
import org.collectionspace.services.common.XmlSaxFragmenter;
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.imports.ImportsResource;
import org.collectionspace.services.imports.TemplateExpander;
import org.dom4j.Document;
import org.dom4j.Element;
import org.restlet.util.Template;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;

import java.io.File;

/**  Run this with something like:
 *     cd C:\src\trunk\services\imports\service
 *     mvn test -Dtest=ImportsServiceTest
 */
public class ImportsServiceTest {
    //These paths won't work when deployed in jboss, but they will work in the build in the source tree, which is what this test is for.
    public static final String TEMPLATES_REL_DIR_TO_MODULE = "./src/main/resources/templates";
    public static final String REQUESTS_REL_DIR_TO_MODULE = "./src/test/resources/requests";
    public static final String BOGUS_TENANT_ID = "-1";

    /** this test just attempts to expand a single file upload to nuxeo's import/export file/directory format,
     *   but does not do the import, so that this test may be run without a nuxeo instance running.
     * @throws Exception
     */
    @Test
    public void testImports() throws Exception {
        String TEMPLATE_DIR = (new File(TEMPLATES_REL_DIR_TO_MODULE)).getCanonicalPath();
        String REQUESTS_DIR = (new File(REQUESTS_REL_DIR_TO_MODULE)).getCanonicalPath();
        String outputDir = FileTools.createTmpDir("imports-test-").getCanonicalPath();

        String xmlPayload = FileTools.readFile(REQUESTS_DIR,"authority-request.xml");
        InputSource inputSource = ImportsResource.payloadToInputSource(xmlPayload);
        ImportsResource.expandXmlPayloadToDir(BOGUS_TENANT_ID, inputSource, TEMPLATE_DIR, outputDir);

        //TODO: inspect dir, then *cleanup*!!
    }
}
