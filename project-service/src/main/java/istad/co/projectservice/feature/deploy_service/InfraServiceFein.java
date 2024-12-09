package istad.co.projectservice.feature.deploy_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@FeignClient(name = "infrastructure-service")
public interface InfraServiceFein {

    @PostMapping("/api/v1/jenkins/create-job")
    public ResponseEntity<String> createPipeline(@RequestParam String name, @RequestParam String gitUrl, @RequestParam String branch, @RequestParam String subdomain, @RequestParam String token);

    @PostMapping("/api/v1/jenkins/start-build")
    public ResponseEntity<String> startBuild(@RequestParam String name);

    @GetMapping("/api/v1/jenkins/stream-log")
    public SseEmitter streamLog(@RequestParam String jobName, @RequestParam int buildNumber);

    @GetMapping("/api/v1/jenkins/stream-logs")
    public ResponseEntity<String> streamLogs(@RequestParam String jobName, @RequestParam int buildNumber);

    @PostMapping("/api/v1/jenkins/create-folder")
    public ResponseEntity<String> createFolder(@RequestParam String folderName);

    @PostMapping("/api/v1/jenkins/create-main-pipeline")
    public ResponseEntity<String> createMainPipeLine(@RequestParam String folderName);

}
