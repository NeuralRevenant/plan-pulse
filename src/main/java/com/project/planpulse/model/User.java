package com.project.planpulse.model;

import com.project.planpulse.validation.ValidPassword;
import com.project.planpulse.validation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @NotBlank(message = "Firstname is required")
    private String firstname;

    @NotBlank(message = "Lastname is required")
    private String lastname;

    @Indexed(unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Indexed(unique = true)
    @NotBlank(message = "Username is required")
    @ValidUsername
    private String username;

    @NotBlank(message = "Password is required")
    private String passwordHash;

    // the profile-image URL stored in the user document
    private String profileImageUrl;

    @Indexed
    private List<String> boardIds = new ArrayList<>();
}
