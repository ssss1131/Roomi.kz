package kz.site.roomi.auth;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

import kz.site.roomi.auth.dto.RegisterRequest;
import kz.site.roomi.common.ApiException;
import kz.site.roomi.user.User;
import kz.site.roomi.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private static final Duration TOKEN_TTL = Duration.ofHours(24);

	private final UserRepository users;
	private final VerificationTokenRepository tokens;
	private final PasswordEncoder passwordEncoder;
	private final MailService mailService;
	private final SecureRandom random = new SecureRandom();

	public AuthService(UserRepository users, VerificationTokenRepository tokens, PasswordEncoder passwordEncoder,
			MailService mailService) {
		this.users = users;
		this.tokens = tokens;
		this.passwordEncoder = passwordEncoder;
		this.mailService = mailService;
	}

	@Transactional
	public void register(RegisterRequest request) {
		String email = normalize(request.email());
		if (users.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "This email is already registered");
		}
		User user;
		try {
			user = users.saveAndFlush(new User(email, passwordEncoder.encode(request.password())));
		}
		catch (DataIntegrityViolationException ex) {
			throw new ApiException(HttpStatus.CONFLICT, "This email is already registered");
		}
		issueToken(user);
	}

	@Transactional
	public void confirm(String rawToken) {
		VerificationToken token = tokens.findByToken(rawToken == null ? "" : rawToken.trim())
				.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Confirmation link is invalid"));
		if (token.isExpired(Instant.now())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Confirmation link has expired, please request a new one");
		}
		User user = token.getUser();
		user.setEnabled(true);
		users.save(user);
		tokens.deleteAllByUser(user);
	}

	@Transactional
	public void resend(String rawEmail) {
		users.findByEmail(normalize(rawEmail))
				.filter(user -> !user.isEnabled())
				.ifPresent(user -> {
					tokens.deleteAllByUser(user);
					issueToken(user);
				});
	}

	private void issueToken(User user) {
		VerificationToken token = tokens.save(new VerificationToken(generateToken(), user, Instant.now().plus(TOKEN_TTL)));
		mailService.sendConfirmation(user.getEmail(), token.getToken());
	}

	private String generateToken() {
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	static String normalize(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}

}
