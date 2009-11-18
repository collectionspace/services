package org.collectionspace.services.id.part;

public interface IDPartOutputFormatter {

    public static int DEFAULT_MAX_OUTPUT_LENGTH = Integer.MAX_VALUE;

    public int getMaxOutputLength();

    public String getFormatPattern();

    public String format(String id);

}

