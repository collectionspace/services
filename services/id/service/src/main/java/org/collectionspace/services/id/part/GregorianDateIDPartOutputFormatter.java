package org.collectionspace.services.id.part;

import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Uses Java's interpreter for printf-style format strings.
// http://java.sun.com/javase/1.6/docs/api/java/util/Formatter.html

public class GregorianDateIDPartOutputFormatter implements IDPartOutputFormatter {

    final Logger logger =
        LoggerFactory.getLogger(GregorianDateIDPartOutputFormatter.class);

    private int maxOutputLength = DEFAULT_MAX_OUTPUT_LENGTH;
    private Locale locale = null;
    private String language;
    private String formatPattern;

    public GregorianDateIDPartOutputFormatter(String formatPattern) {
        setFormatPattern(formatPattern);
        setLocale(Locale.getDefault());
    }

    public GregorianDateIDPartOutputFormatter(String formatPattern,
            String languageCode) {
        setFormatPattern(formatPattern);
        setLocale(languageCode);
    }

    @Override
    public int getMaxOutputLength () {
        return this.maxOutputLength;
    }

    public void setMaxOutputLength (int length) {
        this.maxOutputLength = length;
    }

    @Override
    public String getFormatPattern () {
        return this.formatPattern;
    }

    public void setFormatPattern(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            logger.error("Format pattern cannot be null or empty.");
        } else {
            this.formatPattern = pattern;
        }
    }

    @Override
    public String format(String id) {

        Long millisecondsInEpoch = 0L;
        try {
            millisecondsInEpoch = (Long.parseLong(id));
        } catch (NumberFormatException e) {
            logger.error("Could not parse date milliseconds as a number.", e);
            return "";
        }
        
        String formattedID = "";
        if (millisecondsInEpoch > 0) {
          Date d = new Date(millisecondsInEpoch);
          formattedID = formatDate(d);

          // @TODO Check for exceeding maximum length before
          // returning formatted date value.
        }

        return formattedID;
    }

    public String formatDate(Date date) {
        SimpleDateFormat dateformatter;
        if (getLocale() != null) {
            dateformatter = new SimpleDateFormat(getFormatPattern(), getLocale());
        } else {
            dateformatter = new SimpleDateFormat(getFormatPattern());
        }
        return dateformatter.format(date);
    }

    // @TODO Consider generalizing locale-specific operations
    // in a utility class outside of ID package.

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setLocale(String languageCode) {

        if (languageCode == null || languageCode.trim().isEmpty()) {
            logger.error("Locale language code cannot be null or empty.");
            return;
        }

        if (languageCode.length() != 2) {
            logger.error(
                "Locale language code '" + languageCode +
                "' must be a two-letter ISO-639 language code.");
            return;
        }

        // Although language codes are documented as required to be
        // in lowercase, and they are output in that way in
        // DateFormat.getAvailableLocales(), they appear to be
        // matched - within Locales - in a case-insensitive manner.
        /*
        if (! languageCode.equals(languageCode.toLowerCase())) {
            logger.error("Locale language code must be in lower case.");
            return;
        }
        */

        Locale l = new Locale(languageCode, "");
        if (isValidLocaleForDateFormatter(l)) {
            setLocale(l);
        } else {
            logger.error("Locale language code '" + languageCode +
                "' cannot be used for formatting dates.");
            return;
        }
    }
    
    private boolean isValidLocaleForDateFormatter(Locale l) {
        Locale[] locales = DateFormat.getAvailableLocales();
        return (Arrays.asList(locales).contains(l)) ? true : false;
    }

    private boolean isValidLength(String id) {
        if (id.length() > getMaxOutputLength()) {
            return false;
        } else {
            return true;
        }
    }

}

