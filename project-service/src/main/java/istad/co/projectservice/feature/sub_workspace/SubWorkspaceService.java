package istad.co.projectservice.feature.sub_workspace;

import istad.co.projectservice.feature.sub_workspace.dto.SubWorkspaceRequest;

public interface SubWorkspaceService {

    void createSubWorkspace(SubWorkspaceRequest request);

    void deleteSubWorkspace(String name);

    void updateSubWorkspace(String name);


}
