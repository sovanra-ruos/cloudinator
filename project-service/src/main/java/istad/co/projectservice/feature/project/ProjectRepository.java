package istad.co.projectservice.feature.project;

import istad.co.projectservice.domain.MicroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<MicroService, Long> {

    Optional<MicroService> findByName(String name);

    Page<MicroService> findBySubWorkspace_Name (String name, Pageable pageable);

}
