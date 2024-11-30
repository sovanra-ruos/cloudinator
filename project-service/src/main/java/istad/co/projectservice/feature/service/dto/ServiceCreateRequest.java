package istad.co.projectservice.feature.service.dto;

import lombok.Builder;

@Builder
public record ServiceCreateRequest(
        String name,
        String git,
        String branch,
        String token,
        String serviceTypeName
) {
}
