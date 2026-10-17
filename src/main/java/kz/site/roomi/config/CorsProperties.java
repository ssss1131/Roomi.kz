package kz.site.roomi.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> origins) {

	public CorsProperties {
		origins = origins == null ? List.of() : origins.stream().map(String::trim).filter(o -> !o.isEmpty()).toList();
	}

}
