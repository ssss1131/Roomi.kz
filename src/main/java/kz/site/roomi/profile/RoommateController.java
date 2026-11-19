package kz.site.roomi.profile;

import kz.site.roomi.common.CurrentUser;
import kz.site.roomi.common.ItemsResponse;
import kz.site.roomi.profile.dto.PublicProfile;
import kz.site.roomi.profile.dto.RoommateSummary;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roommates")
public class RoommateController {

	private final ProfileService profileService;

	public RoommateController(ProfileService profileService) {
		this.profileService = profileService;
	}

	@GetMapping
	public ItemsResponse<RoommateSummary> list(@AuthenticationPrincipal Jwt jwt) {
		return new ItemsResponse<>(profileService.roommates(CurrentUser.id(jwt)));
	}

	@GetMapping("/{id}")
	public PublicProfile get(@PathVariable Long id) {
		return profileService.roommate(id);
	}

}
