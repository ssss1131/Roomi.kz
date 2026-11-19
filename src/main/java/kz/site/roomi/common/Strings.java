package kz.site.roomi.common;

public final class Strings {

	private Strings() {
	}

	public static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.strip();
		return trimmed.isEmpty() ? null : trimmed;
	}

}
