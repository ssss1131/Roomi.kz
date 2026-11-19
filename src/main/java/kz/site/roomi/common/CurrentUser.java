package kz.site.roomi.common;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentUser {

	private CurrentUser() {
	}

	public static Long id(Jwt jwt) {
		try {
			return Long.parseLong(jwt.getSubject());
		}
		catch (RuntimeException ex) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
		}
	}

}
