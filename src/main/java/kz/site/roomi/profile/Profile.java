package kz.site.roomi.profile;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import kz.site.roomi.common.Gender;
import kz.site.roomi.user.User;

@Entity
@Table(name = "profiles")
public class Profile {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, length = 50)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Gender gender;

	@Column(length = 32)
	private String telegram;

	@Column(length = 16)
	private String whatsapp;

	@Column(length = 16)
	private String phone;

	@Column(name = "looking_for_roommate", nullable = false)
	private boolean lookingForRoommate;

	@Column(length = 1000)
	private String about;

	@Column(name = "min_price")
	private Integer minPrice;

	@Column(name = "max_price")
	private Integer maxPrice;

	private Boolean smokes;

	@Column(name = "has_pets")
	private Boolean hasPets;

	@Column(name = "is_tidy")
	private Boolean tidy;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Profile() {
	}

	public Profile(User user) {
		this.user = user;
	}

	@PrePersist
	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	public boolean hasContact() {
		return telegram != null || whatsapp != null || phone != null;
	}

	public Long getUserId() {
		return userId;
	}

	public User getUser() {
		return user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getTelegram() {
		return telegram;
	}

	public void setTelegram(String telegram) {
		this.telegram = telegram;
	}

	public String getWhatsapp() {
		return whatsapp;
	}

	public void setWhatsapp(String whatsapp) {
		this.whatsapp = whatsapp;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isLookingForRoommate() {
		return lookingForRoommate;
	}

	public void setLookingForRoommate(boolean lookingForRoommate) {
		this.lookingForRoommate = lookingForRoommate;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public Integer getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(Integer minPrice) {
		this.minPrice = minPrice;
	}

	public Integer getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(Integer maxPrice) {
		this.maxPrice = maxPrice;
	}

	public Boolean getSmokes() {
		return smokes;
	}

	public void setSmokes(Boolean smokes) {
		this.smokes = smokes;
	}

	public Boolean getHasPets() {
		return hasPets;
	}

	public void setHasPets(Boolean hasPets) {
		this.hasPets = hasPets;
	}

	public Boolean getTidy() {
		return tidy;
	}

	public void setTidy(Boolean tidy) {
		this.tidy = tidy;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

}
