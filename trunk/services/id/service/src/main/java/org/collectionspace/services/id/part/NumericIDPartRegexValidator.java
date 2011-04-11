package org.collectionspace.services.id.part;

public class NumericIDPartRegexValidator extends IDPartRegexValidator {

    final static String REGEX_PATTERN = "(\\d+)";

    public NumericIDPartRegexValidator() {
    }

    @Override
    public String getRegexPattern() {
        return REGEX_PATTERN;
    }
}
