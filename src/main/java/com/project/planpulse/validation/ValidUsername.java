package com.project.planpulse.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUsername {
    String message() default "Invalid username. It must be 3-30 characters long and can only contain letters, numbers, dots, underscores, and hyphens.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
