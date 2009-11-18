package org.collectionspace.services.id.part.test;

import org.collectionspace.services.id.part.IDPart;
import org.collectionspace.services.id.part.UUIDPart;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class UUIDPartTest {
    
    IDPart part;
    String firstID;
    String secondID;
    String thirdID;

    @BeforeTest
    public void setUp() {
        part = new UUIDPart();
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

    @Test(dependsOnMethods = {"isValid"})
    public void isValidWithInvalidValues() {
        Assert.assertFalse(part.getValidator().isValid(null));
        Assert.assertFalse(part.getValidator().isValid(""));
        Assert.assertFalse(part.getValidator().isValid("not a UUID"));
        Assert.assertFalse(part.getValidator().isValid("12345"));
        // Invalid character in 15th position (should be '4').
        Assert.assertFalse(part.getValidator().isValid(
            "4c9395a8-1669-31f9-806c-920d86e40912"));
        // Invalid character in 20th position
        // (should be '8', '9', 'a', or 'b').
        Assert.assertFalse(part.getValidator().isValid(
            "4c9395a8-1669-41f9-106c-920d86e40912"));
    }

}
