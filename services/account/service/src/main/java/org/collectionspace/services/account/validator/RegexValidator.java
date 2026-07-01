package org.collectionspace.services.account.validator;

import java.util.regex.Pattern;

/**
 * Validator that looks for a regular expression in a password.
 *
 * Four default validators are provided for:
 * - lower case ([a-z])
 * - upper case ([A-Z])
 * - digits (\d)
 * - special characters (Punct)
 * @since 9.0.0
 */
public class RegexValidator implements PasswordValidator {
    public static final RegexValidator LOWER_CASE_VALIDATOR =
        new RegexValidator(Pattern.compile("[a-z]"), ValidationErrorCode.ERR_MISSING_LOWERCASE);

    public static final RegexValidator UPPER_CASE_VALIDATOR =
        new RegexValidator(Pattern.compile("[A-Z]"), ValidationErrorCode.ERR_MISSING_UPPERCASE);

    public static final RegexValidator DIGIT_CASE_VALIDATOR =
        new RegexValidator(Pattern.compile("\\d"), ValidationErrorCode.ERR_MISSING_DIGIT);

    public static final RegexValidator SPECIAL_CASE_VALIDATOR =
        new RegexValidator(Pattern.compile("\\p{Punct}"), ValidationErrorCode.ERR_MISSING_SPECIAL);

    private final Pattern pattern;
    private final ValidationErrorCode errorCode;

    public RegexValidator(final Pattern pattern, final ValidationErrorCode errorCode) {
        this.pattern = pattern;
        this.errorCode = errorCode;
    }

    @Override
    public ValidationResult validate(final String password) {
        final var matcher = pattern.matcher(password);
        if (!matcher.find()) {
            return new ValidationResult(errorCode);
        }

        return new ValidationResult();
    }
}
