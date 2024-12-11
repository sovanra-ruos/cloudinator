package istad.co.projectservice.feature.deploy_service;

import feign.Response;
import istad.co.projectservice.feature.deploy_service.dto.BuildInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@FeignClient(name = "infrastructure-service")
public interface InfraServiceFein {

    @PostMapping("/api/v1/jenkins/create-job")
    public ResponseEntity<String> createPipeline(@RequestParam String name, @RequestParam String gitUrl, @RequestParam String branch, @RequestParam String subdomain, @RequestParam String token);

    @PostMapping("/api/v1/jenkins/start-build")
    public ResponseEntity<String> startBuild(@RequestParam String name);

    @GetMapping("/api/v1/jenkins/stream-logs")
    public ResponseEntity<String> streamLogs(@RequestParam String jobName, @RequestParam int buildNumber);

    @PostMapping("/api/v1/jenkins/create-folder")
    public ResponseEntity<String> createFolder(@RequestParam String folderName);

    @PostMapping("/api/v1/jenkins/create-main-pipeline")
    public ResponseEntity<String> createMainPipeLine(@RequestParam String folderName);

    @PostMapping("/api/v1/jenkins/deploy-database-service")
    public ResponseEntity<String> deployDatabaseService(@RequestParam String dbName, @RequestParam String dbUser, @RequestParam String dbPassword, @RequestParam String dbType);

    @PatchMapping("/api/v1/jenkins/disable-job")
    public ResponseEntity<String> disableJob(@RequestParam String jobName);

    @PatchMapping("/api/v1/jenkins/enable-job")
    public ResponseEntity<String> enableJob(@RequestParam String jobName);

    @DeleteMapping("/api/v1/jenkins/delete-job")
    public ResponseEntity<String> deleteJob(@RequestParam String jobName);

    @PostMapping("/api/v1/jenkins/update-job")
    public ResponseEntity<String > updateJob(@RequestParam String folderName, @RequestParam String jobName, @RequestParam String serviceName);

    @GetMapping("/api/v1/jenkins/get-build-numbers")
    public ResponseEntity<List<BuildInfo>> getBuildNumbers(@RequestParam String jobName);

    @GetMapping(value = "/api/v1/jenkins/stream-log/{jobName}/{buildNumber}", produces = "text/event-stream")
    Response streamLog(@PathVariable("jobName") String jobName, @PathVariable("buildNumber") int buildNumber);



}
