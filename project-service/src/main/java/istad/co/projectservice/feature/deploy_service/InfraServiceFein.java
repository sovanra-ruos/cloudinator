package istad.co.projectservice.feature.deploy_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "infrastructure-service")
public interface InfraServiceFein {

    @PostMapping("/api/v1/jenkins/create-job")
    public ResponseEntity<String> createPipeline(@RequestParam String name, @RequestParam String gitUrl, @RequestParam String branch, @RequestParam String subdomain, @RequestParam String token);



}
