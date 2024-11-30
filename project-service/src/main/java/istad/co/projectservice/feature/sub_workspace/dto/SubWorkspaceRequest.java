package istad.co.projectservice.feature.sub_workspace.dto;

import lombok.Builder;

@Builder
public record SubWorkspaceRequest(
        String name,
        String workspaceName
) {
}
