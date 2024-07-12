/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.client.test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RestrictedMediaClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.restrictedmedia.LanguageList;
import org.collectionspace.services.restrictedmedia.RestrictedMediaCommon;
import org.collectionspace.services.restrictedmedia.SubjectList;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * RestrictedMediaServiceTest, carries out tests against a deployed and running Restricted Media Service.
 */
public class RestrictedMediaServiceTest extends AbstractPoxServiceTestImpl<AbstractCommonList, RestrictedMediaCommon> {

    private final Logger logger = LoggerFactory.getLogger(RestrictedMediaServiceTest.class);
    private static final String PUBLIC_URL_DECK = "https://farm8.staticflickr.com/7231/6962564226_4bdfc17599_k_d.jpg";

    private boolean mediaCleanup = true;

    /**
     * Sets up create tests.
     */
    @Override
    protected void setupCreate() {
        super.setupCreate();
        String noMediaCleanup = System.getProperty(NO_MEDIA_CLEANUP);
        if (Boolean.parseBoolean(noMediaCleanup)) {
            // Don't delete the blobs that we created during the test cycle
            this.mediaCleanup = false;
        }
    }

    private boolean isMediaCleanup() {
        return mediaCleanup;
    }

    @Override
    public String getServicePathComponent() {
        return RestrictedMediaClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return RestrictedMediaClient.SERVICE_NAME;
    }

    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new RestrictedMediaClient();
    }

    @Override
    protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
        return new RestrictedMediaClient(clientPropertiesFilename);
    }

    @Override
    protected AbstractCommonList getCommonList(Response response) {
        return response.readEntity(AbstractCommonList.class);
    }

    /**
     * Looks in the .../src/test/resources/blobs directory for files from which to create Blob
     * instances.
     *
     * @param testName the test name
     * @param fromUri - if 'true' then send the service a URI from which to create the blob.
     * @param fromUri - if 'false' then send the service a multipart/form-data POST from which to create the blob.
     * @throws Exception the exception
     */
    public void createBlob(String testName, boolean fromUri) throws Exception {
        setupCreate();

        // First create a restricted media record
        RestrictedMediaClient client = new RestrictedMediaClient();
        PoxPayloadOut multipart = createMediaInstance(createIdentifier());
        Response mediaRes = client.create(multipart);
        String mediaCsid = null;
        try {
            assertStatusCode(mediaRes, testName);
            mediaCsid = extractId(mediaRes);
        } finally {
            if (mediaRes != null) {
                mediaRes.close();
            }
        }

        String currentDir = getResourceDir();
        File blobsDir = new File(currentDir, BLOBS_DIR);
        File blob = findBlobForMedia(blobsDir);
        if (blob != null) {
            createBlob(blob, fromUri, testName, mediaCsid);
        } else {
            logger.warn("Could not find blobbable file in {}", blobsDir);
        }
    }

    /**
     * Iterate a directory for a file which matches isBlobbable
     *
     * @param blobsDir the directory to iterate
     * @return a blob or null
     */
    public File findBlobForMedia(File blobsDir) {
        if (blobsDir.exists() && blobsDir.canRead()) {
            File[] children = blobsDir.listFiles();
            if (children != null && children.length > 0) {
                // Since Media records can have only a single associated
                // blob, we'll stop after we find a valid candidate.
                for (File child : children) {
                    if (isBlobbable(child)) {
                        return child;
                    }
                }
            } else {
                logger.warn("Directory: {} is empty or cannot be read.", blobsDir);
            }
        } else {
            logger.warn("Directory: {} is missing or cannot be read.", blobsDir);
        }

        return null;
    }

    public void createBlob(File blobFile, boolean fromUri, String testName, String mediaCsid) throws Exception {
        logger.debug("Processing file URI: {}", blobFile.getAbsolutePath());

        String mimeType = getMimeType(blobFile);
        logger.debug("MIME type is: {}", mimeType);

        Response res;
        RestrictedMediaClient client = new RestrictedMediaClient();
        if (fromUri) {
            URL childUrl = blobFile.toURI().toURL();
            res = client.createBlobFromUri(mediaCsid, childUrl.toString());
        } else {
            MultipartFormDataOutput formData = new MultipartFormDataOutput();
            formData.addFormData("file", blobFile, MediaType.valueOf(mimeType));
            res = client.createBlobFromFormData(mediaCsid, formData);
        }

        try {
            assertStatusCode(res, testName);
            String blobCsid = extractId(res);
            if (isMediaCleanup()) {
                allResourceIdsCreated.add(blobCsid);
                allResourceIdsCreated.add(mediaCsid);
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test(dataProvider = "testName", dependsOnMethods = {"CRUDTests"})
    public void createWithBlobUri(String testName) throws Exception {
        createBlob(testName, true /*with URI*/);
    }

    @Test(dataProvider = "testName", dependsOnMethods = {"createWithBlobUri"})
    public void createMediaAndBlobWithUri(String testName) throws Exception {
        RestrictedMediaClient client = new RestrictedMediaClient();
        PoxPayloadOut multipart = createMediaInstance(createIdentifier());

        // purge the original
        Response mediaRes = client.createMediaAndBlobWithUri(multipart, PUBLIC_URL_DECK, true);
        String mediaCsid = null;
        try {
            assertStatusCode(mediaRes, testName);
            mediaCsid = extractId(mediaRes);
            if (isMediaCleanup()) {
                allResourceIdsCreated.add(mediaCsid);
            }
        } finally {
            if (mediaRes != null) {
                mediaRes.close();
            }
        }
    }

    @Test(dataProvider = "testName", dependsOnMethods = {"createWithBlobUri"})
    public void createWithBlobPost(String testName) throws Exception {
        createBlob(testName, false /*with POST*/);
    }

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------

    @Override
    protected PoxPayloadOut createInstance(String identifier) throws Exception {
        return createMediaInstance(identifier);
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    private PoxPayloadOut createMediaInstance(String title) throws Exception {
        String identifier = "media.title-" + title;
        RestrictedMediaCommon media = new RestrictedMediaCommon();
        media.setTitle(identifier);
        media.setIdentificationNumber(UUID.randomUUID().toString());
        media.setContributor("Joe-bob briggs");
        media.setCoverage("Lots of stuff");
        media.setPublisher("Ludicrum Enterprises");
        SubjectList subjects = new SubjectList();
        List<String> subjList = subjects.getSubject();
        subjList.add("Pints of blood");
        subjList.add("Much skin");
        media.setSubjectList(subjects);
        LanguageList languages = new LanguageList();
        List<String> langList = languages.getLanguage();
        langList.add("English");
        langList.add("German");
        media.setLanguageList(languages);
        PoxPayloadOut multipart = new PoxPayloadOut(RestrictedMediaClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(media, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new RestrictedMediaClient().getCommonPartName());

        logger.debug("to be created, media common");
        logger.debug(objectAsXmlString(media, RestrictedMediaCommon.class));

        return multipart;
    }

    @Override
    protected PoxPayloadOut createInstance(String commonPartName, String identifier) throws Exception {
        return createMediaInstance(identifier);
    }

    @Override
    protected RestrictedMediaCommon updateInstance(final RestrictedMediaCommon original) {
        RestrictedMediaCommon result = new RestrictedMediaCommon();
        result.setTitle("updated-" + original.getTitle());
        return result;
    }

    @Override
    protected void compareUpdatedInstances(RestrictedMediaCommon original, RestrictedMediaCommon updated) {
        Assert.assertEquals(updated.getTitle(), original.getTitle());
    }

    @Override
    @Test(dataProvider = "testName",
          dependsOnMethods = {"org.collectionspace.services.client.test.AbstractServiceTestImpl.baseCRUDTests"})
    public void CRUDTests(String testName) {}
}
