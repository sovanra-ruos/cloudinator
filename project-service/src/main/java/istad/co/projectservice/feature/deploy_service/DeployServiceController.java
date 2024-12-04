package istad.co.projectservice.feature.deploy_service;

import istad.co.projectservice.feature.deploy_service.dto.CreateDeployServiceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


}