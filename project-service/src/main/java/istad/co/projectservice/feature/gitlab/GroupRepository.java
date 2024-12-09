package istad.co.projectservice.feature.gitlab;

import istad.co.projectservice.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group,Long> {

    Optional<Group> findByUser_Username (String username);

}
