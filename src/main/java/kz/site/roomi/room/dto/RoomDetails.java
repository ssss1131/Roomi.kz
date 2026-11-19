package kz.site.roomi.room.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import kz.site.roomi.profile.dto.PublicProfile;
import kz.site.roomi.room.Room;
import kz.site.roomi.room.RoomRule;

public record RoomDetails(
		Long id,
		String city,
		String region,
		String approximateAddress,
		int price,
		int roomsCount,
		int floor,
		int bathroomsCount,
		int roommatesCount,
		String gender,
		String description,
		List<String> rules,
		String otherRules,
		boolean taken,
		Instant publishedAt,
		long viewsCount,
		@JsonProperty("isOwner") boolean isOwner,
		PublicProfile owner) {

	public static RoomDetails from(Room room, long viewsCount, boolean isOwner, PublicProfile owner) {
		return new RoomDetails(room.getId(), room.getCity(), room.getRegion(), room.getApproximateAddress(),
				room.getPrice(), room.getRoomsCount(), room.getFloor(), room.getBathroomsCount(),
				room.getRoommatesCount(), room.getGender().value(), room.getDescription(),
				room.getRules().stream().map(RoomRule::value).toList(), room.getOtherRules(), room.isTaken(),
				room.getPublishedAt(), viewsCount, isOwner, owner);
	}

}
