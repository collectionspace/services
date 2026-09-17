package org.collectionspace.services.common.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Assert;
import org.testng.annotations.Test;

/**
 * Test url normalization for {@link ResponseTimeFilter}
 */
public class ResponseTimeFilterTest {

    // replacement strings found in urls
    final String csid = ":csid";
    final String refname = ":refname";

    // test urls
    final String singleUuid = "/media/65ad6ad8-8ce9-4993-9923-ec3650342f8e/authorityrefs";
    final String multiUuid = "/conceptauthorities/c2c1dbac-6f03-45da-8799/items/60f021cc-ad1c-49e1-a7dd";
    final String singleRef = "/orgauthorities/urn:cspace:name(organization)/items";
    final String multiRef = "/placeauthorities/urn:cspace:name(place)/items/urn:cspace:name(testname1512066142927)";
    final String uuidAndRef = "/orgauthorities/urn:cspace:name(organization)/items/088331d4-8ba9-4777-a30d/authorityrefs";

    ResponseTimeFilter filter = new ResponseTimeFilter();

    private int countMatches(String regex, String input) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        int matches = 0;
        while (matcher.find()) {
            matches++;
        }

        return matches;
    }

    @Test
    public void testSingleUuid() {
        String normalized = filter.normalizeUri(singleUuid);
        int numMatches = countMatches(csid, normalized);
        Assert.assertEquals(1, numMatches);
    }

    @Test
    public void testMultipleUuids() {
        String normalized = filter.normalizeUri(multiUuid);
        int numMatches = countMatches(csid, normalized);
        Assert.assertEquals(2, numMatches);
    }


    @Test
    public void testSingleRefname() {
        String normalized = filter.normalizeUri(singleRef);
        int numMatches = countMatches(refname, normalized);
        Assert.assertEquals(1, numMatches);
    }

    @Test
    public void testMultipleRefnames() {
        String normalized = filter.normalizeUri(multiRef);
        int numMatches = countMatches(refname, normalized);
        Assert.assertEquals(2, numMatches);
    }

    @Test
    public void testUuidAndRefname() {
        String normalized = filter.normalizeUri(uuidAndRef);
        int uuidMatches = countMatches(csid, normalized);
        int refMatches = countMatches(refname, normalized);
        Assert.assertEquals(1, uuidMatches);
        Assert.assertEquals(1, refMatches);
    }

}