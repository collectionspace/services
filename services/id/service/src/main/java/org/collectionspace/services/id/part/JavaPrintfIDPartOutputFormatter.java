package org.collectionspace.services.id.part;

import java.util.IllegalFormatException;
import java.io.PrintWriter;
import java.io.StringWriter;

// Uses Java's interpreter for printf-style format strings.
// http://java.sun.com/javase/1.6/docs/api/java/util/Formatter.html

public class JavaPrintfIDPartOutputFormatter implements IDPartOutputFormatter {

    StringWriter stringwriter = new StringWriter();
    private int maxOutputLength = DEFAULT_MAX_OUTPUT_LENGTH;
    private String formatPattern;

    public JavaPrintfIDPartOutputFormatter () {
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
        this.formatPattern = pattern;
    }

    @Override
    public String format(String id) {

        String formattedID = id;

        String pattern = getFormatPattern();

        // If the formatting pattern is empty, just check length.
        if (pattern == null || pattern.trim().isEmpty()) {
            isValidLength(formattedID);

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
                // @TODO Log and handle this exception.
            }
            isValidLength(formattedID);
        }

        return formattedID;
    }

    // Check whether the formatted ID exceeds the specified maximum length.

    private void isValidLength(String id) {
        if (id.length() > getMaxOutputLength()) {
            // @TODO Log error, possibly throw exception.
        }

    }

}

