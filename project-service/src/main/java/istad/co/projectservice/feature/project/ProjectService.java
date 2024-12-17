package istad.co.projectservice.feature.project;

import istad.co.projectservice.feature.project.dto.ProjectRequest;
import istad.co.projectservice.feature.project.dto.ProjectResponse;
import istad.co.projectservice.utils.CustomPage;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProjectService {

    void createSpringService(ProjectRequest projectRequest, Authentication authentication);

    CustomPage<ProjectResponse> getSpringServices(String name,int page, int size);

    ProjectResponse getSpringService(String name);

}
