package kz.site.roomi;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import kz.site.roomi.auth.VerificationTokenRepository;
import kz.site.roomi.profile.ProfileRepository;
import kz.site.roomi.room.RoomRepository;
import kz.site.roomi.user.User;
import kz.site.roomi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public abstract class IntegrationTestSupport {

	protected static final String PASSWORD = "password123";

	@Autowired
	protected WebApplicationContext context;

	@Autowired
	protected UserRepository users;

	@Autowired
	protected VerificationTokenRepository tokens;

	@Autowired
	protected ProfileRepository profiles;

	@Autowired
	protected RoomRepository rooms;

	protected MockMvc mvc;

	@BeforeEach
	void resetState() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
		rooms.deleteAll();
		profiles.deleteAll();
		tokens.deleteAll();
		users.deleteAll();
	}

	protected String loginAsNewUser(String email) throws Exception {
		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
				.andExpect(status().isCreated());
		User user = users.findByEmail(email).orElseThrow();
		String confirmToken = tokens.findAllByUser(user).getFirst().getToken();
		mvc.perform(get("/api/auth/confirm").param("token", confirmToken)).andExpect(status().isOk());
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.accessToken");
	}

	protected Long userId(String email) {
		return users.findByEmail(email).orElseThrow().getId();
	}

	protected String loginWithProfile(String email, String name, boolean lookingForRoommate) throws Exception {
		String token = loginAsNewUser(email);
		perform(put("/api/users/me"), token, profileJson(name, lookingForRoommate)).andExpect(status().isOk());
		return token;
	}

	protected static String profileJson(String name, boolean lookingForRoommate) {
		return """
				{"name":"%s","gender":"female","contacts":{"telegram":"user_%s","whatsapp":null,"phone":null},
				"lookingForRoommate":%s,"about":"Student, quiet","minPrice":30000,"maxPrice":50000,
				"habits":{"smokes":false,"hasPets":null,"isTidy":true}}
				""".formatted(name, name.toLowerCase().replace(' ', '_'), lookingForRoommate);
	}

	protected ResultActions perform(MockHttpServletRequestBuilder request, String token) throws Exception {
		return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
	}

	protected ResultActions perform(MockHttpServletRequestBuilder request, String token, String json) throws Exception {
		return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(json));
	}

}
