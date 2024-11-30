package istad.co.projectservice.feature.gitlab.dto;

import lombok.Builder;

@Builder
public record CreateUserRequest(
        String username,
        String email,
        String password
) {
}
