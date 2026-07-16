package org.collectionspace.services.account.validator;

import java.nio.charset.StandardCharsets;

/**
 * Max Length validation check to ensure that the password is not greater than 72 bytes to conform with bcrypt.
 *
 * @since 9.0.0
 */
public class MaxLengthValidator implements PasswordValidator {
    public static final int MAX_LENGTH = 72;

    @Override
    public ValidationResult validate(final String password) {
        if (password != null && password.getBytes(StandardCharsets.UTF_8).length > MAX_LENGTH) {
            return new ValidationResult(ValidationErrorCode.ERR_TOO_LONG);
        }

        return new ValidationResult();
    }
}
