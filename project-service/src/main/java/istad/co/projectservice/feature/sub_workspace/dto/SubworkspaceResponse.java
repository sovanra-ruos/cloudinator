package istad.co.projectservice.feature.sub_workspace.dto;

import lombok.Builder;

@Builder
public record SubworkspaceResponse(
        String uuid,
        String name,
        String type
) {
}
