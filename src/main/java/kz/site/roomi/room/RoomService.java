package kz.site.roomi.room;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import kz.site.roomi.common.ApiException;
import kz.site.roomi.common.Gender;
import kz.site.roomi.profile.Profile;
import kz.site.roomi.profile.ProfileRepository;
import kz.site.roomi.profile.dto.PublicProfile;
import kz.site.roomi.room.dto.MyRoomSummary;
import kz.site.roomi.room.dto.RoomDetails;
import kz.site.roomi.room.dto.RoomRequest;
import kz.site.roomi.room.dto.RoomSummary;
import kz.site.roomi.user.User;
import kz.site.roomi.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {

	private static final String NOT_FOUND = "Room not found";

	private final RoomRepository rooms;
	private final UserRepository users;
	private final ProfileRepository profiles;

	public RoomService(RoomRepository rooms, UserRepository users, ProfileRepository profiles) {
		this.rooms = rooms;
		this.users = users;
		this.profiles = profiles;
	}

	@Transactional(readOnly = true)
	public List<RoomSummary> feed() {
		return rooms.findAllByTakenFalseOrderByPublishedAtDescIdDesc().stream().map(RoomSummary::from).toList();
	}

	@Transactional(readOnly = true)
	public List<MyRoomSummary> mine(Long userId) {
		return rooms.findAllByOwnerIdOrderByPublishedAtDescIdDesc(userId).stream().map(MyRoomSummary::from).toList();
	}

	@Transactional
	public RoomDetails get(Long roomId, Long callerId) {
		Room room = rooms.findById(roomId).orElseThrow(() -> ApiException.notFound(NOT_FOUND));
		boolean isOwner = room.isOwnedBy(callerId);
		if (room.isTaken() && !isOwner) {
			throw ApiException.notFound(NOT_FOUND);
		}
		long views = room.getViewsCount();
		if (!isOwner) {
			rooms.incrementViews(roomId);
			views = rooms.findViewsCount(roomId);
		}
		return RoomDetails.from(room, views, isOwner, ownerProfile(room));
	}

	@Transactional
	public RoomDetails create(Long userId, RoomRequest request) {
		Set<RoomRule> rules = parseRules(request.rules());
		Optional<Profile> profile = profiles.findById(userId);
		if (profile.isEmpty() || profile.get().getName() == null || !profile.get().hasContact()) {
			throw new ApiException(HttpStatus.CONFLICT, "Complete your profile before publishing a room");
		}
		User owner = users.findById(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required"));
		Room room = new Room(owner);
		apply(room, request, rules);
		room = rooms.save(room);
		return RoomDetails.from(room, room.getViewsCount(), true, PublicProfile.from(profile.get()));
	}

	@Transactional
	public RoomDetails update(Long roomId, Long userId, RoomRequest request) {
		Room room = ownedRoom(roomId, userId);
		apply(room, request, parseRules(request.rules()));
		room = rooms.saveAndFlush(room);
		return RoomDetails.from(room, room.getViewsCount(), true, ownerProfile(room));
	}

	@Transactional
	public void delete(Long roomId, Long userId) {
		rooms.delete(ownedRoom(roomId, userId));
	}

	private Room ownedRoom(Long roomId, Long userId) {
		Room room = rooms.findById(roomId).orElseThrow(() -> ApiException.notFound(NOT_FOUND));
		if (!room.isOwnedBy(userId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "You are not the owner of this room");
		}
		return room;
	}

	private PublicProfile ownerProfile(Room room) {
		Long ownerId = room.getOwner().getId();
		return profiles.findById(ownerId).map(PublicProfile::from).orElseGet(() -> PublicProfile.withoutProfile(ownerId));
	}

	private static void apply(Room room, RoomRequest request, Set<RoomRule> rules) {
		room.setCity(request.city());
		room.setRegion(request.region());
		room.setApproximateAddress(request.approximateAddress());
		room.setPrice(request.price());
		room.setRoomsCount(request.roomsCount());
		room.setFloor(request.floor());
		room.setBathroomsCount(request.bathroomsCount());
		room.setRoommatesCount(request.roommatesCount());
		room.setGender(Gender.from(request.gender()));
		room.setDescription(request.description());
		room.setRules(rules);
		room.setOtherRules(request.otherRules());
		room.setTaken(request.taken());
	}

	private static Set<RoomRule> parseRules(List<String> values) {
		Set<RoomRule> rules = EnumSet.noneOf(RoomRule.class);
		Set<String> seen = new HashSet<>();
		for (String value : values) {
			Optional<RoomRule> rule = RoomRule.parse(value);
			if (rule.isEmpty()) {
				throw ApiException.validation(Map.of("rules", "Choose valid house rules"));
			}
			if (!seen.add(value)) {
				throw ApiException.validation(Map.of("rules", "Choose each house rule only once"));
			}
			rules.add(rule.get());
		}
		return rules;
	}

}
