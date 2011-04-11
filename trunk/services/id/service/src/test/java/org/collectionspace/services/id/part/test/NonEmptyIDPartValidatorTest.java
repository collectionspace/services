package org.collectionspace.services.id.part.test;

import org.collectionspace.services.id.part.NonEmptyIDPartValidator;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NonEmptyIDPartValidatorTest {

    NonEmptyIDPartValidator validator = new NonEmptyIDPartValidator();

    @Test
    public void isValid() {
        Assert.assertTrue(validator.isValid("any string"));
    }

    @Test(dependsOnMethods = {"isValid"})
    public void isValidWithInvalidValues() {
        Assert.assertFalse(validator.isValid(null));
        Assert.assertFalse(validator.isValid(""));
    }

}
