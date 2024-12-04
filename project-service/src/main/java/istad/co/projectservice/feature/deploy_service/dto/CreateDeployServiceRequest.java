package istad.co.projectservice.feature.deploy_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateDeployServiceRequest(
        @NotNull(message = "Name is required")
        String name,
        @NotNull(message = "Git URL is required")
        String gitUrl,
        @NotNull(message = "Branch is required")
        String branch,
        @NotNull(message = "Subdomain is required")
        String subdomain,
        String token,
        @NotNull(message = "Workspace name is required")
        String workspaceName
) {
}
