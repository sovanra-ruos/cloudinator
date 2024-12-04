package istad.co.infrastructureservice.feature.jenkins;

import istad.co.infrastructureservice.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job,Long> {
}
