package istad.co.projectservice.feature.sub_workspace;

import istad.co.projectservice.feature.sub_workspace.dto.SubWorkspaceRequest;
import istad.co.projectservice.feature.sub_workspace.dto.SubworkspaceResponse;
import istad.co.projectservice.utils.CustomPage;

public interface SubWorkspaceService {

    void createSubWorkspace(SubWorkspaceRequest request);

    void deleteSubWorkspace(String name);

    void updateSubWorkspace(String name);

    CustomPage<SubworkspaceResponse> getSubWorkspaces(String name, int page, int size);


}
