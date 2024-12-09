package istad.co.infrastructureservice.feature.jenkins;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

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

    @PostMapping("/start-build")
    public ResponseEntity<String> startBuild(@RequestParam String name) {

        try {
            int buildNumber = jenkinsService.startMonolithicBuild(name);
            return ResponseEntity.ok("Build started successfully with build number: " + buildNumber);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to start build");
        }
    }

    @GetMapping("/stream-logs")
    public ResponseEntity<String> streamLogs(@RequestParam String jobName, @RequestParam int buildNumber) {

        try {
            jenkinsService.streamBuildLog(jobName, buildNumber);
            return ResponseEntity.ok("Log streaming started successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to stream logs");
        }
    }

    @GetMapping("/stream-log/{jobName}/{buildNumber}")
    public SseEmitter streamLog(@PathVariable String jobName, @PathVariable int buildNumber) throws IOException, InterruptedException {
        return jenkinsService.streamLog(jobName, buildNumber);
    }

    @PostMapping("/create-folder")
    public ResponseEntity<String> createFolder(@RequestParam String folderName) {
        try {
            jenkinsService.createFolder(folderName);
            return ResponseEntity.ok("Folder created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create folder");
        }
    }


    @PostMapping("/deploy-database-service")
    public ResponseEntity<String> deployDatabaseService(@RequestParam String dbName, @RequestParam String dbUser, @RequestParam String dbPassword, @RequestParam String dbType) {
        try {
            jenkinsService.deployDatabaseService(dbName, dbUser, dbPassword, dbType);
            return ResponseEntity.ok("Database service created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create database service");
        }
    }

    @PatchMapping("/disable-job")
    public ResponseEntity<String> disableJob(@RequestParam String jobName) {
        try {
            jenkinsService.disableJob(jobName);
            return ResponseEntity.ok("Job disabled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to disable job");
        }
    }

    @PatchMapping("/enable-job")
    public ResponseEntity<String> enableJob(@RequestParam String jobName) {
        try {
            jenkinsService.enableJob(jobName);
            return ResponseEntity.ok("Job enabled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to enable job");
        }
    }

    @DeleteMapping("/delete-job")
    public ResponseEntity<String> deleteJob(@RequestParam String jobName) {
        try {
            jenkinsService.deleteJob(jobName);
            return ResponseEntity.ok("Job deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete job");
        }
    }

    @PutMapping("/update-job")
    public ResponseEntity<String > updateJob(@RequestParam String folderName, @RequestParam String jobName, @RequestParam String serviceName) {

        try {
            jenkinsService.updateJobPipeline(folderName, jobName, serviceName);
            return ResponseEntity.ok("Job has been updated");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Fail to update the job");
        }

    }



}
