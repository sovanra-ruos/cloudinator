package istad.co.projectservice.feature.project;

import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProjectService {

    void createSpringService(String name, String group, String folder, List<String> dependencies, Authentication authentication);

}
