package de.eseidinger.taskboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class BoardMembershipId implements Serializable {

    @Column(name = "board_id")
    private UUID boardId;

    @Column(name = "user_id")
    private UUID userId;

    protected BoardMembershipId() {
    }

    public BoardMembershipId(UUID boardId, UUID userId) {
        this.boardId = boardId;
        this.userId = userId;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BoardMembershipId that)) {
            return false;
        }
        return Objects.equals(boardId, that.boardId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardId, userId);
    }
}
