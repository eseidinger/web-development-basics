package de.eseidinger.taskboard.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@EnableConfigurationProperties(PublicConfigController.PublicConfigProperties.class)
public class PublicConfigController {

    private final PublicConfigProperties properties;

    public PublicConfigController(PublicConfigProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    PublicConfigResponse getConfig() {
        return new PublicConfigResponse(
                properties.getApiBasePath(),
                properties.getKeycloakUrl(),
                properties.getKeycloakRealm(),
                properties.getKeycloakClientId());
    }

    public record PublicConfigResponse(
            String apiBasePath,
            String keycloakUrl,
            String keycloakRealm,
            String keycloakClientId) {
    }

    @ConfigurationProperties(prefix = "taskboard.public-config")
    public static class PublicConfigProperties {
        private String apiBasePath;
        private String keycloakUrl;
        private String keycloakRealm;
        private String keycloakClientId;

        public String getApiBasePath() {
            return apiBasePath;
        }

        public void setApiBasePath(String apiBasePath) {
            this.apiBasePath = apiBasePath;
        }

        public String getKeycloakUrl() {
            return keycloakUrl;
        }

        public void setKeycloakUrl(String keycloakUrl) {
            this.keycloakUrl = keycloakUrl;
        }

        public String getKeycloakRealm() {
            return keycloakRealm;
        }

        public void setKeycloakRealm(String keycloakRealm) {
            this.keycloakRealm = keycloakRealm;
        }

        public String getKeycloakClientId() {
            return keycloakClientId;
        }

        public void setKeycloakClientId(String keycloakClientId) {
            this.keycloakClientId = keycloakClientId;
        }
    }
}