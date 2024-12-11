package istad.co.projectservice.feature.workspace;

import istad.co.projectservice.feature.workspace.dto.CreateWorkspaceRequest;
import istad.co.projectservice.feature.workspace.dto.WorkspaceResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface WorkspaceService {

    void createWorkspace(CreateWorkspaceRequest request, Authentication authentication);

    void deleteWorkspace(String name);

    void updateWorkspace(String name);

    List<WorkspaceResponse> getWorkspaces(Authentication authentication);

}
