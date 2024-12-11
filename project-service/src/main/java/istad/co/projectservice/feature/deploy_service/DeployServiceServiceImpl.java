package istad.co.projectservice.feature.deploy_service;

import istad.co.projectservice.domain.DeployService;
import istad.co.projectservice.domain.SubWorkspace;
import istad.co.projectservice.domain.Workspace;
import istad.co.projectservice.feature.deploy_service.dto.CreateDeployServiceRequest;
import istad.co.projectservice.feature.deploy_service.dto.ServiceResponse;
import istad.co.projectservice.feature.sub_workspace.SubWorkspaceRepository;
import istad.co.projectservice.feature.workspace.WorkspaceRepository;
import istad.co.projectservice.utils.CustomPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeployServiceServiceImpl implements DeployServiceService{

    private final DeployServiceRepository deployServiceRepository;
    private final WorkspaceRepository workspaceRepository;
    private final InfraServiceFein infraServiceFein;
    private final DeployServiceMapper deployServiceMapper;

    @Override
    public void createDeployService(CreateDeployServiceRequest request) {

        DeployService deployService = new DeployService();

        Workspace workspace = workspaceRepository.findByName(request.workspaceName())
                .orElseThrow(() -> new NoSuchElementException("Workspace not found"));

        deployService.setName(request.name());

        deployService.setGitUrl(request.gitUrl());

        deployService.setBranch(request.branch());

        deployService.setSubdomain(request.subdomain());

        deployService.setWorkspace(workspace);

        deployService.setStatus(true);

        deployService.setType(request.type());

        deployService.setUuid(UUID.randomUUID().toString());


        deployServiceRepository.save(deployService);

        infraServiceFein.createPipeline(request.name(), request.gitUrl(), request.branch(), request.subdomain(), request.token());

    }

    @Override
    public void buildService(String name) {

        infraServiceFein.startBuild(name);

    }

    @Override
    public void disableService(String name) {

        DeployService deployService = deployServiceRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Deploy Service not found"));


        deployService.setStatus(false);

        deployServiceRepository.save(deployService);

        infraServiceFein.disableJob(name);

    }

    @Override
    public void enableService(String name) {

        DeployService deployService = deployServiceRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Deploy Service not found"));

        deployService.setStatus(true);

        infraServiceFein.enableJob(name);

        deployServiceRepository.save(deployService);


    }

    @Override
    public void deleteService(String name) {

        DeployService deployService = deployServiceRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Deploy Service not found"));

        infraServiceFein.deleteJob(name);

        deployServiceRepository.delete(deployService);

    }

    @Override
    public ServiceResponse getServiceByName(String name) {

        DeployService deployService = deployServiceRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Deploy Service not found"));

        return deployServiceMapper.toServiceResponse(deployService);
    }

    @Override
    public CustomPage<ServiceResponse> getServices(String name ,int page, int size) {

        Page<DeployService> deployServices = deployServiceRepository.findDeployServiceByWorkspace_Name(name, PageRequest.of(page, size, Sort.by("id").descending()));

        return customPage(deployServices.map(deployServiceMapper::toServiceResponse));
    }

    public CustomPage<ServiceResponse> customPage(Page<ServiceResponse> page){

        CustomPage<ServiceResponse> customPage = new CustomPage<>();

        //check if page has next
        if(page != null && page.hasPrevious()){
            customPage.setPrevious(true); // Set to true if there is a previous page
        } else {
            customPage.setPrevious(false); // Set to false if there is no previous page
        }

        if(page != null && page.hasNext()){
            customPage.setNext(true); // Set to true if there is a next page
        } else {
            customPage.setNext(false); // Set to false if there is no next page
        }

        //set total
        customPage.setTotal((int) page.getTotalElements());
        customPage.setTotalElements(page.getTotalElements());

        //set total pages
        customPage.setResults(page.getContent());

        return customPage;
    }
}
