package istad.co.projectservice.feature.workspace.dto;

import lombok.Builder;

@Builder
public record WorkspaceResponse(
        String uuid,
        String name
) {
}
