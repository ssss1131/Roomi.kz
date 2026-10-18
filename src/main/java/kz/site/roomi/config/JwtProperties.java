package kz.site.roomi.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration ttl) {

	public JwtProperties {
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalArgumentException("app.jwt.secret (JWT_SECRET) must be at least 32 bytes");
		}
		if (ttl == null || ttl.isNegative() || ttl.isZero()) {
			ttl = Duration.ofHours(1);
		}
	}

}
