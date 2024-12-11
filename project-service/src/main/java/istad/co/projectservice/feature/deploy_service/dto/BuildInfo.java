package istad.co.projectservice.feature.deploy_service.dto;

import lombok.Builder;

@Builder
public record BuildInfo(
        int buildNumber,
        String status
) {
}
