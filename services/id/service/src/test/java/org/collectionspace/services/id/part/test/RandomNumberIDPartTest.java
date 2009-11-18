package org.collectionspace.services.id.part.test;

import org.collectionspace.services.id.part.IDPart;
import org.collectionspace.services.id.part.RandomNumberIDPart;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class RandomNumberIDPartTest {

    IDPart part;
    String firstID;
    String secondID;
    String thirdID;

    @BeforeTest
    public void setUp() {
        part = new RandomNumberIDPart();
        firstID = part.newID();
        secondID = part.newID();
        thirdID = part.newID();
    }

    @Test
    public void newIDGeneratesNonRepeatingIDs() {
        Assert.assertTrue(firstID.compareTo(secondID) != 0);
        Assert.assertTrue(firstID.compareTo(thirdID) != 0);
        Assert.assertTrue(secondID.compareTo(thirdID) != 0);
    }

    @Test
    public void isValid() {
        Assert.assertTrue(part.getValidator().isValid(firstID));
        Assert.assertTrue(part.getValidator().isValid(secondID));
        Assert.assertTrue(part.getValidator().isValid(thirdID));
    }

}
