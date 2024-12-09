package istad.co.projectservice.feature.project;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spring")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create-service")
    public ResponseEntity<String> createSpringService(
            @RequestParam String name,
            @RequestParam String group,
            @RequestParam String folder,
            @RequestParam(required = false) List<String> dependencies,
            Authentication authentication
    ) {
        projectService.createSpringService(name, group, folder, dependencies != null ? dependencies : List.of(),authentication);
        return ResponseEntity.ok("Service created successfully");
    }

}
