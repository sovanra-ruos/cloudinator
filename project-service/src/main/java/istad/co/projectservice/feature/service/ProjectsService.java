package istad.co.projectservice.feature.service;

import istad.co.projectservice.feature.service.dto.ServiceCreateRequest;

public interface ProjectsService {

    void createService(ServiceCreateRequest request);

    void deleteService(String name);

}
