package de.eseidinger.taskboard.domain;

public enum BoardRole {
    OWNER,
    MEMBER,
    VIEWER;

    public boolean atLeast(BoardRole required) {
        return rank(this) >= rank(required);
    }

    public String apiValue() {
        return name().toLowerCase();
    }

    private static int rank(BoardRole role) {
        return switch (role) {
            case VIEWER -> 0;
            case MEMBER -> 1;
            case OWNER -> 2;
        };
    }
}
