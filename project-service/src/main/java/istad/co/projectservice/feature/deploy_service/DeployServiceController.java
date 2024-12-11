package istad.co.projectservice.feature.deploy_service;

import feign.Response;
import istad.co.projectservice.feature.deploy_service.dto.BuildInfo;
import istad.co.projectservice.feature.deploy_service.dto.CreateDeployServiceRequest;
import istad.co.projectservice.feature.deploy_service.dto.ServiceResponse;
import istad.co.projectservice.utils.CustomPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/v1/deploy-service")
@RequiredArgsConstructor
public class DeployServiceController {

    private final DeployServiceService deployServiceService;
    private final InfraServiceFein infraServiceFein;


    @PostMapping("/create-service")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> createDeployService(@Valid @RequestBody CreateDeployServiceRequest createDeployServiceRequest) {
        deployServiceService.createDeployService(createDeployServiceRequest);
        return ResponseEntity.ok("Deploy Service created successfully");
    }

    @PostMapping("/run-service/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> runDeployService(@PathVariable String name) {
        deployServiceService.buildService(name);
        return ResponseEntity.ok("Deploy Service started successfully");
    }

    @PatchMapping("/disable-service/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> disableService(@PathVariable String name) {
        deployServiceService.disableService(name);
        return ResponseEntity.ok("Deploy Service disabled successfully");
    }

    @PatchMapping("/enable-service/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> enableService(@PathVariable String name) {
        deployServiceService.enableService(name);
        return ResponseEntity.ok("Deploy Service enabled successfully");
    }


    @DeleteMapping("/delete-service/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteService(@PathVariable String name) {
        deployServiceService.deleteService(name);
        return ResponseEntity.ok("Deploy Service deleted successfully");
    }

//    @GetMapping("/stream-log")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<String> streamLog(@RequestParam String jobName, @RequestParam int buildNumber) {
//        return infraServiceFein.streamLogs(jobName, buildNumber);
//    }

    @GetMapping("/stream-log/{jobName}/{buildNumber}")
    public SseEmitter streamLogs(@PathVariable String jobName, @PathVariable int buildNumber) {
        SseEmitter emitter = new SseEmitter();
        Executors.newSingleThreadExecutor().execute(() -> {
            try (Response response = infraServiceFein.streamLog(jobName, buildNumber)) {
                try (var inputStream = response.body().asInputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        emitter.send(new String(buffer, 0, bytesRead));
                    }
                }
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @GetMapping("/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomPage<ServiceResponse>> getServices(@PathVariable String name ,@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(deployServiceService.getServices(name,page, size));
    }

    @GetMapping("/get-service/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceResponse> getService(@PathVariable String name){
        return ResponseEntity.ok(deployServiceService.getServiceByName(name));
    }

    @GetMapping("/get-build-numbers/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BuildInfo>> getBuildNumbers(@PathVariable String name){

        return ResponseEntity.ok(infraServiceFein.getBuildNumbers(name).getBody());
    }



}
