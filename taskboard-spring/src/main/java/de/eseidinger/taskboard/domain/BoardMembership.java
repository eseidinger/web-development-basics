package de.eseidinger.taskboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "board_membership")
public class BoardMembership {

    @EmbeddedId
    private BoardMembershipId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("boardId")
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardRole role;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected BoardMembership() {
    }

    public BoardMembership(Board board, AppUser user, BoardRole role, Instant joinedAt) {
        this.id = new BoardMembershipId(board.getId(), user.getId());
        this.board = board;
        this.user = user;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public BoardMembershipId getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public AppUser getUser() {
        return user;
    }

    public BoardRole getRole() {
        return role;
    }

    public void setRole(BoardRole role) {
        this.role = role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
