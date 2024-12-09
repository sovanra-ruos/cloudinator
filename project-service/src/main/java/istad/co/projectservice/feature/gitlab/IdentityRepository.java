package istad.co.projectservice.feature.gitlab;

import istad.co.projectservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdentityRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername (String username);

}
