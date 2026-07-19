package de.eseidinger.taskboard.security;

import de.eseidinger.taskboard.domain.AppUser;

public record CurrentUser(AppUser user, String subject) {
}
