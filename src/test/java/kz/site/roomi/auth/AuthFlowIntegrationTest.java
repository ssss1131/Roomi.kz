package kz.site.roomi.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.jayway.jsonpath.JsonPath;
import kz.site.roomi.user.User;
import kz.site.roomi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AuthFlowIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private UserRepository users;

	@Autowired
	private VerificationTokenRepository tokens;

	private MockMvc mvc;

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
		tokens.deleteAll();
		users.deleteAll();
	}

	@Test
	void fullRegistrationConfirmationAndLoginFlow() throws Exception {
		register("Student@Example.com", "password123").andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").isNotEmpty());

		login("student@example.com", "password123").andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Please confirm your email"));

		mvc.perform(get("/api/auth/confirm").param("token", tokenFor("student@example.com")))
				.andExpect(status().isOk());

		String body = login("  STUDENT@example.com ", "password123").andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(greaterThan(0)))
				.andReturn().getResponse().getContentAsString();
		String accessToken = JsonPath.read(body, "$.accessToken");

		mvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("student@example.com"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.createdAt").isNotEmpty());

		assertThat(tokens.count()).isZero();
	}

	@Test
	void duplicateEmailInDifferentCaseIsRejected() throws Exception {
		register("dup@example.com", "password123").andExpect(status().isCreated());

		register("DUP@Example.COM", "password456").andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("This email is already registered"));

		assertThat(users.count()).isEqualTo(1);
	}

	@Test
	void invalidRegistrationReturnsFieldErrors() throws Exception {
		register("not-an-email", "short").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.errors.email").isNotEmpty())
				.andExpect(jsonPath("$.errors.password").isNotEmpty());

		assertThat(users.count()).isZero();
	}

	@Test
	void wrongPasswordAndUnknownEmailReturnUnauthorized() throws Exception {
		register("user@example.com", "password123").andExpect(status().isCreated());
		mvc.perform(get("/api/auth/confirm").param("token", tokenFor("user@example.com")))
				.andExpect(status().isOk());

		login("user@example.com", "wrong-password").andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Wrong email or password"));

		login("nobody@example.com", "password123").andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Wrong email or password"));
	}

	@Test
	void invalidConfirmationTokenReturnsBadRequest() throws Exception {
		mvc.perform(get("/api/auth/confirm").param("token", "definitely-not-a-real-token"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Confirmation link is invalid"));
	}

	@Test
	void resendReplacesOldToken() throws Exception {
		register("resend@example.com", "password123").andExpect(status().isCreated());
		String oldToken = tokenFor("resend@example.com");

		mvc.perform(post("/api/auth/resend").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"Resend@Example.com\"}"))
				.andExpect(status().isOk());

		String newToken = tokenFor("resend@example.com");
		assertThat(newToken).isNotEqualTo(oldToken);

		mvc.perform(get("/api/auth/confirm").param("token", oldToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Confirmation link is invalid"));

		mvc.perform(get("/api/auth/confirm").param("token", newToken))
				.andExpect(status().isOk());
	}

	@Test
	void resendForUnknownEmailStillReturnsOk() throws Exception {
		mvc.perform(post("/api/auth/resend").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"ghost@example.com\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void meWithoutTokenReturnsUnauthorized() throws Exception {
		mvc.perform(get("/api/users/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").isNotEmpty());
	}

	@Test
	void meWithInvalidTokenReturnsUnauthorized() throws Exception {
		mvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt"))
				.andExpect(status().isUnauthorized());
	}

	private ResultActions register(String email, String password) throws Exception {
		return mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(credentials(email, password)));
	}

	private ResultActions login(String email, String password) throws Exception {
		return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(credentials(email, password)));
	}

	private static String credentials(String email, String password) {
		return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
	}

	private String tokenFor(String email) {
		User user = users.findByEmail(email).orElseThrow();
		List<VerificationToken> userTokens = tokens.findAllByUser(user);
		assertThat(userTokens).hasSize(1);
		return userTokens.getFirst().getToken();
	}

}
