package kz.site.roomi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record AppMailProperties(boolean enabled, String from, String confirmUrl) {
}
