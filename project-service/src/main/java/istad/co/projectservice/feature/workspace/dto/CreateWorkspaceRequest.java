package istad.co.projectservice.feature.workspace.dto;

import lombok.Builder;

@Builder
public record CreateWorkspaceRequest(
        String name
) {
}
