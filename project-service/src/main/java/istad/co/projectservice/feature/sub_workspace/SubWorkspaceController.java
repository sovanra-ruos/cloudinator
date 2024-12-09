package istad.co.projectservice.feature.sub_workspace;

import istad.co.projectservice.feature.deploy_service.InfraServiceFein;
import istad.co.projectservice.feature.sub_workspace.dto.SubWorkspaceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sub-workspace")
@RequiredArgsConstructor
public class SubWorkspaceController {

    private final SubWorkspaceService subWorkspaceService;


    @PostMapping("/create")
    public ResponseEntity<?> createSubWorkspace(@RequestBody SubWorkspaceRequest subWorkspaceRequest) {
        subWorkspaceService.createSubWorkspace(subWorkspaceRequest);
        return ResponseEntity.ok("Sub Workspace created successfully");
    }

    @DeleteMapping("/delete/{name}")
    public ResponseEntity<?> deleteSubWorkspace(@PathVariable String name) {
        subWorkspaceService.deleteSubWorkspace(name);
        return ResponseEntity.ok("Sub Workspace deleted successfully");
    }


    @PutMapping("/update/{name}")
    public ResponseEntity<?> updateSubWorkspace(@PathVariable String name) {
        subWorkspaceService.updateSubWorkspace(name);
        return ResponseEntity.ok("Sub Workspace updated successfully");
    }


}
