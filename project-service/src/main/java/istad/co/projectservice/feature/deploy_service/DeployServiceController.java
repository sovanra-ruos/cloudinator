package istad.co.projectservice.feature.deploy_service;

import istad.co.projectservice.feature.deploy_service.dto.CreateDeployServiceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/deploy-service")
@RequiredArgsConstructor
public class DeployServiceController {

    private final DeployServiceService deployServiceService;
    private final InfraServiceFein infraServiceFein;


    @GetMapping
    public String getDeployService() {
        return "Deploy Service";
    }

    @PostMapping("/create-service")
    public ResponseEntity<String> createDeployService(@Valid @RequestBody CreateDeployServiceRequest createDeployServiceRequest) {
        deployServiceService.createDeployService(createDeployServiceRequest);
        infraServiceFein.createPipeline(createDeployServiceRequest.name(), createDeployServiceRequest.gitUrl(), createDeployServiceRequest.branch(), createDeployServiceRequest.subdomain(), createDeployServiceRequest.token());
        return ResponseEntity.ok("Deploy Service created successfully");
    }

    @PostMapping("/run-service")
    public ResponseEntity<String> runDeployService(@RequestParam String name) {
        infraServiceFein.startBuild(name);
        return ResponseEntity.ok("Deploy Service started successfully");
    }

    @GetMapping("/stream-log")
    public ResponseEntity<String> streamLog(@RequestParam String jobName, @RequestParam int buildNumber) {
        return infraServiceFein.streamLogs(jobName, buildNumber);
    }

    @GetMapping("/stream-logs")
    public SseEmitter streamLogs(@RequestParam String jobName, @RequestParam int buildNumber) {
        return infraServiceFein.streamLog(jobName, buildNumber);
    }




}
