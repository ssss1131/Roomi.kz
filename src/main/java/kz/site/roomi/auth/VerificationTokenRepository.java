package kz.site.roomi.auth;

import java.util.List;
import java.util.Optional;

import kz.site.roomi.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

	Optional<VerificationToken> findByToken(String token);

	List<VerificationToken> findAllByUser(User user);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from VerificationToken t where t.user = :user")
	void deleteAllByUser(@Param("user") User user);

}
