package com.project.planpulse.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password must be at least 8 characters long, include an uppercase letter, a lowercase letter, a digit, and a special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
