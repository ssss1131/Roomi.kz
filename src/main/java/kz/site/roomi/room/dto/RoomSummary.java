package kz.site.roomi.room.dto;

import java.time.Instant;

import kz.site.roomi.room.Room;

public record RoomSummary(
		Long id,
		String city,
		String region,
		int price,
		int roomsCount,
		int floor,
		int roommatesCount,
		String gender,
		Instant publishedAt) {

	public static RoomSummary from(Room room) {
		return new RoomSummary(room.getId(), room.getCity(), room.getRegion(), room.getPrice(), room.getRoomsCount(),
				room.getFloor(), room.getRoommatesCount(), room.getGender().value(), room.getPublishedAt());
	}

}
