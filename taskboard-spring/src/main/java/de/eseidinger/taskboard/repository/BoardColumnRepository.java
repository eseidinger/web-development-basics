package de.eseidinger.taskboard.repository;

import de.eseidinger.taskboard.domain.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, UUID> {

    List<BoardColumn> findAllByBoardIdOrderByPositionAsc(UUID boardId);

    Optional<BoardColumn> findByBoardIdAndId(UUID boardId, UUID columnId);

    long countByBoardId(UUID boardId);

    boolean existsByBoardIdAndId(UUID boardId, UUID columnId);

    @Query("select count(t) from Task t where t.column.id = :columnId")
    long countTasksByColumnId(UUID columnId);
}
