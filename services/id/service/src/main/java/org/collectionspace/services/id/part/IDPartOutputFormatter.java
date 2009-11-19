package org.collectionspace.services.id.part;

public interface IDPartOutputFormatter {

    public static int DEFAULT_MAX_OUTPUT_LENGTH = Integer.MAX_VALUE;

    public int getMaxOutputLength();

    public void setMaxOutputLength(int length);

    public String getFormatPattern();

    public void setFormatPattern(String pattern);

    // @TODO Consider throwing IllegalStateException from this method.
    public String format(String id);

}

