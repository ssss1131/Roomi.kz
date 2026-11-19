package kz.site.roomi.profile.dto;

import kz.site.roomi.common.Gender;
import kz.site.roomi.profile.Profile;

public record RoommateSummary(
		Long userId,
		String name,
		String gender,
		String about,
		Integer minPrice,
		Integer maxPrice) {

	public static RoommateSummary from(Profile profile) {
		return new RoommateSummary(profile.getUserId(), profile.getName(), Gender.toValue(profile.getGender()),
				profile.getAbout(), profile.getMinPrice(), profile.getMaxPrice());
	}

}
