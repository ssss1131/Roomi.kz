package kz.site.roomi.auth;

import jakarta.validation.Valid;
import kz.site.roomi.auth.dto.LoginRequest;
import kz.site.roomi.auth.dto.MessageResponse;
import kz.site.roomi.auth.dto.RegisterRequest;
import kz.site.roomi.auth.dto.ResendRequest;
import kz.site.roomi.auth.dto.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
		authService.register(request);
		return new MessageResponse("Registration successful. Please check your email to confirm your account");
	}

	@GetMapping("/confirm")
	public MessageResponse confirm(@RequestParam String token) {
		authService.confirm(token);
		return new MessageResponse("Email confirmed. You can now log in");
	}

	@PostMapping("/resend")
	public MessageResponse resend(@Valid @RequestBody ResendRequest request) {
		authService.resend(request.email());
		return new MessageResponse("If this email is registered and not confirmed, a new confirmation link has been sent");
	}

	@PostMapping("/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

}
