package org.collectionspace.services.id.part.test;

import org.collectionspace.services.id.part.GregorianDateIDPart;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GregorianDateIDPartTest {

    final Logger logger =
        LoggerFactory.getLogger(GregorianDateIDPartTest.class);

    GregorianDateIDPart part;

    @Test
    public void newID() {

        // @TODO Replace these hard-coded expedients, which will all fail
        // when the current month or year doesn't match these asserted values.
        
        part = new GregorianDateIDPart("yyyy");
        Assert.assertEquals(part.newID(), "2009");

        part = new GregorianDateIDPart("M");
        Assert.assertEquals(part.newID(), "11");

        part = new GregorianDateIDPart("MMMM");
        Assert.assertEquals(part.newID(), "November");

        part = new GregorianDateIDPart("MMMM", "fr");
        // Month names are not capitalized in French.
        Assert.assertEquals(part.newID(), "novembre");
        
    }


    @Test
    public void format() {
    }

    @Test
    public void isValid() {
        part = new GregorianDateIDPart("yyyy");
        Assert.assertTrue(part.getValidator().isValid(part.newID()));
    }

}
