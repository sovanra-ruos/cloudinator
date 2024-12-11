package istad.co.projectservice.feature.database_deployment;

import istad.co.projectservice.feature.deploy_service.InfraServiceFein;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/database")
@RequiredArgsConstructor
public class DatabaseController {

    private final InfraServiceFein infraServiceFein;


    @PostMapping("/deploy-database")
    public ResponseEntity<String> deployDatabase(@RequestParam String dbName, @RequestParam String dbUser, @RequestParam String dbPassword, @RequestParam String dbType) {
        return infraServiceFein.deployDatabaseService(dbName, dbUser, dbPassword, dbType);
    }

}
