package kz.site.roomi.room;

import jakarta.validation.Valid;
import kz.site.roomi.common.CurrentUser;
import kz.site.roomi.common.ItemsResponse;
import kz.site.roomi.room.dto.MyRoomSummary;
import kz.site.roomi.room.dto.RoomDetails;
import kz.site.roomi.room.dto.RoomRequest;
import kz.site.roomi.room.dto.RoomSummary;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoomController {

	private final RoomService roomService;

	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}

	@GetMapping("/api/rooms")
	public ItemsResponse<RoomSummary> feed() {
		return new ItemsResponse<>(roomService.feed());
	}

	@GetMapping("/api/rooms/{id}")
	public RoomDetails get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		return roomService.get(id, CurrentUser.id(jwt));
	}

	@PostMapping("/api/rooms")
	@ResponseStatus(HttpStatus.CREATED)
	public RoomDetails create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RoomRequest request) {
		return roomService.create(CurrentUser.id(jwt), request);
	}

	@PutMapping("/api/rooms/{id}")
	public RoomDetails update(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody RoomRequest request) {
		return roomService.update(id, CurrentUser.id(jwt), request);
	}

	@DeleteMapping("/api/rooms/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		roomService.delete(id, CurrentUser.id(jwt));
	}

	@GetMapping("/api/users/me/rooms")
	public ItemsResponse<MyRoomSummary> mine(@AuthenticationPrincipal Jwt jwt) {
		return new ItemsResponse<>(roomService.mine(CurrentUser.id(jwt)));
	}

}
