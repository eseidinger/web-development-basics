package de.eseidinger.taskboard.repository;

import de.eseidinger.taskboard.domain.BoardMembership;
import de.eseidinger.taskboard.domain.BoardMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardMembershipRepository extends JpaRepository<BoardMembership, BoardMembershipId> {

    @Query("select bm from BoardMembership bm join fetch bm.board where bm.user.id = :userId order by bm.joinedAt asc")
    List<BoardMembership> findAllByUserId(UUID userId);

    @Query("select bm from BoardMembership bm join fetch bm.user where bm.board.id = :boardId order by bm.joinedAt asc")
    List<BoardMembership> findAllByBoardId(UUID boardId);

    Optional<BoardMembership> findByBoardIdAndUserId(UUID boardId, UUID userId);

    boolean existsByBoardIdAndUserId(UUID boardId, UUID userId);
}
