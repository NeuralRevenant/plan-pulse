package com.project.planpulse.controller;

import com.project.planpulse.model.Task;
import com.project.planpulse.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;


    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable String id, Authentication authentication) {
        String requesterId = authentication.getName();
        return taskService.getTaskById(id, requesterId);
    }

    @GetMapping("/board/{boardId}")
    public List<Task> getTasksByBoard(@PathVariable String boardId, Authentication authentication) {
        String requesterId = authentication.getName();
        return taskService.getTasksByBoard(boardId, requesterId);
    }

    @PutMapping("/{taskId}/status")
    public Task updateTaskStatus(@PathVariable String taskId, @RequestBody String status, Authentication authentication) {
        String requesterId = authentication.getName();
        return taskService.updateTaskStatus(taskId, status, requesterId);
    }

    @PostMapping("/{taskId}/time")
    public Task trackTime(@PathVariable String taskId, @RequestBody long minutes, Authentication authentication) {
        String requesterId = authentication.getName();
        return taskService.trackTime(taskId, minutes, requesterId);
    }
}
