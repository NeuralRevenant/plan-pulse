package com.project.planpulse.service;

import com.project.planpulse.model.Board;
import com.project.planpulse.model.Task;
import com.project.planpulse.repository.BoardRepository;
import com.project.planpulse.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BoardRepository boardRepository;

    // create a new task
    public Task createTask(Task task) {
        task.setCreatedAt(new Date());
        task.setUpdatedAt(new Date());
        task.setStatus("TO_DO");
        return taskRepository.save(task);
    }

    public Task getTaskById(String taskId, String requesterId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        Board board = boardRepository.findById(task.getBoardId()).orElseThrow(() -> new RuntimeException("Invalid Details"));
        validatePermission(board, requesterId);
        return task;
    }

    // retrieve all tasks for a given board
    public List<Task> getTasksByBoard(String boardId, String requesterId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new RuntimeException("Invalid board details"));
        validatePermission(board, requesterId);
        return taskRepository.findByBoardId(boardId);
    }

    // update the task's status
    public Task updateTaskStatus(String taskId, String status, String requesterId) {
        if (requesterId == null || requesterId.isBlank() || status == null || status.isBlank() || taskId == null || taskId.isBlank()) {
            throw new RuntimeException("Invalid credentials");
        }
        Task task = getTaskById(taskId, requesterId);
        if (isInvalidStatus(status)) {
            throw new RuntimeException("Invalid status transition");
        }
        task.setStatus(status);
        task.setUpdatedAt(new Date());
        return taskRepository.save(task);
    }

    private void validatePermission(Board board, String requesterId) {
        if (!board.getCreatorId().equals(requesterId) &&
                (board.getCollaboratorIds() == null || !board.getCollaboratorIds().contains(requesterId))) {
            throw new RuntimeException("Permission denied: The user does not have access to add to this board.");
        }
    }

    // Track time spent on a task
    public Task trackTime(String taskId, long minutes, String requesterId) {
        Task task = getTaskById(taskId, requesterId);
        task.setTimeSpent(task.getTimeSpent() + minutes);
        task.setUpdatedAt(new Date());
        return taskRepository.save(task);
    }

    private boolean isInvalidStatus(String newStatus) {
        return !(Set.of("TO_DO", "IN_PROGRESS", "IN_REVIEW", "DONE").contains(newStatus));
    }
}
