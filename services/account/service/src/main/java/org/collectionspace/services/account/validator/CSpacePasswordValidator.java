package org.collectionspace.services.account.validator;

import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.config.tenant.TenantBindingType;

/**
 * @since 9.0.0
 */
public class CSpacePasswordValidator {
    private CSpacePasswordValidator() {}

    /**
     * Validate a password for a given tenant.
     * @param binding The tenant binding which contains the password requirements
     * @param password The password being validated
     * @return The aggregate result from the validators created using the tenant binding
     */
    public static ValidationResult validatePasswordForTenant(final TenantBindingType binding, final String password) {
        final var validators = initializeForTenant(binding);
        return validators.stream()
            .map(validator -> validator.validate(password))
            .reduce(new ValidationResult(), ValidationResult::merge);
    }

    private static List<PasswordValidator> initializeForTenant(final TenantBindingType binding) {
        final var config = binding.getPasswordRequirementConfig();

        final var validators = new ArrayList<PasswordValidator>();
        validators.add(new MaxLengthValidator());
        validators.add(new MinLengthValidator(config));
        if (config != null && config.isEnabled()) {
            if (config.isRequireLowerCase()) {
                validators.add(RegexValidator.LOWER_CASE_VALIDATOR);
            }

            if (config.isRequireUpperCase()) {
                validators.add(RegexValidator.UPPER_CASE_VALIDATOR);
            }

            if (config.isRequireDigit()) {
                validators.add(RegexValidator.DIGIT_CASE_VALIDATOR);
            }

            if (config.isRequireSpecial()) {
                validators.add(RegexValidator.SPECIAL_CASE_VALIDATOR);
            }
        }

        return validators;
    }

}
