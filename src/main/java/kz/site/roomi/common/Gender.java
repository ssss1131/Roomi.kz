package kz.site.roomi.common;

import java.util.Locale;

public enum Gender {

	MALE, FEMALE;

	public static final String PATTERN = "male|female";

	public String value() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static Gender from(String value) {
		return value == null ? null : valueOf(value.toUpperCase(Locale.ROOT));
	}

	public static String toValue(Gender gender) {
		return gender == null ? null : gender.value();
	}

}
