package org.collectionspace.services.id.part;

public class NoOpIDPartOutputFormatter implements IDPartOutputFormatter {

    public NoOpIDPartOutputFormatter() {
    }

    public NoOpIDPartOutputFormatter(String formatPattern) {
        // Do nothing.
    }

    @Override
    public int getMaxOutputLength() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setMaxOutputLength(int length) {
        // Do nothing.
    }

    @Override
    public String getFormatPattern() {
        return "";
    }

    @Override
    public void setFormatPattern(String pattern) {
        // Do nothing.
    }

    @Override
    public String format(String id) {
        return id;
    }


}

