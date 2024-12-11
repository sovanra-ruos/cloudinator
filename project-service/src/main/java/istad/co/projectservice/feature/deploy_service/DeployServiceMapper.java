package istad.co.projectservice.feature.deploy_service;

import istad.co.projectservice.domain.DeployService;
import istad.co.projectservice.feature.deploy_service.dto.CreateDeployServiceRequest;
import istad.co.projectservice.feature.deploy_service.dto.ServiceResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeployServiceMapper {

    ServiceResponse toServiceResponse(DeployService deployService);

    DeployService toDeployService(CreateDeployServiceRequest createDeployServiceRequest);


}
