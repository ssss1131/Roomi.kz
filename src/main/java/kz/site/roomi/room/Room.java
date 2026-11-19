package kz.site.roomi.room;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import kz.site.roomi.common.Gender;
import kz.site.roomi.user.User;

@Entity
@Table(name = "rooms", indexes = {
		@Index(name = "idx_rooms_owner", columnList = "owner_id"),
		@Index(name = "idx_rooms_published", columnList = "published_at")
})
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@Column(nullable = false, length = 60)
	private String city;

	@Column(nullable = false, length = 60)
	private String region;

	@Column(name = "approximate_address", nullable = false, length = 150)
	private String approximateAddress;

	@Column(nullable = false)
	private int price;

	@Column(name = "rooms_count", nullable = false)
	private int roomsCount;

	@Column(nullable = false)
	private int floor;

	@Column(name = "bathrooms_count", nullable = false)
	private int bathroomsCount;

	@Column(name = "roommates_count", nullable = false)
	private int roommatesCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Gender gender;

	@Column(nullable = false, length = 2000)
	private String description;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "room_rules", joinColumns = @JoinColumn(name = "room_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "rule", nullable = false, length = 20)
	private Set<RoomRule> rules = new HashSet<>();

	@Column(name = "other_rules", length = 500)
	private String otherRules;

	@Column(nullable = false)
	private boolean taken;

	@Column(name = "published_at", nullable = false, updatable = false)
	private Instant publishedAt;

	@Column(name = "views_count", nullable = false)
	private long viewsCount;

	protected Room() {
	}

	public Room(User owner) {
		this.owner = owner;
	}

	@PrePersist
	void onCreate() {
		if (publishedAt == null) {
			publishedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
		}
	}

	public boolean isOwnedBy(Long userId) {
		return owner.getId().equals(userId);
	}

	public Long getId() {
		return id;
	}

	public User getOwner() {
		return owner;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getApproximateAddress() {
		return approximateAddress;
	}

	public void setApproximateAddress(String approximateAddress) {
		this.approximateAddress = approximateAddress;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getRoomsCount() {
		return roomsCount;
	}

	public void setRoomsCount(int roomsCount) {
		this.roomsCount = roomsCount;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}

	public int getBathroomsCount() {
		return bathroomsCount;
	}

	public void setBathroomsCount(int bathroomsCount) {
		this.bathroomsCount = bathroomsCount;
	}

	public int getRoommatesCount() {
		return roommatesCount;
	}

	public void setRoommatesCount(int roommatesCount) {
		this.roommatesCount = roommatesCount;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<RoomRule> getRules() {
		return rules.isEmpty() ? Set.of() : EnumSet.copyOf(rules);
	}

	public void setRules(Set<RoomRule> rules) {
		this.rules.clear();
		this.rules.addAll(rules);
	}

	public String getOtherRules() {
		return otherRules;
	}

	public void setOtherRules(String otherRules) {
		this.otherRules = otherRules;
	}

	public boolean isTaken() {
		return taken;
	}

	public void setTaken(boolean taken) {
		this.taken = taken;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public long getViewsCount() {
		return viewsCount;
	}

}
