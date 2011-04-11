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

        part = new NumericSequenceIDPart(0);
        id = part.newID();
        Assert.assertEquals(id, "0");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "1");
        part.setCurrentID(id);

        part = new NumericSequenceIDPart(100);
        id = part.newID();
        Assert.assertEquals(id, "100");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "101");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "102");

        // Tests whether default formatting has disabled grouping.

        part = new NumericSequenceIDPart(12345);
        id = part.newID();
        Assert.assertEquals(id, "12345"); // No grouping separator; e.g. 12,345
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "12346");
        part.setCurrentID(id);

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
    public void formatWithZeroValue() {

        // With default format pattern.
        part = new NumericSequenceIDPart(0);
        id = part.newID();
        Assert.assertEquals(id, Long.toString(0));

        // With supplied format pattern.
        part = new NumericSequenceIDPart("#####", 0, 1);
        id = part.newID();
        Assert.assertEquals(id, Long.toString(0));
        part.setCurrentID(id);

    }

    @Test
    public void formatWithMaxPossibleValue() {

        // With default format pattern.
        part = new NumericSequenceIDPart(Long.MAX_VALUE);
        id = part.newID();
        Assert.assertEquals(id, Long.toString(Long.MAX_VALUE));
        part.setCurrentID(id);

        // With supplied format pattern.
        part = new NumericSequenceIDPart("#####", Long.MAX_VALUE, 1);
        id = part.newID();
        Assert.assertEquals(id, Long.toString(Long.MAX_VALUE));
        part.setCurrentID(id);

    }

    @Test
    public void formatWithLeadingZeros() {
        
        // Pad at left with leading zeros up to width specified.
        part = new NumericSequenceIDPart("000");
        id = part.newID();
        Assert.assertEquals(id, "001");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "002");
        part.setCurrentID(id);

        part = new NumericSequenceIDPart("000", 20, 5);
        id = part.newID();
        Assert.assertEquals(id, "020");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "025");
        part.setCurrentID(id);

        // With zero value.
        // Pad at left with leading zeros up to width specified.
        part = new NumericSequenceIDPart("000", 0, 1);
        id = part.newID();
        Assert.assertEquals(id, "000");
        part.setCurrentID(id);

        // Values containing more digits than supplied pattern
        // do not receive zero padding.
        part = new NumericSequenceIDPart("000", 5000, 1);
        id = part.newID();
        Assert.assertEquals(id, "5000");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "5001");
        part.setCurrentID(id);
    }

    @Test
    public void formatWithSeparators() {

        part = new NumericSequenceIDPart("#,###", 1234567, 1);
        id = part.newID();
        Assert.assertEquals(id, "1,234,567");
        part.setCurrentID(id);

        id = part.newID();
        Assert.assertEquals(id, "1,234,568");
        part.setCurrentID(id);
    }

    @Test
    public void isValid() {
        part = new NumericSequenceIDPart();
        Assert.assertTrue(part.getValidator().isValid(part.newID()));
    }

}
