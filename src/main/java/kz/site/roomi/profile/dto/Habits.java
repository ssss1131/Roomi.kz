package kz.site.roomi.profile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kz.site.roomi.profile.Profile;

public record Habits(Boolean smokes, Boolean hasPets, @JsonProperty("isTidy") Boolean isTidy) {

	public static Habits empty() {
		return new Habits(null, null, null);
	}

	public static Habits from(Profile profile) {
		return new Habits(profile.getSmokes(), profile.getHasPets(), profile.getTidy());
	}

}
