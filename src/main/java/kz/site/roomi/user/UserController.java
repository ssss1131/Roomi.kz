package kz.site.roomi.user;

import java.util.Optional;

import kz.site.roomi.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserRepository users;

	public UserController(UserRepository users) {
		this.users = users;
	}

	@GetMapping("/me")
	@Transactional(readOnly = true)
	public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
		return parseId(jwt.getSubject())
				.flatMap(users::findById)
				.map(UserResponse::from)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
	}

	private static Optional<Long> parseId(String subject) {
		try {
			return Optional.of(Long.parseLong(subject));
		}
		catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

}
