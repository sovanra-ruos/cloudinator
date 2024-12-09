package istad.co.projectservice.feature.workspace;

import istad.co.projectservice.feature.workspace.dto.CreateWorkspaceRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String getWorkspace(Authentication authentication) {

        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;

        var idToken = jwtAuthenticationToken.getToken().getId();

        if (idToken != null) {
            return idToken;
        }else {
            return "No id token found";
        }
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public ResponseEntity<?> createWorkspace(@RequestBody CreateWorkspaceRequest name, Authentication authentication) {
        workspaceService.createWorkspace(name,authentication);
        return ResponseEntity.ok("Workspace created successfully");
    }

    @DeleteMapping("/delete/{name}")
    public ResponseEntity<?> deleteWorkspace(@PathVariable String name) {
        workspaceService.deleteWorkspace(name);
        return ResponseEntity.ok("Workspace deleted successfully");
    }

    @PutMapping("/update/{name}")
    public ResponseEntity<?> updateWorkspace(@PathVariable String name) {
        workspaceService.updateWorkspace(name);
        return ResponseEntity.ok("Workspace updated successfully");
    }


}
