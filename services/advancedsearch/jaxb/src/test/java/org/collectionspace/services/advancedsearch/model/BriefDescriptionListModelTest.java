package org.collectionspace.services.advancedsearch.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.collectionspace.services.collectionobject.BriefDescriptionList;
import org.collectionspace.services.collectionobject.ObjectFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test that we truncate the Brief Description when required
 */
public class BriefDescriptionListModelTest {
    private final ObjectFactory objectFactory = new ObjectFactory();

    @Test
    public void testShortBriefDescription() {
        final String description = "short description";
        final BriefDescriptionList briefDescriptionList = objectFactory.createBriefDescriptionList();
        briefDescriptionList.getBriefDescription().add(description);

        Assert.assertEquals(BriefDescriptionListModel.briefDescriptionListToDisplayString(briefDescriptionList),
                            description);

    }

    @Test
    public void testTruncatedBriefDescription() {
        String longDescription = RandomStringUtils.randomAlphabetic(300);
        final BriefDescriptionList briefDescriptionList = objectFactory.createBriefDescriptionList();
        briefDescriptionList.getBriefDescription().add(longDescription);

        String truncated = BriefDescriptionListModel.briefDescriptionListToDisplayString(briefDescriptionList);
        Assert.assertTrue(longDescription.length() > truncated.length());
        Assert.assertTrue(truncated.endsWith("..."));
    }
}