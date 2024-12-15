package com.project.planpulse.repository;

import com.project.planpulse.model.Board;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends MongoRepository<Board, String> {
    // Fetch boards created by the user
    List<Board> findByCreatorId(String creatorId);

    // Fetch boards where the user is a collaborator
    List<Board> findByCollaboratorIdsContaining(String userId);
}
