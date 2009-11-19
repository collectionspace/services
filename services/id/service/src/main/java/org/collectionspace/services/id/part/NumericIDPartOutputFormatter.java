package org.collectionspace.services.id.part;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// For symbols used in format patterns, see:
// http://java.sun.com/docs/books/tutorial/i18n/format/decimalFormat.html#numberpattern

public class NumericIDPartOutputFormatter implements IDPartOutputFormatter {

    final Logger logger =
        LoggerFactory.getLogger(NumericIDPartOutputFormatter.class);

    private int maxOutputLength = DEFAULT_MAX_OUTPUT_LENGTH;
    private String formatPattern;
    private NumberFormat numberFormat;

    public NumericIDPartOutputFormatter() {
        setNumberFormat(formatPattern);
    }

    public NumericIDPartOutputFormatter(String formatPattern) {
        setFormatPattern(formatPattern);
        setNumberFormat(formatPattern);
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

        Long l;
        try {
            l = (Long.parseLong(id));
        } catch (NumberFormatException e) {
            logger.error("Could not parse id '" + id + "' as a number.", e);
            return id;
        }

        // Format the number using the specified format pattern.
        String formattedID = formatLong(l);

        if (! isValidLength(formattedID)) {
            logger.error(
                "Formatted ID '" + formattedID +
                "' exceeds maximum length of " +
                getMaxOutputLength() + " characters." +
                "Returning ID without formatting.");
            return id;
        }

        return formattedID;
    }

    public String formatLong(Long l) {
        NumberFormat nf = getNumberFormat();
        return nf.format(l);
    }

    public long parseAsLong(String id) {
        Long l = 0L;
        try {
            Number n = getNumberFormat().parse(id);
            l = n.longValue();
        } catch (ParseException e) {
            // @TODO Handle this exception
        }
        return l;
    }

    private NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    private void setNumberFormat(String pattern) {

        NumberFormat nf = new DecimalFormat();

        // If there is no pattern specified, use a general purpose pattern.
        if (pattern == null || pattern.trim().isEmpty()) {
            nf = NumberFormat.getIntegerInstance();
            // In this general purpose pattern, turn off grouping
            // of numbers via grouping separators (such as "," or ".").
            nf.setGroupingUsed(false);
        // Otherwise, use the specified pattern.
        } else {
             if (nf instanceof DecimalFormat) {
                 try {
                      ((DecimalFormat) nf).applyPattern(pattern);
                 } catch (IllegalArgumentException e) {
                    // @TODO Handle this exception;
                 }
            }

        }

        this.numberFormat = nf;
    }

    private boolean isValidLength(String id) {
        if (id.length() <= getMaxOutputLength()) {
            return true;
        } else {
            return false;
        }
    }

 
}

