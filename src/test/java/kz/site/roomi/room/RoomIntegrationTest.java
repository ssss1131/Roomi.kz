package kz.site.roomi.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import kz.site.roomi.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class RoomIntegrationTest extends IntegrationTestSupport {

	private static String roomJson(String city, int price, boolean taken) {
		return """
				{"city":"  %s ","region":"Almaly","approximateAddress":"Near the supermarket on 12th street",
				"price":%d,"roomsCount":3,"floor":4,"bathroomsCount":1,"roommatesCount":2,"gender":"male",
				"description":"Spacious flat, two roommates wanted.","rules":["keep_tidy","no_smoking"],
				"otherRules":"Quiet after 11 pm","taken":%s}
				""".formatted(city, price, taken);
	}

	private Long createRoom(String token, String city, boolean taken) throws Exception {
		String body = perform(post("/api/rooms"), token, roomJson(city, 45000, taken))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

	@Test
	void createRoomReturnsDetails() throws Exception {
		String token = loginWithProfile("owner@example.com", "Owner", false);

		perform(post("/api/rooms"), token, roomJson("Almaty", 45000, false))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.city").value("Almaty"))
				.andExpect(jsonPath("$.region").value("Almaly"))
				.andExpect(jsonPath("$.approximateAddress").value("Near the supermarket on 12th street"))
				.andExpect(jsonPath("$.price").value(45000))
				.andExpect(jsonPath("$.roomsCount").value(3))
				.andExpect(jsonPath("$.floor").value(4))
				.andExpect(jsonPath("$.bathroomsCount").value(1))
				.andExpect(jsonPath("$.roommatesCount").value(2))
				.andExpect(jsonPath("$.gender").value("male"))
				.andExpect(jsonPath("$.rules", contains("no_smoking", "keep_tidy")))
				.andExpect(jsonPath("$.otherRules").value("Quiet after 11 pm"))
				.andExpect(jsonPath("$.taken").value(false))
				.andExpect(jsonPath("$.publishedAt").isNotEmpty())
				.andExpect(jsonPath("$.viewsCount").value(0))
				.andExpect(jsonPath("$.isOwner").value(true))
				.andExpect(jsonPath("$.owner.userId").value(userId("owner@example.com")))
				.andExpect(jsonPath("$.owner.name").value("Owner"))
				.andExpect(jsonPath("$.owner.contacts.telegram").value("user_owner"))
				.andExpect(jsonPath("$.owner.email").doesNotExist());
	}

	@Test
	void createRoomWithoutProfileReturns409() throws Exception {
		String token = loginAsNewUser("noprofile@example.com");

		perform(post("/api/rooms"), token, roomJson("Almaty", 45000, false))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").isNotEmpty());

		assertThat(rooms.count()).isZero();
	}

	@Test
	void invalidRoomReturnsFieldErrors() throws Exception {
		String token = loginWithProfile("owner@example.com", "Owner", false);

		perform(post("/api/rooms"), token, """
				{"city":"A","region":" ","approximateAddress":"ab","price":0,"roomsCount":21,"floor":0,
				"bathroomsCount":11,"roommatesCount":0,"gender":"any","description":"short","otherRules":null}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.city").isNotEmpty())
				.andExpect(jsonPath("$.errors.region").isNotEmpty())
				.andExpect(jsonPath("$.errors.approximateAddress").isNotEmpty())
				.andExpect(jsonPath("$.errors.price").isNotEmpty())
				.andExpect(jsonPath("$.errors.roomsCount").isNotEmpty())
				.andExpect(jsonPath("$.errors.floor").isNotEmpty())
				.andExpect(jsonPath("$.errors.bathroomsCount").isNotEmpty())
				.andExpect(jsonPath("$.errors.roommatesCount").isNotEmpty())
				.andExpect(jsonPath("$.errors.gender").isNotEmpty())
				.andExpect(jsonPath("$.errors.description").isNotEmpty());

		perform(post("/api/rooms"), token, roomJson("Almaty", 45000, false).replace("\"keep_tidy\"", "\"no_smoking\""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.rules").isNotEmpty());

		perform(post("/api/rooms"), token, roomJson("Almaty", 45000, false).replace("keep_tidy", "no_parties"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.rules").isNotEmpty());

		perform(post("/api/rooms"), token, roomJson("Almaty", 45000, false).replace("45000", "45000.5"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.price").isNotEmpty());

		assertThat(rooms.count()).isZero();
	}

	@Test
	void feedIsPublicAndShowsOnlyActiveRoomsNewestFirst() throws Exception {
		String token = loginWithProfile("owner@example.com", "Owner", false);
		createRoom(token, "Older", false);
		createRoom(token, "Taken", true);
		createRoom(token, "Newer", false);

		mvc.perform(get("/api/rooms"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(2)))
				.andExpect(jsonPath("$.items[*].city", contains("Newer", "Older")))
				.andExpect(jsonPath("$.items[0].price").value(45000))
				.andExpect(jsonPath("$.items[0].gender").value("male"))
				.andExpect(jsonPath("$.items[0].publishedAt").isNotEmpty())
				.andExpect(jsonPath("$.items[0].owner").doesNotExist())
				.andExpect(jsonPath("$.items[0].viewsCount").doesNotExist());
	}

	@Test
	void detailsRequireTokenAndCountViewsOnlyForOthers() throws Exception {
		String owner = loginWithProfile("owner@example.com", "Owner", false);
		String guest = loginAsNewUser("guest@example.com");
		Long id = createRoom(owner, "Almaty", false);

		mvc.perform(get("/api/rooms/" + id)).andExpect(status().isUnauthorized());

		perform(get("/api/rooms/" + id), guest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.viewsCount").value(1))
				.andExpect(jsonPath("$.isOwner").value(false))
				.andExpect(jsonPath("$.owner.name").value("Owner"));
		perform(get("/api/rooms/" + id), guest)
				.andExpect(jsonPath("$.viewsCount").value(2));
		perform(get("/api/rooms/" + id), owner)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.viewsCount").value(2))
				.andExpect(jsonPath("$.isOwner").value(true));
		mvc.perform(get("/api/rooms")).andExpect(status().isOk());

		perform(get("/api/users/me/rooms"), owner)
				.andExpect(jsonPath("$.items[0].viewsCount").value(2));
		perform(get("/api/rooms/999999"), guest).andExpect(status().isNotFound());
	}

	@Test
	void takenRoomIsHiddenFromOthersButVisibleToOwner() throws Exception {
		String owner = loginWithProfile("owner@example.com", "Owner", false);
		String other = loginWithProfile("other@example.com", "Other", false);
		Long id = createRoom(owner, "Almaty", true);

		perform(get("/api/rooms/" + id), other).andExpect(status().isNotFound());
		perform(get("/api/rooms/" + id), owner)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.taken").value(true))
				.andExpect(jsonPath("$.viewsCount").value(0));

		perform(put("/api/rooms/" + id), owner, roomJson("Almaty", 45000, false)).andExpect(status().isOk());
		perform(get("/api/rooms/" + id), other).andExpect(status().isOk());
	}

	@Test
	void ownerCanUpdateRoomAndPublishedAtStays() throws Exception {
		String owner = loginWithProfile("owner@example.com", "Owner", false);
		String created = perform(post("/api/rooms"), owner, roomJson("Almaty", 45000, false))
				.andReturn().getResponse().getContentAsString();
		Long id = ((Number) JsonPath.read(created, "$.id")).longValue();
		String publishedAt = JsonPath.read(created, "$.publishedAt");

		perform(put("/api/rooms/" + id), owner, """
				{"city":"Astana","region":"Yesil","approximateAddress":"Near the park","price":60000,"roomsCount":2,
				"floor":9,"bathroomsCount":2,"roommatesCount":1,"gender":"female","description":"Updated description here"}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.city").value("Astana"))
				.andExpect(jsonPath("$.price").value(60000))
				.andExpect(jsonPath("$.gender").value("female"))
				.andExpect(jsonPath("$.rules", hasSize(0)))
				.andExpect(jsonPath("$.otherRules").value(nullValue()))
				.andExpect(jsonPath("$.taken").value(false))
				.andExpect(jsonPath("$.publishedAt").value(publishedAt));
	}

	@Test
	void onlyOwnerCanUpdateOrDelete() throws Exception {
		String owner = loginWithProfile("owner@example.com", "Owner", false);
		String other = loginWithProfile("other@example.com", "Other", false);
		Long id = createRoom(owner, "Almaty", false);

		perform(put("/api/rooms/" + id), other, roomJson("Hacked", 1, false)).andExpect(status().isForbidden());
		perform(delete("/api/rooms/" + id), other).andExpect(status().isForbidden());
		perform(put("/api/rooms/999999"), owner, roomJson("Almaty", 45000, false)).andExpect(status().isNotFound());
		perform(delete("/api/rooms/999999"), owner).andExpect(status().isNotFound());
		mvc.perform(delete("/api/rooms/" + id)).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(roomJson("Almaty", 1, false)))
				.andExpect(status().isUnauthorized());

		perform(delete("/api/rooms/" + id), owner).andExpect(status().isNoContent());
		assertThat(rooms.count()).isZero();
		perform(get("/api/rooms/" + id), owner).andExpect(status().isNotFound());
	}

	@Test
	void myRoomsListsOwnRoomsIncludingTaken() throws Exception {
		String owner = loginWithProfile("owner@example.com", "Owner", false);
		String other = loginWithProfile("other@example.com", "Other", false);
		createRoom(owner, "First", false);
		createRoom(owner, "Second", true);
		createRoom(other, "Foreign", false);

		perform(get("/api/users/me/rooms"), owner)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(2)))
				.andExpect(jsonPath("$.items[*].city", contains("Second", "First")))
				.andExpect(jsonPath("$.items[0].taken").value(true))
				.andExpect(jsonPath("$.items[0].viewsCount").value(0));

		mvc.perform(get("/api/users/me/rooms")).andExpect(status().isUnauthorized());
	}

}
