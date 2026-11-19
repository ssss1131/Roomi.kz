package kz.site.roomi.user;

import jakarta.validation.Valid;
import kz.site.roomi.common.CurrentUser;
import kz.site.roomi.profile.ProfileService;
import kz.site.roomi.profile.dto.ProfileRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final ProfileService profileService;

	public UserController(ProfileService profileService) {
		this.profileService = profileService;
	}

	@GetMapping("/me")
	public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
		return profileService.me(CurrentUser.id(jwt));
	}

	@PutMapping("/me")
	public UserResponse updateProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProfileRequest request) {
		return profileService.save(CurrentUser.id(jwt), request);
	}

}
