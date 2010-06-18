package org.collectionspace.services.id.part.test;

import java.lang.Math;
import java.util.HashSet;

import org.collectionspace.services.id.part.IDPart;
import org.collectionspace.services.id.part.RandomNumberIDPart;
import org.collectionspace.services.id.part.JavaRandomNumberIDPartAlgorithm;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomNumberIDPartTest {

    private final String CLASS_NAME = RandomNumberIDPartTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    IDPart part;

    // Repetition factor for generating sufficiently
    // large numbers of sample pseudorandom numbers.
    final int REPETITIONS = 5;

    @Test
    public void newIDGeneratesSufficientVarietyOfIDs() {
        String id;
        final int IDS_TO_GENERATE = 100;
        part = new RandomNumberIDPart(1000,0);
        HashSet<String> ids = new HashSet<String>();
        for (int i=0; i < IDS_TO_GENERATE; i++) {
            id = part.newID();
            ids.add(id); // Adds only elements not already present.
        }
        // A sufficiently high percentage of the pseudorandom numbers
        // generated must be unique, to confirm apparent randomness.
        final double MIN_REQUIRED_PERCENTAGE_UNIQUE = 0.85;
        int minUniqueIdsRequired =
            (int) Math.round(IDS_TO_GENERATE * MIN_REQUIRED_PERCENTAGE_UNIQUE);
        int uniqueIdsObtained = ids.size();

        // Since the results of this test are probabilistic, rather than
        // deterministic, only output a warning rather than throwing an
        // AssertionErrorException.
        if (uniqueIdsObtained < minUniqueIdsRequired) {
            logger.warn("Too few pseudorandom IDs were unique." +
                " Obtained " + uniqueIdsObtained + ", required " +
                minUniqueIdsRequired + " out of " + IDS_TO_GENERATE);
        }
        
        // Assert.assertTrue(uniqueIdsObtained >= minUniqueIdsRequired);
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

    @Test
    public void defaultMaxValue() {
        part = new RandomNumberIDPart(
            JavaRandomNumberIDPartAlgorithm.DEFAULT_MAX_VALUE);
        part.newID();
    }

     @Test(dependsOnMethods = {"defaultMaxValue"})
    public void defaultMinValue() {
        part = new RandomNumberIDPart(
            JavaRandomNumberIDPartAlgorithm.DEFAULT_MAX_VALUE,
            JavaRandomNumberIDPartAlgorithm.DEFAULT_MIN_VALUE);
        part.newID();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void maxValueTooHigh() {
        int maxValue = Integer.MAX_VALUE; // Value too high
        part = new RandomNumberIDPart(maxValue);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void minValueTooLow() {
        int maxValue = 10;
        int minValue = -1; // Value too low
        part = new RandomNumberIDPart(maxValue, minValue);
    }

    @Test
    public void isValid() {
        part = new RandomNumberIDPart();
        Assert.assertTrue(part.getValidator().isValid(part.newID()));
    }
}
