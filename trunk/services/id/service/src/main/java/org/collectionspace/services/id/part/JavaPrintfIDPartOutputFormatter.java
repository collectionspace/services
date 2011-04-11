package org.collectionspace.services.id.part;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.IllegalFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Uses Java's interpreter for printf-style format strings.
// http://java.sun.com/javase/1.6/docs/api/java/util/Formatter.html

public class JavaPrintfIDPartOutputFormatter implements IDPartOutputFormatter {

    final Logger logger =
        LoggerFactory.getLogger(JavaPrintfIDPartOutputFormatter.class);

    StringWriter stringwriter = new StringWriter();
    private int maxOutputLength = DEFAULT_MAX_OUTPUT_LENGTH;
    private String formatPattern;

    public JavaPrintfIDPartOutputFormatter() {
    }

    public JavaPrintfIDPartOutputFormatter(String formatPattern) {
        setFormatPattern(formatPattern);
    }

    @Override
    public int getMaxOutputLength() {
        return this.maxOutputLength;
    }

    @Override
    public void setMaxOutputLength(int length) {
        this.maxOutputLength = length;
    }

    @Override
    public String getFormatPattern() {
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

        String formattedID = id;
        String pattern = getFormatPattern();

        // If the formatting pattern is empty, just check length.
        if (pattern == null || pattern.trim().isEmpty()) {

            if (! isValidLength(formattedID)) {
                logger.error(
                    "Formatted ID '" + formattedID +
                    "' exceeds maximum length of " +
                    getMaxOutputLength() + " characters." +
                    "Returning ID without formatting.");
                return id;
            }

        // Otherwise, format the ID using the pattern, then check length.
        } else {

            // Clear the StringWriter's buffer from its last usage. 
            StringBuffer buf = stringwriter.getBuffer();
            buf.setLength(0);
            // Apply the formatting pattern to the ID.
            try {
                PrintWriter printwriter = new PrintWriter(stringwriter);
                printwriter.printf(id, pattern);
                formattedID = stringwriter.toString();
            } catch(IllegalFormatException e) {
                logger.error("Error when attempting to format ID '" +
                    id + "' using formatting pattern '" + pattern);
                // @TODO Consider throwing this exception.
            }

            if (! isValidLength(formattedID)) {
                logger.error(
                    "Formatted ID '" + formattedID +
                    "' exceeds maximum length of " +
                    getMaxOutputLength() + " characters." +
                    "Returning ID without formatting.");
                return id;
            }
        }

        return formattedID;
    }

    // Check whether the formatted ID exceeds the specified maximum length.

    private boolean isValidLength(String id) {
        if (id.length() <= getMaxOutputLength()) {
            return true;
        } else {
            return false;
        }
    }

}

