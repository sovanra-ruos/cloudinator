package istad.co.projectservice.feature.workspace;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;


    @PostMapping("/create")
    public ResponseEntity<?> createWorkspace(@RequestBody String name) {
        workspaceService.createWorkspace(name);
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
