package org.collectionspace.services.id.part.test;

import org.collectionspace.services.id.part.SequenceIDPart;
import org.collectionspace.services.id.part.NumericSequenceIDPart;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumericSequenceIDPartTest {

    final Logger logger =
        LoggerFactory.getLogger(NumericSequenceIDPartTest.class);

    SequenceIDPart part;
    String id;

    @Test
    public void newIDWithDefaultInitialValue() {
        part = new NumericSequenceIDPart();
        id = part.newID();
        Assert.assertEquals(id, "1");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "2");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "3");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "4");
    }

    @Test
    public void newIDWithSuppliedInitialValue() {
         part = new NumericSequenceIDPart(100);
        id = part.newID();
        Assert.assertEquals(id, "100");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "101");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "102");
    }

    @Test
    public void newIDWithIncrementByValue() {
        part = new NumericSequenceIDPart(5,5);
        id = part.newID();
        Assert.assertEquals(id, "5");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "10");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "15");
    }

    @Test
    public void format() {
    }

    @Test
    public void isValid() {
        part = new NumericSequenceIDPart();
        Assert.assertTrue(part.getValidator().isValid(part.newID()));
    }

}
