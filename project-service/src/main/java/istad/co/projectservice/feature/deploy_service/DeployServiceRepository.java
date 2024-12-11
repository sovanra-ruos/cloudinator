package istad.co.projectservice.feature.deploy_service;

import istad.co.projectservice.domain.DeployService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeployServiceRepository extends JpaRepository<DeployService,Long> {

    Optional<DeployService> findByName(String name);

    Page<DeployService> findDeployServiceByWorkspace_Name (String name, Pageable pageable);
}
