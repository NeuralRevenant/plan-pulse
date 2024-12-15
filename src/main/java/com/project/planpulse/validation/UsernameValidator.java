package com.project.planpulse.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    private static final String USERNAME_REGEX = "^[A-Za-z0-9._-]{3,30}$";

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return username.matches(USERNAME_REGEX);
    }
}
