package com.project.planpulse.repository;

import com.project.planpulse.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByBoardId(String boardId);

    List<Task> findByAssigneeId(String assigneeId);

    List<Task> findByStatus(String status);

    List<Task> findByDeadlineBefore(Date deadline);
}
