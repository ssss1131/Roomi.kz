package kz.site.roomi.profile.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Pattern;
import kz.site.roomi.common.Strings;
import kz.site.roomi.profile.Profile;

public record Contacts(
		@Pattern(regexp = "^[A-Za-z0-9_]{5,32}$", message = "Use 5-32 letters, digits or underscores, without @") String telegram,
		@Pattern(regexp = Contacts.PHONE, message = "Use international format, for example +77011234567") String whatsapp,
		@Pattern(regexp = Contacts.PHONE, message = "Use international format, for example +77011234567") String phone) {

	static final String PHONE = "^\\+[1-9][0-9]{7,14}$";

	public Contacts {
		telegram = Strings.trimToNull(telegram);
		whatsapp = Strings.trimToNull(whatsapp);
		phone = Strings.trimToNull(phone);
	}

	public static Contacts empty() {
		return new Contacts(null, null, null);
	}

	public static Contacts from(Profile profile) {
		return new Contacts(profile.getTelegram(), profile.getWhatsapp(), profile.getPhone());
	}

	@JsonIgnore
	public boolean isEmpty() {
		return telegram == null && whatsapp == null && phone == null;
	}

}
