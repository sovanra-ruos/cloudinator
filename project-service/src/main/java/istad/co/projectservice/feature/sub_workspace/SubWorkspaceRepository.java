package istad.co.projectservice.feature.sub_workspace;

import istad.co.projectservice.domain.SubWorkspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubWorkspaceRepository extends JpaRepository<SubWorkspace, Long> {

    Optional<SubWorkspace> findByName(String name);

    Page<SubWorkspace> findSubWorkspaceByWorkspace_Name(String name, Pageable pageable);

}
