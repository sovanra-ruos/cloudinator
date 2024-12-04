package istad.co.projectservice.feature.deploy_service;

import istad.co.projectservice.domain.DeployService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeployServiceRepository extends JpaRepository<DeployService,Long> {
}
