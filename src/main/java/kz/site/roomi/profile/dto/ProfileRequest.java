package kz.site.roomi.profile.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kz.site.roomi.common.Gender;
import kz.site.roomi.common.Strings;

public record ProfileRequest(
		@NotNull(message = "Name is required") @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters") String name,
		@NotNull(message = "Choose a gender") @Pattern(regexp = Gender.PATTERN, message = "Choose male or female") String gender,
		@Valid Contacts contacts,
		Boolean lookingForRoommate,
		@Size(max = 1000, message = "About must be 1000 characters or fewer") String about,
		@Min(value = 1, message = "Enter a whole number from 1 to 1000000") @Max(value = 1_000_000, message = "Enter a whole number from 1 to 1000000") Integer minPrice,
		@Min(value = 1, message = "Enter a whole number from 1 to 1000000") @Max(value = 1_000_000, message = "Enter a whole number from 1 to 1000000") Integer maxPrice,
		Habits habits) {

	public ProfileRequest {
		name = Strings.trimToNull(name);
		gender = Strings.trimToNull(gender);
		contacts = contacts == null ? Contacts.empty() : contacts;
		lookingForRoommate = Boolean.TRUE.equals(lookingForRoommate);
		about = Strings.trimToNull(about);
		habits = habits == null ? Habits.empty() : habits;
	}

}
