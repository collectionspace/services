package org.collectionspace.services.id.part.test;

import java.util.HashSet;

import org.collectionspace.services.id.part.IDPart;
import org.collectionspace.services.id.part.RandomNumberIDPart;
import org.collectionspace.services.id.part.JavaRandomNumberIDPartAlgorithm;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RandomNumberIDPartTest {

    IDPart part;

    // Repetition factor for generating sufficiently
    // large numbers of sample pseudorandom numbers.
    final int REPETITIONS = 5;

    @Test
    public void newIDGeneratesSufficientVarietyOfIDs() {
        String id;
        part = new RandomNumberIDPart(1000,0);
        int idsGenerated = 100;
        HashSet<String> ids = new HashSet<String>();
        for (int i=0; i < idsGenerated; i++) {
            id = part.newID();
            ids.add(id); // Adds only elements not already present.
        }
        // A sufficiently high percentage of the pseudorandom numbers
        // generated must be unique, to confirm apparent randomness.
        double percentMustBeUnique = 0.9;
        Assert.assertTrue(ids.size() >= (idsGenerated * percentMustBeUnique));
    }

    // @TODO Consider another test to look at some measure of
    // even distribution of generated pseudorandom numbers
    // across a midpoint, or across some bands (e.g. in quartiles).

    @Test
    public void IDsWithinBoundsOfHighMinAndMaxValues() {
        int minValue = Integer.MAX_VALUE - 10;
        int maxValue = Integer.MAX_VALUE - 2;
        String id;
        part = new RandomNumberIDPart(maxValue, minValue);
        // Generate a sufficient number of values that
        // there is a high probability of generating an
        // out of bounds value, if any.
        for (int i=0; i < (((maxValue - minValue) + 1) * REPETITIONS); i++) {
            id = part.newID();
            Assert.assertTrue(Integer.parseInt(id) >= minValue);
            Assert.assertTrue(Integer.parseInt(id) <= maxValue);
        }
    }

    @Test
    public void newIDsLessThanOrEqualToSuppliedMaxValue() {

        // With only maximum value specified.
        int maxValue = 20;
        String id;
        part = new RandomNumberIDPart(maxValue);
        for (int i=0; i < (maxValue * REPETITIONS); i++) {
            id = part.newID();
            Assert.assertTrue(Integer.parseInt(id) <= maxValue);
        }

        // With minimum value also specified.
        int minValue = 5;
        part = new RandomNumberIDPart(maxValue, minValue);
        for (int i=0; i < (((maxValue - minValue) + 1) * REPETITIONS); i++) {
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
        for (int i=0; i <= (((maxValue - minValue) + 1) * REPETITIONS); i++) {
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

    @Test
    public void isValid() {
        part = new RandomNumberIDPart();
        Assert.assertTrue(part.getValidator().isValid(part.newID()));
    }
}
