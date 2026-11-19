package kz.site.roomi.room;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum RoomRule {

	NO_SMOKING, NO_PETS, KEEP_QUIET, KEEP_TIDY;

	public String value() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static Optional<RoomRule> parse(String value) {
		return Arrays.stream(values()).filter(rule -> rule.value().equals(value)).findFirst();
	}

}
