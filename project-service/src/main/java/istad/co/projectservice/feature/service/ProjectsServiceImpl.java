package istad.co.projectservice.feature.service;

import istad.co.projectservice.domain.ProjectService;
import istad.co.projectservice.domain.ServiceType;
import istad.co.projectservice.feature.service.dto.ServiceCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectsServiceImpl implements ProjectsService {

    private final ServiceRepository serviceRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    @Override
    public void createService(ServiceCreateRequest request) {



        ServiceType serviceType = serviceTypeRepository.findByName(request.serviceTypeName())
                .orElseThrow(() -> new NoSuchElementException("ServiceType not found"));

        ProjectService service = new ProjectService();

        service.setName(request.name());
        service.setServiceType(serviceType);
        service.setNamespace(request.name());
        service.setBranch(request.branch());
        service.setGit(request.git());
        service.setToken(request.token());
        service.setUuid(UUID.randomUUID().toString());

        serviceRepository.save(service);


    }

    @Override
    public void deleteService(String name) {

            ProjectService service = serviceRepository.findByName(name)
                    .orElseThrow(() -> new NoSuchElementException("Service not found"));

            serviceRepository.delete(service);
    }

}
