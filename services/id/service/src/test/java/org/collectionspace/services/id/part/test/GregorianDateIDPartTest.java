package org.collectionspace.services.id.part.test;

import org.collectionspace.services.id.part.GregorianDateIDPart;

import java.text.SimpleDateFormat;
import java.util.Calendar;
//import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GregorianDateIDPartTest {
	
	public GregorianDateIDPartTest(String inComingString) {
		System.err.println("GregorianDateIDPartTest constructor invoked!");
		//empty constructor
	}

    final Logger logger =
        LoggerFactory.getLogger(GregorianDateIDPartTest.class);

    GregorianDateIDPart part;

    final static String MONTH_FULL_NAME_PATTERN = "MMMM";
    final static String FRENCH_LANGUAGE_ISO_CODE = "fr";
    final static Locale FRENCH_LANGUAGE_LOCALE =
        new Locale(FRENCH_LANGUAGE_ISO_CODE, "");

    @Test
    public void newID() {
        
        part = new GregorianDateIDPart("yyyy");
        Assert.assertEquals(part.newID(), currentYearAsString());

        part = new GregorianDateIDPart("M");
        Assert.assertEquals(part.newID(), currentMonthNumberAsString());

        part = new GregorianDateIDPart(MONTH_FULL_NAME_PATTERN);
        Assert.assertEquals(part.newID(), currentMonthFullName());

        part = new GregorianDateIDPart(MONTH_FULL_NAME_PATTERN,
            FRENCH_LANGUAGE_ISO_CODE);
        Assert.assertEquals(part.newID(),
                currentMonthFullNameLocalized(FRENCH_LANGUAGE_LOCALE));
        
    }


    @Test
    public void format() {
    	//empty?
    }

    @Test
    public void isValid() {
        part = new GregorianDateIDPart("yyyy");
        Assert.assertTrue(part.getValidator().isValid(part.newID()));
    }

    public String currentYearAsString() {
        int y = GregorianCalendar.getInstance().get(Calendar.YEAR);
        return Integer.toString(y);
    }

    public String currentMonthNumberAsString() {
        // Calendar.MONTH numbers begin with 0; hence the need to add 1.
        int m = GregorianCalendar.getInstance().get(Calendar.MONTH) + 1;
        return Integer.toString(m);
    }

    public String currentMonthFullName() {
        SimpleDateFormat df =
            new SimpleDateFormat(MONTH_FULL_NAME_PATTERN,
                Locale.getDefault());
        return df.format(GregorianCalendar.getInstance().getTime());
    }

    public String currentMonthFullNameLocalized(Locale locale) {
        SimpleDateFormat df =
            new SimpleDateFormat(MONTH_FULL_NAME_PATTERN, locale);
        return df.format(GregorianCalendar.getInstance().getTime());
    }
}
