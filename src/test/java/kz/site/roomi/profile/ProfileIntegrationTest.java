package kz.site.roomi.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kz.site.roomi.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ProfileIntegrationTest extends IntegrationTestSupport {

	@Test
	void saveProfileReturnsUserWithProfileAndTrimsStrings() throws Exception {
		String token = loginAsNewUser("alex@example.com");

		perform(put("/api/users/me"), token, """
				{"id":999,"email":"hacker@example.com","name":"  Alex  ","gender":"male",
				"contacts":{"telegram":"alex_user","whatsapp":"  ","phone":"+77011234567"},
				"lookingForRoommate":true,"about":"   ","minPrice":30000,"maxPrice":50000,
				"habits":{"smokes":false,"isTidy":true}}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId("alex@example.com")))
				.andExpect(jsonPath("$.email").value("alex@example.com"))
				.andExpect(jsonPath("$.createdAt").isNotEmpty())
				.andExpect(jsonPath("$.profile.name").value("Alex"))
				.andExpect(jsonPath("$.profile.gender").value("male"))
				.andExpect(jsonPath("$.profile.contacts.telegram").value("alex_user"))
				.andExpect(jsonPath("$.profile.contacts.whatsapp").value(nullValue()))
				.andExpect(jsonPath("$.profile.contacts.phone").value("+77011234567"))
				.andExpect(jsonPath("$.profile.lookingForRoommate").value(true))
				.andExpect(jsonPath("$.profile.about").value(nullValue()))
				.andExpect(jsonPath("$.profile.minPrice").value(30000))
				.andExpect(jsonPath("$.profile.maxPrice").value(50000))
				.andExpect(jsonPath("$.profile.habits.smokes").value(false))
				.andExpect(jsonPath("$.profile.habits.hasPets").hasJsonPath())
				.andExpect(jsonPath("$.profile.habits.hasPets").value(nullValue()))
				.andExpect(jsonPath("$.profile.habits.isTidy").value(true));

		perform(get("/api/users/me"), token)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.profile.name").value("Alex"));
	}

	@Test
	void putReplacesWholeProfile() throws Exception {
		String token = loginWithProfile("maria@example.com", "Maria", true);

		perform(put("/api/users/me"), token, """
				{"name":"Maria","gender":"female","contacts":{"phone":"+77011234567"}}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.profile.contacts.telegram").value(nullValue()))
				.andExpect(jsonPath("$.profile.lookingForRoommate").value(false))
				.andExpect(jsonPath("$.profile.about").value(nullValue()))
				.andExpect(jsonPath("$.profile.minPrice").value(nullValue()))
				.andExpect(jsonPath("$.profile.habits.isTidy").value(nullValue()));
	}

	@Test
	void invalidProfileReturnsFieldErrors() throws Exception {
		String token = loginAsNewUser("bad@example.com");

		perform(put("/api/users/me"), token, """
				{"name":"A","gender":"other","contacts":{"telegram":"@ab","whatsapp":"87011234567"},
				"about":null,"minPrice":0,"maxPrice":2000000}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errors.name").isNotEmpty())
				.andExpect(jsonPath("$.errors.gender").isNotEmpty())
				.andExpect(jsonPath("$.errors['contacts.telegram']").isNotEmpty())
				.andExpect(jsonPath("$.errors['contacts.whatsapp']").isNotEmpty())
				.andExpect(jsonPath("$.errors.minPrice").isNotEmpty())
				.andExpect(jsonPath("$.errors.maxPrice").isNotEmpty());

		assertThat(profiles.count()).isZero();
	}

	@Test
	void profileNeedsContactAndConsistentBudget() throws Exception {
		String token = loginAsNewUser("budget@example.com");

		perform(put("/api/users/me"), token, """
				{"name":"Budget","gender":"male","contacts":{"telegram":null,"whatsapp":"","phone":null},"minPrice":5000}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.contacts").isNotEmpty())
				.andExpect(jsonPath("$.errors.minPrice").isNotEmpty())
				.andExpect(jsonPath("$.errors.maxPrice").isNotEmpty());

		perform(put("/api/users/me"), token, """
				{"name":"Budget","gender":"male","contacts":{"telegram":"budget"},"minPrice":50000,"maxPrice":40000}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.maxPrice").isNotEmpty());
	}

	@Test
	void wrongJsonTypeReturnsFieldError() throws Exception {
		String token = loginAsNewUser("types@example.com");

		perform(put("/api/users/me"), token, """
				{"name":"Types","gender":"male","contacts":{"telegram":"typesuser"},"minPrice":"abc","maxPrice":10}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.minPrice").isNotEmpty());
	}

	@Test
	void profileEndpointsRequireToken() throws Exception {
		mvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON).content(profileJson("Anon", true)))
				.andExpect(status().isUnauthorized());
		mvc.perform(get("/api/roommates")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/roommates/1")).andExpect(status().isUnauthorized());
	}

	@Test
	void roommatesListsOnlyLookingUsersExceptCallerNewestFirst() throws Exception {
		String me = loginWithProfile("me@example.com", "Me", true);
		loginWithProfile("first@example.com", "First", true);
		loginWithProfile("hidden@example.com", "Hidden", false);
		loginAsNewUser("noprofile@example.com");
		loginWithProfile("second@example.com", "Second", true);

		perform(get("/api/roommates"), me)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(2)))
				.andExpect(jsonPath("$.items[*].name", contains("Second", "First")))
				.andExpect(jsonPath("$.items[0].userId").value(userId("second@example.com")))
				.andExpect(jsonPath("$.items[0].gender").value("female"))
				.andExpect(jsonPath("$.items[0].about").value("Student, quiet"))
				.andExpect(jsonPath("$.items[0].minPrice").value(30000))
				.andExpect(jsonPath("$.items[0].email").doesNotExist())
				.andExpect(jsonPath("$.items[0].contacts").doesNotExist());
	}

	@Test
	void roommateDetailsReturnsPublicProfileOr404() throws Exception {
		String me = loginWithProfile("viewer@example.com", "Viewer", false);
		loginWithProfile("looking@example.com", "Looking", true);
		loginWithProfile("notlooking@example.com", "Notlooking", false);

		perform(get("/api/roommates/" + userId("looking@example.com")), me)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(userId("looking@example.com")))
				.andExpect(jsonPath("$.name").value("Looking"))
				.andExpect(jsonPath("$.contacts.telegram").value("user_looking"))
				.andExpect(jsonPath("$.habits.isTidy").value(true))
				.andExpect(jsonPath("$.email").doesNotExist())
				.andExpect(jsonPath("$.createdAt").doesNotExist());

		perform(get("/api/roommates/" + userId("notlooking@example.com")), me).andExpect(status().isNotFound());
		perform(get("/api/roommates/999999"), me).andExpect(status().isNotFound());
	}

}
