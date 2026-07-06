package org.collectionspace.services.account.validator;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a Password Validation. Success is inferred by checking that the errors tracked are empty. Multiple results
 * can be merged to form an aggregation of multiple validators (non-deduplicated).
 *
 * See {@link CSpacePasswordValidator} for usage and how multiple results are
 * aggregated.
 * @since 9.0.0
 */
public final class ValidationResult {
    private final List<ValidationErrorCode> errors = new ArrayList<>();

    public ValidationResult() {
    }

    public ValidationResult(final ValidationErrorCode errorCode) {
        errors.add(errorCode);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public ValidationResult merge(final ValidationResult rhs) {
        errors().addAll(rhs.errors());
        return this;
    }

    public List<ValidationErrorCode> errors() {
        return errors;
    }

}
