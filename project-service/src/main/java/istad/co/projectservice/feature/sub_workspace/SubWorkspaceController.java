package istad.co.projectservice.feature.sub_workspace;

import istad.co.projectservice.feature.sub_workspace.dto.SubWorkspaceRequest;
import istad.co.projectservice.feature.sub_workspace.dto.SubworkspaceResponse;
import istad.co.projectservice.utils.CustomPage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomPage<SubworkspaceResponse>> getServices(@PathVariable String name , @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(subWorkspaceService.getSubWorkspaces(name,page, size));
    }




}
