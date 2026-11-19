package kz.site.roomi.user;

import java.time.Instant;

import kz.site.roomi.profile.Profile;
import kz.site.roomi.profile.dto.ProfileResponse;

public record UserResponse(Long id, String email, Instant createdAt, ProfileResponse profile) {

	public static UserResponse from(User user, Profile profile) {
		return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt(), ProfileResponse.from(profile));
	}

}
