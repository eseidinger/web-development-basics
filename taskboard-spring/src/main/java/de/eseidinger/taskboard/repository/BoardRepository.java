package de.eseidinger.taskboard.repository;

import de.eseidinger.taskboard.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    @Query("select b from BoardMembership bm join bm.board b where bm.user.id = :userId order by b.createdAt asc")
    List<Board> findAllVisibleToUser(UUID userId);

    @Query("select b from BoardMembership bm join bm.board b where bm.user.id = :userId and b.id = :boardId")
    Optional<Board> findVisibleToUser(UUID userId, UUID boardId);
}
