package org.collectionspace.services.account.validator;

import static org.collectionspace.services.account.validator.CSpacePasswordValidator.validatePasswordForTenant;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.collectionspace.services.config.tenant.PasswordRequirementConfig;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.testng.annotations.Test;

public class CSpacePasswordValidatorTest {

    private static final String SHORT_PASS = "few";
    private static final String LONG_PASS = "AStringWithLength>10";
    private static final String LOWER_CASE_PASS = "alowercasepassword";
    private static final String UPPER_CASE_PASS = "ANUPPERCASEPASSWORD";
    private static final String DIGIT_PASS = "0123456789";
    private static final String SPECIAL_PASS = "another!";

    @Test
    public void testNoConfig() {
        final var binding = genBinding(false);
        assertTrue(validatePasswordForTenant(binding, SHORT_PASS).isValid());
        assertTrue(validatePasswordForTenant(binding, LOWER_CASE_PASS).isValid());
        assertTrue(validatePasswordForTenant(binding, UPPER_CASE_PASS).isValid());
        assertTrue(validatePasswordForTenant(binding, DIGIT_PASS).isValid());
        assertTrue(validatePasswordForTenant(binding, SPECIAL_PASS).isValid());
    }

    @Test
    public void testMinLength() {
        final var binding = genBinding(true);
        binding.getPasswordRequirementConfig().setMinLength(10);

        assertTrue(validatePasswordForTenant(binding, LONG_PASS).isValid());
        runInvalidCheck(SHORT_PASS, List.of(ValidationErrorCode.ERR_TOO_SHORT), binding);
    }

    @Test
    public void testLowerCase() {
        final var binding = genBinding(true);
        binding.getPasswordRequirementConfig().setRequireLowerCase(true);

        assertTrue(validatePasswordForTenant(binding, LOWER_CASE_PASS).isValid());

        runInvalidCheck(UPPER_CASE_PASS, List.of(ValidationErrorCode.ERR_MISSING_LOWERCASE), binding);
        runInvalidCheck(DIGIT_PASS, List.of(ValidationErrorCode.ERR_MISSING_LOWERCASE), binding);
    }

    @Test
    public void testUpperCase() {
        final var binding = genBinding(true);
        binding.getPasswordRequirementConfig().setRequireUpperCase(true);

        assertTrue(validatePasswordForTenant(binding, UPPER_CASE_PASS).isValid());

        runInvalidCheck(LOWER_CASE_PASS, List.of(ValidationErrorCode.ERR_MISSING_UPPERCASE), binding);
        runInvalidCheck(DIGIT_PASS, List.of(ValidationErrorCode.ERR_MISSING_UPPERCASE), binding);
        runInvalidCheck(SPECIAL_PASS, List.of(ValidationErrorCode.ERR_MISSING_UPPERCASE), binding);
    }

    @Test
    public void testDigits() {
        final var binding = genBinding(true);
        binding.getPasswordRequirementConfig().setRequireDigit(true);

        assertTrue(validatePasswordForTenant(binding, DIGIT_PASS).isValid());

        runInvalidCheck(LOWER_CASE_PASS, List.of(ValidationErrorCode.ERR_MISSING_DIGIT), binding);
        runInvalidCheck(UPPER_CASE_PASS, List.of(ValidationErrorCode.ERR_MISSING_DIGIT), binding);
        runInvalidCheck(SPECIAL_PASS, List.of(ValidationErrorCode.ERR_MISSING_DIGIT), binding);
    }

    @Test
    public void testSpecialChars() {
        final var binding = genBinding(true);
        binding.getPasswordRequirementConfig().setRequireSpecial(true);

        assertTrue(validatePasswordForTenant(binding, SPECIAL_PASS).isValid());

        runInvalidCheck(LOWER_CASE_PASS, List.of(ValidationErrorCode.ERR_MISSING_SPECIAL), binding);
        runInvalidCheck(UPPER_CASE_PASS, List.of(ValidationErrorCode.ERR_MISSING_SPECIAL), binding);
        runInvalidCheck(DIGIT_PASS, List.of(ValidationErrorCode.ERR_MISSING_SPECIAL), binding);
    }

    @Test
    public void testMultipleConstraints() {
        final var binding = genBinding(true);
        binding.getPasswordRequirementConfig().setRequireLowerCase(true);
        binding.getPasswordRequirementConfig().setRequireSpecial(true);

        assertTrue(validatePasswordForTenant(binding, SPECIAL_PASS).isValid());

        final var expected = List.of(ValidationErrorCode.ERR_MISSING_LOWERCASE, ValidationErrorCode.ERR_MISSING_SPECIAL);
        runInvalidCheck(UPPER_CASE_PASS, expected, binding);
        runInvalidCheck(DIGIT_PASS, expected, binding);
    }

    private void runInvalidCheck(final String invalidPassword,
                                 final List<ValidationErrorCode> errorCodes,
                                 final TenantBindingType binding) {
        final var invalidResult = validatePasswordForTenant(binding, invalidPassword);
        assertFalse(invalidResult.isValid());
        assertTrue(invalidResult.errors().containsAll(errorCodes));
    }

    private TenantBindingType genBinding(final boolean enabled) {
       final var complexityConfig = new PasswordRequirementConfig();
       complexityConfig.setEnabled(enabled);
       final var binding = new TenantBindingType();
       binding.setPasswordRequirementConfig(complexityConfig);
       return binding;
    }
}