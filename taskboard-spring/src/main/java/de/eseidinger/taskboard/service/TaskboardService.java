package de.eseidinger.taskboard.service;

import de.eseidinger.taskboard.domain.AppUser;
import de.eseidinger.taskboard.domain.Board;
import de.eseidinger.taskboard.domain.BoardColumn;
import de.eseidinger.taskboard.domain.BoardMembership;
import de.eseidinger.taskboard.domain.BoardRole;
import de.eseidinger.taskboard.domain.Comment;
import de.eseidinger.taskboard.domain.Label;
import de.eseidinger.taskboard.domain.Task;
import de.eseidinger.taskboard.error.ApiException;
import de.eseidinger.taskboard.repository.AppUserRepository;
import de.eseidinger.taskboard.repository.BoardColumnRepository;
import de.eseidinger.taskboard.repository.BoardMembershipRepository;
import de.eseidinger.taskboard.repository.BoardRepository;
import de.eseidinger.taskboard.repository.CommentRepository;
import de.eseidinger.taskboard.repository.LabelRepository;
import de.eseidinger.taskboard.repository.TaskRepository;
import de.eseidinger.taskboard.security.CurrentUser;
import de.eseidinger.taskboard.security.CurrentUserService;
import de.eseidinger.taskboard.web.ApiDtos;
import jakarta.validation.Valid;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class TaskboardService {

    private final CurrentUserService currentUserService;
    private final AppUserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BoardMembershipRepository membershipRepository;
    private final BoardColumnRepository columnRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final LabelRepository labelRepository;

    public TaskboardService(CurrentUserService currentUserService,
                            AppUserRepository userRepository,
                            BoardRepository boardRepository,
                            BoardMembershipRepository membershipRepository,
                            BoardColumnRepository columnRepository,
                            TaskRepository taskRepository,
                            CommentRepository commentRepository,
                            LabelRepository labelRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.membershipRepository = membershipRepository;
        this.columnRepository = columnRepository;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.labelRepository = labelRepository;
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.BoardResponse> listBoards() {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        return membershipRepository.findAllByUserId(currentUser.user().getId()).stream()
                .map(membership -> toBoardResponse(membership.getBoard(), membership.getRole()))
                .toList();
    }

    @Transactional
    public ApiDtos.BoardResponse createBoard(@Valid ApiDtos.CreateBoardRequest request) {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        Instant now = Instant.now();
        Board board = boardRepository.save(new Board(UUID.randomUUID(), request.name().trim(), currentUser.user(), now));
        membershipRepository.save(new BoardMembership(board, currentUser.user(), BoardRole.OWNER, now));
        return toBoardResponse(board, BoardRole.OWNER);
    }

    @Transactional(readOnly = true)
    public ApiDtos.BoardResponse getBoard(UUID boardId) {
        BoardMembership membership = requireMembership(boardId, BoardRole.VIEWER);
        return toBoardResponse(membership.getBoard(), membership.getRole());
    }

    @Transactional
    public ApiDtos.BoardResponse updateBoard(UUID boardId, @Valid ApiDtos.UpdateBoardRequest request) {
        BoardMembership membership = requireMembership(boardId, BoardRole.OWNER);
        membership.getBoard().setName(request.name().trim());
        return toBoardResponse(membership.getBoard(), membership.getRole());
    }

    @Transactional
    public void archiveBoard(UUID boardId) {
        BoardMembership membership = requireMembership(boardId, BoardRole.OWNER);
        membership.getBoard().setArchivedAt(Instant.now());
    }

    @Transactional
    public void deleteBoard(UUID boardId) {
        BoardMembership membership = requireMembership(boardId, BoardRole.OWNER);
        boardRepository.delete(membership.getBoard());
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.ColumnResponse> listColumns(UUID boardId) {
        requireMembership(boardId, BoardRole.VIEWER);
        return columnRepository.findAllByBoardIdOrderByPositionAsc(boardId).stream()
                .map(column -> new ApiDtos.ColumnResponse(column.getId(), column.getName(), column.getPosition(), columnRepository.countTasksByColumnId(column.getId())))
                .toList();
    }

    @Transactional
    public ApiDtos.ColumnResponse createColumn(UUID boardId, @Valid ApiDtos.CreateColumnRequest request) {
        Board board = requireMembership(boardId, BoardRole.OWNER).getBoard();
        int position = (int) columnRepository.countByBoardId(boardId);
        BoardColumn column = columnRepository.save(new BoardColumn(UUID.randomUUID(), board, request.name().trim(), position, Instant.now()));
        return new ApiDtos.ColumnResponse(column.getId(), column.getName(), column.getPosition(), 0);
    }

    @Transactional
    public ApiDtos.ColumnResponse updateColumn(UUID boardId, UUID columnId, @Valid ApiDtos.UpdateColumnRequest request) {
        requireMembership(boardId, BoardRole.OWNER);
        BoardColumn column = requireColumn(boardId, columnId);
        column.setName(request.name().trim());
        return new ApiDtos.ColumnResponse(column.getId(), column.getName(), column.getPosition(), columnRepository.countTasksByColumnId(column.getId()));
    }

    @Transactional
    public void reorderColumns(UUID boardId, @Valid ApiDtos.ReorderColumnsRequest request) {
        requireMembership(boardId, BoardRole.OWNER);
        List<BoardColumn> columns = columnRepository.findAllByBoardIdOrderByPositionAsc(boardId);
        if (columns.size() != request.columnIds().size()) {
            throw ApiException.unprocessable("INVALID_COLUMN_ORDER", "The request must include every column on the board exactly once.");
        }
        Set<UUID> expected = columns.stream().map(BoardColumn::getId).collect(java.util.stream.Collectors.toSet());
        Set<UUID> actual = new HashSet<>(request.columnIds());
        if (actual.size() != request.columnIds().size() || !expected.equals(actual)) {
            throw ApiException.unprocessable("INVALID_COLUMN_ORDER", "The request must include every column on the board exactly once.");
        }
        for (int index = 0; index < request.columnIds().size(); index++) {
            UUID columnId = request.columnIds().get(index);
            BoardColumn column = columns.stream()
                    .filter(candidate -> candidate.getId().equals(columnId))
                    .findFirst()
                    .orElseThrow(() -> ApiException.unprocessable("INVALID_COLUMN_ORDER", "The request must include every column on the board exactly once."));
            column.setPosition(index);
        }
    }

    @Transactional
    public void deleteColumn(UUID boardId, UUID columnId, UUID moveTasksTo, Boolean deleteTasks) {
        requireMembership(boardId, BoardRole.OWNER);
        BoardColumn column = requireColumn(boardId, columnId);
        long taskCount = taskRepository.countByColumnId(columnId);
        if (taskCount > 0) {
            if (Boolean.TRUE.equals(deleteTasks)) {
                taskRepository.deleteAll(taskRepository.findAllByColumnId(columnId));
            } else if (moveTasksTo != null) {
                BoardColumn target = requireColumn(boardId, moveTasksTo);
                if (target.getId().equals(columnId)) {
                    throw ApiException.unprocessable("INVALID_TARGET_COLUMN", "Tasks cannot be moved into the same column.");
                }
                for (Task task : taskRepository.findAllByColumnId(columnId)) {
                    task.setColumn(target);
                }
            } else {
                throw ApiException.conflict("COLUMN_NOT_EMPTY", "Column has tasks and requires moveTasksTo or deleteTasks=true.");
            }
        }
        int removedPosition = column.getPosition();
        columnRepository.delete(column);
        columnRepository.findAllByBoardIdOrderByPositionAsc(boardId).stream()
                .filter(existing -> existing.getPosition() > removedPosition)
                .forEach(existing -> existing.setPosition(existing.getPosition() - 1));
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.TaskResponse> listTasks(UUID boardId, UUID columnId, UUID assigneeId, String label,
                                                LocalDate dueBefore, LocalDate dueAfter, boolean archived) {
        requireMembership(boardId, BoardRole.VIEWER);
        Specification<Task> spec = Specification.where(byBoardId(boardId))
                .and(hasArchivedState(archived));
        if (columnId != null) {
            spec = spec.and(hasColumnId(columnId));
        }
        if (assigneeId != null) {
            spec = spec.and(hasAssigneeId(assigneeId));
        }
        if (label != null && !label.isBlank()) {
            spec = spec.and(hasLabel(label));
        }
        if (dueBefore != null) {
            spec = spec.and(dueBefore(dueBefore));
        }
        if (dueAfter != null) {
            spec = spec.and(dueAfter(dueAfter));
        }
        return taskRepository.findAll(spec).stream()
                .map(this::toTaskResponse)
                .toList();
    }

    @Transactional
    public ApiDtos.TaskResponse createTask(UUID boardId, @Valid ApiDtos.CreateTaskRequest request) {
        BoardMembership membership = requireMembership(boardId, BoardRole.MEMBER);
        BoardColumn column = requireColumn(boardId, request.columnId());
        AppUser assignee = request.assigneeId() == null ? null : requireBoardMember(boardId, request.assigneeId()).getUser();
        Task task = new Task(
                UUID.randomUUID(),
                membership.getBoard(),
                column,
                membership.getUser(),
                request.title().trim(),
                request.description(),
                assignee,
                request.dueDate(),
                Instant.now());
        task.replaceLabels(resolveLabels(boardId, request.labels()));
        return toTaskResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public ApiDtos.TaskResponse getTask(UUID taskId) {
        return toTaskResponse(requireTask(taskId, BoardRole.VIEWER));
    }

    @Transactional
    public ApiDtos.TaskResponse updateTask(UUID taskId, ApiDtos.UpdateTaskRequest request) {
        Task task = requireTask(taskId, BoardRole.MEMBER);
        if (request.isTitleSet()) {
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                throw ApiException.unprocessable("VALIDATION_ERROR", "title must not be blank");
            }
            task.setTitle(request.getTitle().trim());
        }
        if (request.isDescriptionSet()) {
            task.setDescription(request.getDescription());
        }
        if (request.isAssigneeIdSet()) {
            AppUser assignee = request.getAssigneeId() == null ? null : requireBoardMember(task.getBoard().getId(), request.getAssigneeId()).getUser();
            task.setAssignee(assignee);
        }
        if (request.isDueDateSet()) {
            task.setDueDate(request.getDueDate());
        }
        if (request.isLabelsSet()) {
            task.replaceLabels(resolveLabels(task.getBoard().getId(), request.getLabels()));
        }
        return toTaskResponse(task);
    }

    @Transactional
    public ApiDtos.TaskResponse moveTask(UUID taskId, @Valid ApiDtos.MoveTaskRequest request) {
        Task task = requireTask(taskId, BoardRole.MEMBER);
        BoardColumn targetColumn = requireColumn(task.getBoard().getId(), request.columnId());
        if (!Objects.equals(targetColumn.getBoard().getId(), task.getBoard().getId())) {
            throw ApiException.unprocessable("INVALID_TARGET_COLUMN", "Target column does not belong to the same board.");
        }
        task.setColumn(targetColumn);
        return toTaskResponse(task);
    }

    @Transactional
    public void archiveTask(UUID taskId) {
        Task task = requireTask(taskId, BoardRole.MEMBER);
        task.setArchivedAt(Instant.now());
    }

    @Transactional
    public ApiDtos.TaskResponse restoreTask(UUID taskId, @Valid ApiDtos.RestoreTaskRequest request) {
        Task task = requireTask(taskId, BoardRole.MEMBER);
        task.setColumn(requireColumn(task.getBoard().getId(), request.columnId()));
        task.setArchivedAt(null);
        return toTaskResponse(task);
    }

    @Transactional
    public void deleteTask(UUID taskId) {
        Task task = requireTask(taskId, BoardRole.MEMBER);
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.CommentResponse> listComments(UUID taskId) {
        Task task = requireTask(taskId, BoardRole.VIEWER);
        return commentRepository.findAllByTaskIdOrderByCreatedAtAsc(task.getId()).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional
    public ApiDtos.CommentResponse addComment(UUID taskId, @Valid ApiDtos.CreateCommentRequest request) {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        Task task = requireTask(taskId, BoardRole.MEMBER);
        if (task.getArchivedAt() != null) {
            throw ApiException.unprocessable("TASK_ARCHIVED", "Comments cannot be added to archived tasks.");
        }
        Comment comment = commentRepository.save(new Comment(UUID.randomUUID(), task, currentUser.user(), request.body().trim(), Instant.now()));
        return toCommentResponse(comment);
    }

    @Transactional
    public ApiDtos.CommentResponse updateComment(UUID taskId, UUID commentId, @Valid ApiDtos.UpdateCommentRequest request) {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        requireTask(taskId, BoardRole.MEMBER);
        Comment comment = commentRepository.findByTaskIdAndId(taskId, commentId)
                .orElseThrow(() -> ApiException.notFound("COMMENT_NOT_FOUND", "Comment not found."));
        if (!comment.getAuthor().getId().equals(currentUser.user().getId())) {
            throw ApiException.forbidden("FORBIDDEN", "Users may only edit their own comments.");
        }
        comment.setBody(request.body().trim());
        return toCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(UUID taskId, UUID commentId) {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        Task task = requireTask(taskId, BoardRole.VIEWER);
        Comment comment = commentRepository.findByTaskIdAndId(taskId, commentId)
                .orElseThrow(() -> ApiException.notFound("COMMENT_NOT_FOUND", "Comment not found."));
        if (!comment.getAuthor().getId().equals(currentUser.user().getId())) {
            requireMembership(task.getBoard().getId(), BoardRole.OWNER);
        }
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.MemberResponse> listMembers(UUID boardId) {
        requireMembership(boardId, BoardRole.VIEWER);
        return membershipRepository.findAllByBoardId(boardId).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public ApiDtos.MemberResponse addMember(UUID boardId, @Valid ApiDtos.AddMemberRequest request) {
        requireMembership(boardId, BoardRole.OWNER);
        if (membershipRepository.existsByBoardIdAndUserId(boardId, request.userId())) {
            throw ApiException.conflict("MEMBER_EXISTS", "User is already a member of this board.");
        }
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> ApiException.notFound("BOARD_NOT_FOUND", "Board not found."));
        AppUser user = requireUser(request.userId());
        BoardRole role = parseNonOwnerRole(request.role());
        BoardMembership membership = membershipRepository.save(new BoardMembership(board, user, role, Instant.now()));
        return toMemberResponse(membership);
    }

    @Transactional
    public ApiDtos.MemberResponse updateMemberRole(UUID boardId, UUID userId, @Valid ApiDtos.UpdateMemberRoleRequest request) {
        requireMembership(boardId, BoardRole.OWNER);
        BoardMembership membership = requireBoardMember(boardId, userId);
        if (membership.getRole() == BoardRole.OWNER) {
            throw ApiException.unprocessable("OWNER_ROLE_IMMUTABLE", "Cannot change the role of the board owner without transferring ownership.");
        }
        membership.setRole(parseNonOwnerRole(request.role()));
        return toMemberResponse(membership);
    }

    @Transactional
    public void transferOwnership(UUID boardId, @Valid ApiDtos.TransferOwnershipRequest request) {
        BoardMembership currentOwner = requireMembership(boardId, BoardRole.OWNER);
        BoardMembership newOwner = requireBoardMember(boardId, request.userId());
        currentOwner.setRole(BoardRole.MEMBER);
        newOwner.setRole(BoardRole.OWNER);
        currentOwner.getBoard().setOwner(newOwner.getUser());
    }

    @Transactional
    public void removeMember(UUID boardId, UUID userId) {
        requireMembership(boardId, BoardRole.OWNER);
        BoardMembership membership = requireBoardMember(boardId, userId);
        if (membership.getRole() == BoardRole.OWNER) {
            throw ApiException.unprocessable("OWNER_CANNOT_BE_REMOVED", "Cannot remove the board owner; transfer ownership first.");
        }
        membershipRepository.delete(membership);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.LabelResponse> listLabels(UUID boardId) {
        requireMembership(boardId, BoardRole.VIEWER);
        return labelRepository.findAllByBoardIdOrderByNameAsc(boardId).stream()
                .map(label -> new ApiDtos.LabelResponse(label.getId(), label.getName(), label.getColor()))
                .toList();
    }

    @Transactional
    public ApiDtos.LabelResponse createLabel(UUID boardId, @Valid ApiDtos.CreateLabelRequest request) {
        Board board = requireMembership(boardId, BoardRole.MEMBER).getBoard();
        boolean exists = labelRepository.findAllByBoardIdOrderByNameAsc(boardId).stream()
                .anyMatch(label -> label.getName().equalsIgnoreCase(request.name().trim()));
        if (exists) {
            throw ApiException.conflict("LABEL_EXISTS", "A label with that name already exists on this board.");
        }
        Label label = labelRepository.save(new Label(UUID.randomUUID(), board, request.name().trim(), request.color()));
        return new ApiDtos.LabelResponse(label.getId(), label.getName(), label.getColor());
    }

    @Transactional
    public void deleteLabel(UUID boardId, UUID labelId) {
        requireMembership(boardId, BoardRole.OWNER);
        Label label = labelRepository.findByBoardIdAndId(boardId, labelId)
                .orElseThrow(() -> ApiException.notFound("LABEL_NOT_FOUND", "Label not found."));
        labelRepository.delete(label);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.TaskResponse> listMyTasks(LocalDate dueBefore, LocalDate dueAfter) {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        return taskRepository.findAssignedVisibleTasks(currentUser.user().getId()).stream()
                .filter(task -> dueBefore == null || task.getDueDate() == null || !task.getDueDate().isAfter(dueBefore))
                .filter(task -> dueAfter == null || task.getDueDate() == null || !task.getDueDate().isBefore(dueAfter))
                .map(this::toTaskResponse)
                .toList();
    }

    private BoardMembership requireMembership(UUID boardId, BoardRole minimumRole) {
        CurrentUser currentUser = currentUserService.getCurrentUser();
        BoardMembership membership = membershipRepository.findByBoardIdAndUserId(boardId, currentUser.user().getId())
                .orElseThrow(() -> ApiException.notFound("BOARD_NOT_FOUND", "Board not found."));
        if (!membership.getRole().atLeast(minimumRole)) {
            throw ApiException.forbidden("FORBIDDEN", "Authenticated user lacks permission for this operation.");
        }
        return membership;
    }

    private BoardColumn requireColumn(UUID boardId, UUID columnId) {
        return columnRepository.findByBoardIdAndId(boardId, columnId)
                .orElseThrow(() -> ApiException.notFound("COLUMN_NOT_FOUND", "Column not found."));
    }

    private Task requireTask(UUID taskId, BoardRole minimumRole) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> ApiException.notFound("TASK_NOT_FOUND", "Task not found."));
        requireMembership(task.getBoard().getId(), minimumRole);
        return task;
    }

    private BoardMembership requireBoardMember(UUID boardId, UUID userId) {
        return membershipRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> ApiException.notFound("MEMBER_NOT_FOUND", "Board member not found."));
    }

    private AppUser requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found."));
    }

    private Set<Label> resolveLabels(UUID boardId, Collection<String> names) {
        List<String> normalized = normalizeLabels(names);
        if (normalized.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<Label> labels = labelRepository.findAllByBoardIdAndNameIn(boardId, normalized);
        if (labels.size() != normalized.size()) {
            throw ApiException.unprocessable("LABEL_NOT_FOUND", "One or more labels do not exist on this board.");
        }
        return new LinkedHashSet<>(labels);
    }

    private List<String> normalizeLabels(Collection<String> names) {
        if (names == null) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            normalized.add(name.trim());
        }
        return normalized.stream().distinct().toList();
    }

    private BoardRole parseNonOwnerRole(String rawRole) {
        BoardRole role = parseRole(rawRole);
        if (role == BoardRole.OWNER) {
            throw ApiException.unprocessable("INVALID_ROLE", "Only member or viewer may be assigned directly.");
        }
        return role;
    }

    private BoardRole parseRole(String rawRole) {
        try {
            return BoardRole.valueOf(rawRole.trim().toUpperCase());
        } catch (Exception exception) {
            throw ApiException.unprocessable("INVALID_ROLE", "Role must be one of owner, member, or viewer.");
        }
    }

    private ApiDtos.BoardResponse toBoardResponse(Board board, BoardRole role) {
        return new ApiDtos.BoardResponse(board.getId(), board.getName(), role.apiValue(), board.getArchivedAt(), board.getCreatedAt());
    }

    private ApiDtos.TaskResponse toTaskResponse(Task task) {
        return new ApiDtos.TaskResponse(
                task.getId(),
                task.getBoard().getId(),
                task.getColumn().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getAssignee() == null ? null : new ApiDtos.UserSummaryResponse(task.getAssignee().getId(), task.getAssignee().getDisplayName()),
                task.getDueDate(),
                task.getLabels().stream().map(Label::getName).sorted().toList(),
                task.getArchivedAt(),
                task.getCreatedAt(),
                new ApiDtos.UserSummaryResponse(task.getCreatedBy().getId(), task.getCreatedBy().getDisplayName()));
    }

    private ApiDtos.CommentResponse toCommentResponse(Comment comment) {
        return new ApiDtos.CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getBody(),
                new ApiDtos.UserSummaryResponse(comment.getAuthor().getId(), comment.getAuthor().getDisplayName()),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

    private ApiDtos.MemberResponse toMemberResponse(BoardMembership membership) {
        return new ApiDtos.MemberResponse(
                membership.getUser().getId(),
                membership.getUser().getDisplayName(),
                membership.getUser().getEmail(),
                membership.getRole().apiValue(),
                membership.getJoinedAt());
    }

    private Specification<Task> byBoardId(UUID boardId) {
        return (root, query, builder) -> builder.equal(root.get("board").get("id"), boardId);
    }

    private Specification<Task> hasArchivedState(boolean archived) {
        return (root, query, builder) -> archived
                ? builder.isNotNull(root.get("archivedAt"))
                : builder.isNull(root.get("archivedAt"));
    }

    private Specification<Task> hasColumnId(UUID columnId) {
        return columnId == null ? null : (root, query, builder) -> builder.equal(root.get("column").get("id"), columnId);
    }

    private Specification<Task> hasAssigneeId(UUID assigneeId) {
        return assigneeId == null ? null : (root, query, builder) -> builder.equal(root.get("assignee").get("id"), assigneeId);
    }

    private Specification<Task> hasLabel(String label) {
        return label == null || label.isBlank() ? null : (root, query, builder) -> {
            query.distinct(true);
            return builder.equal(root.join("labels").get("name"), label.trim());
        };
    }

    private Specification<Task> dueBefore(LocalDate dueBefore) {
        return dueBefore == null ? null : (root, query, builder) -> builder.lessThan(root.get("dueDate"), dueBefore.plusDays(1));
    }

    private Specification<Task> dueAfter(LocalDate dueAfter) {
        return dueAfter == null ? null : (root, query, builder) -> builder.greaterThan(root.get("dueDate"), dueAfter.minusDays(1));
    }
}
