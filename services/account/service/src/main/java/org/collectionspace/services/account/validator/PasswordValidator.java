package org.collectionspace.services.account.validator;

/**
 * @since 9.0.0
 */
@FunctionalInterface
public interface PasswordValidator {
    ValidationResult validate(String password);
}
