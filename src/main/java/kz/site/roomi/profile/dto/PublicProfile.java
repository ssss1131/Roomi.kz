package kz.site.roomi.profile.dto;

import kz.site.roomi.common.Gender;
import kz.site.roomi.profile.Profile;

public record PublicProfile(
		Long userId,
		String name,
		String gender,
		String about,
		Integer minPrice,
		Integer maxPrice,
		Habits habits,
		Contacts contacts) {

	public static PublicProfile from(Profile profile) {
		return new PublicProfile(profile.getUserId(), profile.getName(), Gender.toValue(profile.getGender()),
				profile.getAbout(), profile.getMinPrice(), profile.getMaxPrice(), Habits.from(profile),
				Contacts.from(profile));
	}

	public static PublicProfile withoutProfile(Long userId) {
		return new PublicProfile(userId, null, null, null, null, null, Habits.empty(), Contacts.empty());
	}

}
