package org.collectionspace.services.id.part;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Uses Java's interpreter for printf-style format strings.
// http://java.sun.com/javase/1.6/docs/api/java/util/Formatter.html

public class GregorianDateIDPartOutputFormatter implements IDPartOutputFormatter {

    final Logger logger =
        LoggerFactory.getLogger(GregorianDateIDPartOutputFormatter.class);

    private int maxOutputLength = DEFAULT_MAX_OUTPUT_LENGTH;
    private String formatPattern;
    private final String DEFAULT_ISO_8601_FORMAT_PATTERN =
        "yyyy-MM-dd"; // Note: 'floating date' without time zone.
    private final String DEFAULT_GREGORIAN_DATE_FORMAT_PATTERN =
        DEFAULT_ISO_8601_FORMAT_PATTERN;
    private Locale locale = null;
    
    public GregorianDateIDPartOutputFormatter() {
        setFormatPattern(DEFAULT_GREGORIAN_DATE_FORMAT_PATTERN);
        setLocale(Locale.getDefault());
    }

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

    @Override
    public void setMaxOutputLength (int length) {
        this.maxOutputLength = length;
    }

    @Override
    public String getFormatPattern () {
        return this.formatPattern;
    }

    @Override
    public void setFormatPattern(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            logger.error("Format pattern cannot be null or empty.");
        } else {
            this.formatPattern = pattern;
        }
    }

    @Override
    public String format(String id) {

        String pattern = getFormatPattern();
        // If the formatting pattern is empty, output using
        // a default formatting pattern.
        if (pattern == null || pattern.trim().isEmpty()) {
            pattern = DEFAULT_GREGORIAN_DATE_FORMAT_PATTERN;
        }

        // Convert milliseconds in Epoch to Date.
        Long millisecondsInEpoch = 0L;
        try {
            millisecondsInEpoch = (Long.parseLong(id));
        } catch (NumberFormatException e) {
            logger.error("Could not parse date milliseconds as a number.", e);
            return "";
        }

        if (millisecondsInEpoch <= 0) {
            return "";

        }

        // Format the date using the specified format pattern.
        Date d = new Date(millisecondsInEpoch);
        String formattedID = formatDate(d);

        if (! isValidLength(formattedID)) {
            logger.error(
                "Formatted ID '" + formattedID +
                "' exceeds maximum length of " +
                getMaxOutputLength() + " characters.");
            return "";
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

    private boolean isValidLength(String id) {
        if (id.length() <= getMaxOutputLength()) {
            return true;
        } else {
            return false;
        }
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
                "' must be a two-letter ISO 639-1 language code.");
            return;
        }

        // Although it is documented in Sun's Javadocs that
        // the language code parameter when initializing a
        // Locale is required to be in lowercase, and as well,
        // language codes are output only in lowercase in
        // DateFormat.getAvailableLocales(), they appear to be
        // matched - within Locales - in a case-insensitive manner.
        // If that ever changes, uncomment the following block.
        //
        // if (! languageCode.equals(languageCode.toLowerCase())) {
        //    logger.error("Locale language code must be in lower case.");
        //    return;
        // }

        Locale l = new Locale(languageCode, "");
        if (isValidLocaleForFormatter(l)) {
            setLocale(l);
        } else {
            logger.error("Locale language code '" + languageCode +
                "' cannot be used for formatting dates.");
            return;
        }
    }
    
    private boolean isValidLocaleForFormatter(Locale l) {
        Locale[] locales = DateFormat.getAvailableLocales();
        return (Arrays.asList(locales).contains(l)) ? true : false;
    }

}

