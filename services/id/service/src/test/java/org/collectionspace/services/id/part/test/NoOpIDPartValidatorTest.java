package org.collectionspace.services.id.part.test;

import org.collectionspace.services.id.part.NoOpIDPartValidator;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NoOpIDPartValidatorTest {

    NoOpIDPartValidator validator = new NoOpIDPartValidator();

    @Test
    public void isValid() {
        Assert.assertTrue(validator.isValid("any string"));
    }

    @Test(dependsOnMethods = {"isValid"})
    public void isValidWithNullAndEmptyValues() {
        Assert.assertTrue(validator.isValid(null));
        Assert.assertTrue(validator.isValid(""));
    }
}
