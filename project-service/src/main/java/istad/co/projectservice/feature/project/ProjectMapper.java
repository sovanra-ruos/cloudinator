package istad.co.projectservice.feature.project;

import istad.co.projectservice.domain.MicroService;
import istad.co.projectservice.feature.project.dto.ProjectResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectResponse toResponse(MicroService microService);

}
