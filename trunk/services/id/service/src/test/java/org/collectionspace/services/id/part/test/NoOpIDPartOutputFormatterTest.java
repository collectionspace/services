package org.collectionspace.services.id.part.test;

import org.collectionspace.services.id.part.NoOpIDPartOutputFormatter;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NoOpIDPartOutputFormatterTest {

    NoOpIDPartOutputFormatter formatter = new NoOpIDPartOutputFormatter();

    @Test
    public void format() {
        Assert.assertEquals("any string", formatter.format("any string"));
    }

    @Test(dependsOnMethods = {"format"})
    public void formatWithNullAndEmptyValues() {
        Assert.assertEquals(null, formatter.format(null));
        Assert.assertEquals("", formatter.format(""));
    }
}
