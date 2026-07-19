package de.eseidinger.taskboard.repository;

import de.eseidinger.taskboard.domain.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface LabelRepository extends JpaRepository<Label, UUID> {

    List<Label> findAllByBoardIdOrderByNameAsc(UUID boardId);

    Optional<Label> findByBoardIdAndId(UUID boardId, UUID labelId);

    List<Label> findAllByBoardIdAndNameIn(UUID boardId, Collection<String> names);

    Set<Label> findAllByBoardIdAndNameInOrderByNameAsc(UUID boardId, Collection<String> names);
}
