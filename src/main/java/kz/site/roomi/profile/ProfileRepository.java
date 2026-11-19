package kz.site.roomi.profile;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

	@Query("select p from Profile p where p.lookingForRoommate = true and p.userId <> :userId order by p.userId desc")
	List<Profile> findRoommatesExcluding(@Param("userId") Long userId);

}
