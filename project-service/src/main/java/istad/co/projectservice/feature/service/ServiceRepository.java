package istad.co.projectservice.feature.service;

import istad.co.projectservice.domain.ProjectService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ProjectService,Long> {

    Optional<ProjectService> findByName(String name);

}
