package org.collectionspace.services.id.part;

public class NoOpIDPartOutputFormatter implements IDPartOutputFormatter {

    public NoOpIDPartOutputFormatter () {
    }

    @Override
    public int getMaxOutputLength () {
        return Integer.MAX_VALUE;
    }

    public void setMaxOutputLength (int length) {
        // Do nothing.
    }

    @Override
    public String getFormatPattern () {
        return "";
    }

    public void setFormatPattern(String pattern) {
        // Do nothing.
    }

    @Override
    public String format(String id) {
        return id;
    }


}

