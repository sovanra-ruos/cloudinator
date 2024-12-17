package istad.co.projectservice.feature.project.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProjectRequest(
        String name,
        String group,
        String folder,
        List<String> dependencies,
        List<String> servicesNames
) {
}
