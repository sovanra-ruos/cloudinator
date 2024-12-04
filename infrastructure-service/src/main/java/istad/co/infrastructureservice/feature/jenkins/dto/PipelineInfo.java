package istad.co.infrastructureservice.feature.jenkins.dto;

import lombok.Builder;

@Builder
public record PipelineInfo(
        String pipeline
) {
}
