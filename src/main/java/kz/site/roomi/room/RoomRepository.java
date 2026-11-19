package kz.site.roomi.room;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {

	List<Room> findAllByTakenFalseOrderByPublishedAtDescIdDesc();

	List<Room> findAllByOwnerIdOrderByPublishedAtDescIdDesc(Long ownerId);

	@Modifying
	@Query("update Room r set r.viewsCount = r.viewsCount + 1 where r.id = :id")
	void incrementViews(@Param("id") Long id);

	@Query("select r.viewsCount from Room r where r.id = :id")
	long findViewsCount(@Param("id") Long id);

}
