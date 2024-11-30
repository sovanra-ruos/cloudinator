package istad.co.projectservice.feature.gitlab.dto;

import lombok.Builder;

@Builder
public record CreateUserResponse(
        String Username
) {
}
