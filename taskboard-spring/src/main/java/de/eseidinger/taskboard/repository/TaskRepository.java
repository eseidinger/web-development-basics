package de.eseidinger.taskboard.repository;

import de.eseidinger.taskboard.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    @Query("select t from Task t join BoardMembership bm on bm.board.id = t.board.id where bm.user.id = :userId and t.assignee.id = :userId and t.archivedAt is null order by t.createdAt asc")
    List<Task> findAssignedVisibleTasks(UUID userId);

    List<Task> findAllByColumnId(UUID columnId);

    long countByColumnId(UUID columnId);
}

