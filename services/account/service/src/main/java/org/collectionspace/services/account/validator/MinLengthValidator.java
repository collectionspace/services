package org.collectionspace.services.account.validator;

/**
 * Validator that checks the length of a password is greater than a given minimum length
 * @since 9.0.0
 */
public class MinLengthValidator implements PasswordValidator {

    private final int minLength;

    public MinLengthValidator(final int minLength) {
        this.minLength = minLength;
    }

    @Override
    public ValidationResult validate(final String password) {
        if (password.length() < minLength) {
            return new ValidationResult(ValidationErrorCode.ERR_TOO_SHORT);
        }

        return new ValidationResult();
    }
}
