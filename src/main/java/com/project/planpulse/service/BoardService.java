package com.project.planpulse.service;

import com.project.planpulse.model.Board;
import com.project.planpulse.model.Task;
import com.project.planpulse.model.User;
import com.project.planpulse.repository.BoardRepository;
import com.project.planpulse.repository.TaskRepository;
import com.project.planpulse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    public List<Board> getBoardsForUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        // fetch boards where the user is the creator
        List<Board> createdBoards = boardRepository.findByCreatorId(userId);
        // fetch boards where the user is a collaborator
        List<Board> collaboratorBoards = boardRepository.findByCollaboratorIdsContaining(userId);
        // combining the two lists
        Set<Board> allBoards = new HashSet<>(createdBoards);
        allBoards.addAll(collaboratorBoards);
        return new ArrayList<>(allBoards);
    }

    public Board createBoard(Board board, String userId) {
        board.setCreatedAt(new Date());
        board.setUpdatedAt(new Date());
        board.setCreatorId(userId);
        board.setCollaboratorIds(new ArrayList<>());
        board.setTaskIds(new ArrayList<>());
        board = boardRepository.save(board);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getBoardIds() == null) user.setBoardIds(new ArrayList<>());
        user.getBoardIds().add(board.getId());
        userRepository.save(user);
        return board;
    }

    public Board getBoardById(String boardId, String requesterId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new RuntimeException("Board not found"));
        validateAddPermission(board, requesterId);
        return board;
    }

    // add a collaborator by email or username
    public Board addCollaborator(String requesterId, String boardId, String identifier) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new RuntimeException("Board not found"));
        validateAddPermission(board, requesterId);
        User user;
        if (isEmail(identifier)) {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("Invalid user credentials"));
        } else {
            user = userRepository.findByUsername(identifier)
                    .orElseThrow(() -> new RuntimeException("Invalid user credentials"));
        }

        if (board.getCollaboratorIds() == null) {
            board.setCollaboratorIds(new ArrayList<>());
        }
        if (!board.getCollaboratorIds().contains(user.getId()) && !user.getId().equals(requesterId)) {
            board.getCollaboratorIds().add(user.getId());
            board.setUpdatedAt(new Date());
            board = boardRepository.save(board);
            if (user.getBoardIds() == null) user.setBoardIds(new ArrayList<>());
            user.getBoardIds().add(board.getId());
            userRepository.save(user);
            return board;
        } else {
            throw new RuntimeException("The user already has access to the board.");
        }
    }

    // add a new task to a board
    public Task addTaskToBoard(String boardId, Task newTask, String requesterId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        // requester must have permission to the board
        validateAddPermission(board, requesterId);

        newTask.setBoardId(boardId);
        newTask.setReporterId(requesterId);
        newTask.setCreatedAt(new Date());
        newTask.setUpdatedAt(new Date());
        Task savedTask = taskRepository.save(newTask);

        // adding the task ID to the board's taskIds
        if (board.getTaskIds() == null) board.setTaskIds(new ArrayList<>());
        board.getTaskIds().add(savedTask.getId());
        board.setUpdatedAt(new Date());
        boardRepository.save(board);

        return savedTask;
    }

    public List<String> getCollaboratorUsernames(String boardId, String requesterId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new RuntimeException("No such board"));
        List<String> collaboratorIds = board.getCollaboratorIds();
        if (collaboratorIds == null || collaboratorIds.isEmpty()) {
            return new ArrayList<>();
        }
        validateAddPermission(board, requesterId);
        return collaboratorIds.stream()
                .map(
                        userId -> userRepository.findById(userId)
                                .map(User::getUsername)
                                .orElse(null) // for cases where a user might be deleted
                )
                .filter(Objects::nonNull) // filter out null values
                .collect(Collectors.toList());
    }

    private void validateAddPermission(Board board, String requesterId) {
        if (!board.getCreatorId().equals(requesterId) &&
                (board.getCollaboratorIds() == null || !board.getCollaboratorIds().contains(requesterId))) {
            throw new RuntimeException("Permission denied: The user does not have access to add to this board.");
        }
    }

    private boolean isEmail(String identifier) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return identifier.matches(emailRegex);
    }
}
