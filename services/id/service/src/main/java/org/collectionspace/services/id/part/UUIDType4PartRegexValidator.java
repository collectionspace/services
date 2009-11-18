package org.collectionspace.services.id.part;

public class UUIDType4PartRegexValidator extends IDPartRegexValidator {

    // @TODO The name of this class may be too generic, since
    // there may be other UUID generators with different algorithms.

    final static String REGEX_PATTERN =
        "(" +
        "[a-z0-9\\-]{8}" +
        "\\-" +
        "[a-z0-9\\-]{4}" +
        "\\-" +
        "4" +
        "[a-z0-9\\-]{3}" +
        "\\-" +
        "[89ab]" +
        "[a-z0-9\\-]{3}" +
        "\\-" +
        "[a-z0-9\\-]{12}" +
        ")";

    public UUIDType4PartRegexValidator() {
    }

    @Override
    public String getRegexPattern() {
        return REGEX_PATTERN;
    }

}
