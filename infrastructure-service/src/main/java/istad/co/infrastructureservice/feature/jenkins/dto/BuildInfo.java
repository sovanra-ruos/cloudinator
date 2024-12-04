package istad.co.infrastructureservice.feature.jenkins.dto;

import lombok.Builder;

@Builder
public record BuildInfo(
        int buildNumber,
        String status,
        String log
) {
}