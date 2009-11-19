package org.collectionspace.services.id.part.test;

import org.collectionspace.services.id.part.NumericIDPartRegexValidator;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NumericIDPartRegexValidatorTest {

    NumericIDPartRegexValidator validator = new NumericIDPartRegexValidator();

    @Test
    public void isValid() {
        Assert.assertTrue(validator.isValid("0"));
        Assert.assertTrue(validator.isValid("5"));
        Assert.assertTrue(validator.isValid("123456789012345"));
    }

    @Test(dependsOnMethods = {"isValid"})
    public void isValidWithInvalidValues() {
        Assert.assertFalse(validator.isValid(null));
        Assert.assertFalse(validator.isValid(""));
        Assert.assertFalse(validator.isValid("non-numeric value"));
        Assert.assertFalse(validator.isValid("-1"));
    }

    @Test
    public void format() {

    }

}
