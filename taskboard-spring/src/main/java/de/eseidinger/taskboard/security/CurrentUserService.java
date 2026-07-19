package de.eseidinger.taskboard.security;

import de.eseidinger.taskboard.domain.AppUser;
import de.eseidinger.taskboard.error.ApiException;
import de.eseidinger.taskboard.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class CurrentUserService {

    private final AppUserRepository userRepository;

    public CurrentUserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw ApiException.unauthorized("UNAUTHORIZED", "A valid access token is required.");
        }

        Jwt jwt = jwtAuthenticationToken.getToken();
        String subject = Optional.ofNullable(jwt.getSubject())
                .orElseThrow(() -> ApiException.unauthorized("UNAUTHORIZED", "Token subject is missing."));

        String email = Optional.ofNullable(jwt.getClaimAsString("email"))
                .orElse(subject + "@taskboard.local");
        String displayName = Optional.ofNullable(jwt.getClaimAsString("name"))
                .or(() -> Optional.ofNullable(jwt.getClaimAsString("preferred_username")))
                .orElse(email);

        AppUser user = userRepository.findByExternalId(subject)
                .map(existing -> refresh(existing, displayName, email))
                .orElseGet(() -> userRepository.save(new AppUser(UUID.randomUUID(), subject, displayName, email, Instant.now())));

        return new CurrentUser(user, subject);
    }

    private AppUser refresh(AppUser existing, String displayName, String email) {
        existing.setDisplayName(displayName);
        existing.setEmail(email);
        return existing;
    }
}
