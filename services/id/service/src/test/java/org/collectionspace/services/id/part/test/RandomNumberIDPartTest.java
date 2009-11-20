package org.collectionspace.services.id.part.test;

import org.collectionspace.services.id.part.IDPart;
import org.collectionspace.services.id.part.RandomNumberIDPart;
import org.collectionspace.services.id.part.JavaRandomNumberIDPartAlgorithm;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class RandomNumberIDPartTest {

    IDPart part;
    String firstID;
    String secondID;
    String thirdID;

    @BeforeSuite
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

    @Test(dependsOnMethods = {"newIDGeneratesNonRepeatingIDs"})
    public void newIDGeneratesNonRepeatingIDsWithSuppliedValues() {
        part = new RandomNumberIDPart(200, 100);
        firstID = part.newID();
        secondID = part.newID();
        thirdID = part.newID();
        Assert.assertTrue(firstID.compareTo(secondID) != 0);
        Assert.assertTrue(firstID.compareTo(thirdID) != 0);
        Assert.assertTrue(secondID.compareTo(thirdID) != 0);
    }

    @Test
    public void highMinAndMaxValues() {
        int minValue = Integer.MAX_VALUE - 1000;
        int maxValue = Integer.MAX_VALUE - 2;
        String id;
        part = new RandomNumberIDPart(maxValue, minValue);
        for (int i=0; i < 20; i++) {
            id = part.newID();
            Assert.assertTrue(Integer.parseInt(id) >= minValue);
        }
    }

    @Test
    public void newIDsLessThanOrEqualToSuppliedMaxValue() {

        // With only maximum value specified.
        int maxValue = 20;
        String id;
        // Generate a sufficient number of values that
        // there is a high probability of realizing an
        // error condition, if any.
        part = new RandomNumberIDPart(maxValue);
        for (int i=0; i < (maxValue * 5); i++) {
            id = part.newID();
            Assert.assertTrue(Integer.parseInt(id) <= maxValue);
        }

        // With minimum value also specified.
        int minValue = 5;
        part = new RandomNumberIDPart(maxValue, minValue);
        for (int i=0; i < ((maxValue - minValue) * 5); i++) {
            id = part.newID();
            Assert.assertTrue(Integer.parseInt(id) <= maxValue);
        }
    }

    @Test(dependsOnMethods = {"newIDsLessThanOrEqualToSuppliedMaxValue"})
    public void newIDsHigherThanOrEqualToSuppliedMinValue() {
        int minValue = 5;
        int maxValue = 20;
        String id;
        part = new RandomNumberIDPart(maxValue, minValue);
        for (int i=0; i <= ((maxValue - minValue) * 5); i++) {
            id = part.newID();
            Assert.assertTrue(Integer.parseInt(id) >= minValue);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void minValueTooLow() {
        int minValue = -1;
        part = new RandomNumberIDPart(
            JavaRandomNumberIDPartAlgorithm.DEFAULT_MAX_VALUE, minValue);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void maxValueTooHigh() {
        part = new RandomNumberIDPart(Integer.MAX_VALUE);
    }
}
