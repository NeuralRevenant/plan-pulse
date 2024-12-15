package com.project.planpulse.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    private String id;

    @Indexed(unique = true)
    private String token; // unique reset token

    @Indexed(unique = true)
    private String userId;

    private Date createdAt = new Date();
    private Date expiresAt;

    public PasswordResetToken(String userId, long ttlMinutes) {
        this.userId = userId;
        this.token = UUID.randomUUID().toString();
        this.expiresAt = new Date(System.currentTimeMillis() + ttlMinutes * 60000);
    }

    public boolean isExpired() {
        return new Date().after(this.expiresAt);
    }
}
