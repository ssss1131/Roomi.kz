package kz.site.roomi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendRequest(
		@NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email) {
}
