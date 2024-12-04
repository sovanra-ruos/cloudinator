package istad.co.infrastructureservice.feature.jenkins;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jenkins")
public class JenkinsController {

    private final JenkinsService jenkinsService;

    @PostMapping("/create-job")
    public ResponseEntity<String> createPipeline(@RequestParam String name, @RequestParam String gitUrl, @RequestParam String branch, @RequestParam String subdomain, @RequestParam String token) {

        try {
            jenkinsService.createMonolithicJob(name, gitUrl, branch, subdomain, token);
            return ResponseEntity.ok("Job created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create job");
        }
    }

}
