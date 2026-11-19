package kz.site.roomi.profile.dto;

import kz.site.roomi.common.Gender;
import kz.site.roomi.profile.Profile;

public record ProfileResponse(
		String name,
		String gender,
		Contacts contacts,
		boolean lookingForRoommate,
		String about,
		Integer minPrice,
		Integer maxPrice,
		Habits habits) {

	public static ProfileResponse empty() {
		return new ProfileResponse(null, null, Contacts.empty(), false, null, null, null, Habits.empty());
	}

	public static ProfileResponse from(Profile profile) {
		if (profile == null) {
			return empty();
		}
		return new ProfileResponse(profile.getName(), Gender.toValue(profile.getGender()), Contacts.from(profile),
				profile.isLookingForRoommate(), profile.getAbout(), profile.getMinPrice(), profile.getMaxPrice(),
				Habits.from(profile));
	}

}
