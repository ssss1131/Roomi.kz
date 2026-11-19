package kz.site.roomi.room.dto;

import java.time.Instant;

import kz.site.roomi.room.Room;

public record MyRoomSummary(
		Long id,
		String city,
		String region,
		int price,
		int roomsCount,
		int floor,
		int roommatesCount,
		String gender,
		Instant publishedAt,
		boolean taken,
		long viewsCount) {

	public static MyRoomSummary from(Room room) {
		return new MyRoomSummary(room.getId(), room.getCity(), room.getRegion(), room.getPrice(),
				room.getRoomsCount(), room.getFloor(), room.getRoommatesCount(), room.getGender().value(),
				room.getPublishedAt(), room.isTaken(), room.getViewsCount());
	}

}
