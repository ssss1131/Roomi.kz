package kz.site.roomi.room.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kz.site.roomi.common.Gender;
import kz.site.roomi.common.Strings;

public record RoomRequest(
		@NotNull(message = "City is required") @Size(min = 2, max = 60, message = "City must be 2-60 characters") String city,
		@NotNull(message = "Region is required") @Size(min = 2, max = 60, message = "Region must be 2-60 characters") String region,
		@NotNull(message = "Approximate address is required") @Size(min = 3, max = 150, message = "Approximate address must be 3-150 characters") String approximateAddress,
		@NotNull(message = "Price is required") @Min(value = 1, message = "Price must be from 1 to 1000000") @Max(value = 1_000_000, message = "Price must be from 1 to 1000000") Integer price,
		@NotNull(message = "Rooms count is required") @Min(value = 1, message = "Choose a number from 1 to 20") @Max(value = 20, message = "Choose a number from 1 to 20") Integer roomsCount,
		@NotNull(message = "Floor is required") @Min(value = 1, message = "Choose a number from 1 to 100") @Max(value = 100, message = "Choose a number from 1 to 100") Integer floor,
		@NotNull(message = "Bathrooms count is required") @Min(value = 1, message = "Choose a number from 1 to 10") @Max(value = 10, message = "Choose a number from 1 to 10") Integer bathroomsCount,
		@NotNull(message = "Roommates count is required") @Min(value = 1, message = "Choose a number from 1 to 10") @Max(value = 10, message = "Choose a number from 1 to 10") Integer roommatesCount,
		@NotNull(message = "Choose a gender") @Pattern(regexp = Gender.PATTERN, message = "Choose male or female") String gender,
		@NotNull(message = "Description is required") @Size(min = 10, max = 2000, message = "Description must be 10-2000 characters") String description,
		List<String> rules,
		@Size(max = 500, message = "Other rules must be 500 characters or fewer") String otherRules,
		Boolean taken) {

	public RoomRequest {
		city = Strings.trimToNull(city);
		region = Strings.trimToNull(region);
		approximateAddress = Strings.trimToNull(approximateAddress);
		gender = Strings.trimToNull(gender);
		description = Strings.trimToNull(description);
		rules = rules == null ? List.of() : new ArrayList<>(rules);
		otherRules = Strings.trimToNull(otherRules);
		taken = Boolean.TRUE.equals(taken);
	}

}
