package istad.co.projectservice.feature.workspace;

import istad.co.projectservice.feature.workspace.dto.CreateWorkspaceRequest;
import org.springframework.security.core.Authentication;

public interface WorkspaceService {

    void createWorkspace(CreateWorkspaceRequest request, Authentication authentication);

    void deleteWorkspace(String name);

    void updateWorkspace(String name);

}
