package istad.co.projectservice.feature.sub_workspace;

import istad.co.projectservice.domain.SubWorkspace;
import istad.co.projectservice.feature.sub_workspace.dto.SubworkspaceResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubWorkspaceMapper {

    SubworkspaceResponse toResponse(SubWorkspace subWorkspace);

}
