package org.collectionspace.services.account.validator;

import java.util.Optional;

import org.collectionspace.services.config.tenant.PasswordRequirementConfig;

/**
 * Validator that checks the length of a password is greater than a given minimum length
 * @since 9.0.0
 */
public class MinLengthValidator implements PasswordValidator {

    public static final int DEFAULT_MIN_LENGTH = 8;

    private final int minLength;

    public MinLengthValidator(final PasswordRequirementConfig config) {
        this.minLength = Optional.ofNullable(config)
            .filter(PasswordRequirementConfig::isEnabled)
            .map(PasswordRequirementConfig::getMinLength)
            .orElse(DEFAULT_MIN_LENGTH);
    }

    @Override
    public ValidationResult validate(final String password) {
        if (password.length() < minLength) {
            return new ValidationResult(ValidationErrorCode.ERR_TOO_SHORT);
        }

        return new ValidationResult();
    }
}
