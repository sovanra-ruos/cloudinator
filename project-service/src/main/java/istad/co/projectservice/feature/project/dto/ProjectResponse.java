package istad.co.projectservice.feature.project.dto;

import java.util.List;

public record ProjectResponse(
        String uuid,
        String name,
        String branch,
        String namespace,
        String git
) {
}
