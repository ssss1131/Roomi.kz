package kz.site.roomi.profile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kz.site.roomi.common.ApiException;
import kz.site.roomi.common.Gender;
import kz.site.roomi.profile.dto.ProfileRequest;
import kz.site.roomi.profile.dto.PublicProfile;
import kz.site.roomi.profile.dto.RoommateSummary;
import kz.site.roomi.user.User;
import kz.site.roomi.user.UserRepository;
import kz.site.roomi.user.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

	private final UserRepository users;
	private final ProfileRepository profiles;

	public ProfileService(UserRepository users, ProfileRepository profiles) {
		this.users = users;
		this.profiles = profiles;
	}

	@Transactional(readOnly = true)
	public UserResponse me(Long userId) {
		User user = loadUser(userId);
		return UserResponse.from(user, profiles.findById(userId).orElse(null));
	}

	@Transactional
	public UserResponse save(Long userId, ProfileRequest request) {
		validate(request);
		User user = loadUser(userId);
		Profile profile = profiles.findById(userId).orElseGet(() -> new Profile(user));
		profile.setName(request.name());
		profile.setGender(Gender.from(request.gender()));
		profile.setTelegram(request.contacts().telegram());
		profile.setWhatsapp(request.contacts().whatsapp());
		profile.setPhone(request.contacts().phone());
		profile.setLookingForRoommate(request.lookingForRoommate());
		profile.setAbout(request.about());
		profile.setMinPrice(request.minPrice());
		profile.setMaxPrice(request.maxPrice());
		profile.setSmokes(request.habits().smokes());
		profile.setHasPets(request.habits().hasPets());
		profile.setTidy(request.habits().isTidy());
		return UserResponse.from(user, profiles.save(profile));
	}

	@Transactional(readOnly = true)
	public List<RoommateSummary> roommates(Long callerId) {
		return profiles.findRoommatesExcluding(callerId).stream().map(RoommateSummary::from).toList();
	}

	@Transactional(readOnly = true)
	public PublicProfile roommate(Long userId) {
		return profiles.findById(userId)
				.filter(Profile::isLookingForRoommate)
				.map(PublicProfile::from)
				.orElseThrow(() -> ApiException.notFound("Roommate profile not found"));
	}

	private User loadUser(Long userId) {
		return users.findById(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required"));
	}

	private static void validate(ProfileRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		if (request.contacts().isEmpty()) {
			errors.put("contacts", "Add at least one contact method");
		}
		Integer min = request.minPrice();
		Integer max = request.maxPrice();
		if ((min == null) != (max == null)) {
			errors.put("minPrice", "Set both budget limits or leave both empty");
			errors.put("maxPrice", "Set both budget limits or leave both empty");
		}
		else if (min != null && max < min) {
			errors.put("maxPrice", "Maximum budget must not be less than minimum budget");
		}
		if (!errors.isEmpty()) {
			throw ApiException.validation(errors);
		}
	}

}
