package org.collectionspace.services.id.part.test;

import org.collectionspace.services.id.part.NoOpIDPartValidator;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NoOpIDPartValidatorTest {

    @Test
    public void isValid() {
        NoOpIDPartValidator validator = new NoOpIDPartValidator();
        Assert.assertTrue(validator.isValid(null));
        Assert.assertTrue(validator.isValid(""));
        Assert.assertTrue(validator.isValid("any string"));
    }

}
