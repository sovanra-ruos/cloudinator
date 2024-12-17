package istad.co.projectservice.feature.project;


import istad.co.projectservice.feature.deploy_service.InfraServiceFein;
import istad.co.projectservice.feature.deploy_service.dto.BuildInfo;
import istad.co.projectservice.feature.project.dto.ProjectRequest;
import istad.co.projectservice.feature.project.dto.ProjectResponse;
import istad.co.projectservice.utils.CustomPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/spring")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final InfraServiceFein infraServiceFein;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create-service")
    public ResponseEntity<String> createSpringService(
            @RequestBody ProjectRequest projectRequest,
            Authentication authentication
    ) {
        projectService.createSpringService(projectRequest,authentication);
        return ResponseEntity.ok("Service created successfully");
    }

    @GetMapping("/project/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> getSpringService(@PathVariable String name){
        return ResponseEntity.ok(projectService.getSpringService(name));
    }

    @GetMapping("/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomPage<ProjectResponse>> getServices(@PathVariable String name , @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(projectService.getSpringServices(name,page, size));
    }

    @GetMapping("/get-build-numbers/{folder}/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BuildInfo>> getBuildNumbers(@PathVariable String folder,@PathVariable String name){

        return ResponseEntity.ok(infraServiceFein.getBuildNumbersInFolder(folder,name).getBody());
    }

    @PostMapping("/start-build/{folder}/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> startBuild(@PathVariable String folder,@PathVariable String name, @RequestParam String[] serviceName){

        log.info("Starting build for folder {} and name {}",folder,name);
        log.info("Service name {}",serviceName);
        log.info("Service name {}",serviceName);

        infraServiceFein.startBuildInFolder(folder,name,serviceName);
        return ResponseEntity.ok("Build started successfully");
    }

}
