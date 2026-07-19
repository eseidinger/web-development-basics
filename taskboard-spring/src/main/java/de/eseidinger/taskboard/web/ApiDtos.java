package de.eseidinger.taskboard.web;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record UserSummaryResponse(UUID id, String displayName) {
    }

    public record BoardResponse(UUID id, String name, String role, Instant archivedAt, Instant createdAt) {
    }

    public record ColumnResponse(UUID id, String name, int position, long taskCount) {
    }

    public record TaskResponse(
            UUID id,
            UUID boardId,
            UUID columnId,
            String title,
            String description,
            UserSummaryResponse assignee,
            LocalDate dueDate,
            List<String> labels,
            Instant archivedAt,
            Instant createdAt,
            UserSummaryResponse createdBy) {
    }

    public record CommentResponse(
            UUID id,
            UUID taskId,
            String body,
            UserSummaryResponse author,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record MemberResponse(
            UUID userId,
            String displayName,
            String email,
            String role,
            Instant joinedAt) {
    }

    public record LabelResponse(UUID id, String name, String color) {
    }

    public record CreateBoardRequest(@NotBlank String name) {
    }

    public record UpdateBoardRequest(@NotBlank String name) {
    }

    public record CreateColumnRequest(@NotBlank String name) {
    }

    public record UpdateColumnRequest(@NotBlank String name) {
    }

    public record ReorderColumnsRequest(@NotEmpty List<UUID> columnIds) {
    }

    public record CreateTaskRequest(
            @NotNull UUID columnId,
            @NotBlank String title,
            String description,
            UUID assigneeId,
            LocalDate dueDate,
            List<String> labels) {
    }

    public static final class UpdateTaskRequest {
        private String title;
        private boolean titleSet;
        private String description;
        private boolean descriptionSet;
        private UUID assigneeId;
        private boolean assigneeIdSet;
        private LocalDate dueDate;
        private boolean dueDateSet;
        private List<String> labels;
        private boolean labelsSet;

        public String getTitle() {
            return title;
        }

        public boolean isTitleSet() {
            return titleSet;
        }

        @JsonSetter("title")
        public void setTitle(String title) {
            this.title = title;
            this.titleSet = true;
        }

        public String getDescription() {
            return description;
        }

        public boolean isDescriptionSet() {
            return descriptionSet;
        }

        @JsonSetter("description")
        public void setDescription(String description) {
            this.description = description;
            this.descriptionSet = true;
        }

        public UUID getAssigneeId() {
            return assigneeId;
        }

        public boolean isAssigneeIdSet() {
            return assigneeIdSet;
        }

        @JsonSetter("assigneeId")
        public void setAssigneeId(UUID assigneeId) {
            this.assigneeId = assigneeId;
            this.assigneeIdSet = true;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public boolean isDueDateSet() {
            return dueDateSet;
        }

        @JsonSetter("dueDate")
        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            this.dueDateSet = true;
        }

        public List<String> getLabels() {
            return labels;
        }

        public boolean isLabelsSet() {
            return labelsSet;
        }

        @JsonSetter("labels")
        public void setLabels(List<String> labels) {
            this.labels = labels;
            this.labelsSet = true;
        }
    }

    public record MoveTaskRequest(@NotNull UUID columnId) {
    }

    public record RestoreTaskRequest(@NotNull UUID columnId) {
    }

    public record CreateCommentRequest(@NotBlank String body) {
    }

    public record UpdateCommentRequest(@NotBlank String body) {
    }

    public record AddMemberRequest(@NotNull UUID userId, @NotBlank String role) {
    }

    public record UpdateMemberRoleRequest(@NotBlank String role) {
    }

    public record TransferOwnershipRequest(@NotNull UUID userId) {
    }

    public record CreateLabelRequest(@NotBlank String name, String color) {
    }
}
