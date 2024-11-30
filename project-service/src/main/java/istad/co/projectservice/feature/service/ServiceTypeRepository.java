package istad.co.projectservice.feature.service;

import istad.co.projectservice.domain.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType,Long> {

    Optional<ServiceType> findByName(String name);
}
