package istad.co.projectservice.feature.deploy_service.dto;

import lombok.Builder;

@Builder
public record ServiceResponse(
        String name,
        String gitUrl,
        String branch,
        String subdomain,
        String type
) {
}
