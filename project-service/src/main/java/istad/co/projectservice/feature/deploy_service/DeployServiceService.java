package istad.co.projectservice.feature.deploy_service;

import istad.co.projectservice.feature.deploy_service.dto.CreateDeployServiceRequest;
import istad.co.projectservice.feature.deploy_service.dto.ServiceResponse;
import istad.co.projectservice.utils.CustomPage;

public interface DeployServiceService {

    void createDeployService(CreateDeployServiceRequest request);

    void buildService(String name);

    void disableService(String name);

    void enableService(String name);

    void deleteService(String name);

    ServiceResponse getServiceByName(String name);

    CustomPage<ServiceResponse> getServices(String name,int page, int size);

}
