package istad.co.projectservice.feature.service;

import istad.co.projectservice.feature.service.dto.ServiceCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/service")
@RequiredArgsConstructor
public class ServiceController {

    private final ProjectsService projectsService;


    @PostMapping("/create")
    public ResponseEntity<?> createService(@RequestBody ServiceCreateRequest serviceCreateRequest) {
        projectsService.createService(serviceCreateRequest);
        return ResponseEntity.ok("Service created successfully");
    }

    @DeleteMapping("/delete/{name}")
    public ResponseEntity<?> deleteService(@PathVariable String name) {
        projectsService.deleteService(name);
        return ResponseEntity.ok("Service deleted successfully");
    }

}
