package com.project.planpulse.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@Document(collection = "tasks")
public class Task {
    @Id
    private String id;
    @NotBlank(message = "Title is required")
    private String title;
    @Indexed
    private String boardId;
    private String description;
    private String reporterId;
    private String assigneeId;
    private String priority = "LOW"; // LOW, MEDIUM, HIGH
    private long timeSpent = 0; // stored in minutes
    private Date deadline;
    private String status = "TO_DO"; // TO_DO, IN_PROGRESS, IN_REVIEW, DONE
    private Date createdAt = new Date();
    private Date updatedAt = new Date();
}
