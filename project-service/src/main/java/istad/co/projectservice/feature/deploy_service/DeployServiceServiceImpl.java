package istad.co.projectservice.feature.deploy_service;

import istad.co.projectservice.domain.DeployService;
import istad.co.projectservice.domain.SubWorkspace;
import istad.co.projectservice.domain.Workspace;
import istad.co.projectservice.feature.deploy_service.dto.CreateDeployServiceRequest;
import istad.co.projectservice.feature.sub_workspace.SubWorkspaceRepository;
import istad.co.projectservice.feature.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeployServiceServiceImpl implements DeployServiceService{

    private final DeployServiceRepository deployServiceRepository;
    private final WorkspaceRepository workspaceRepository;

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

        deployService.setUuid(UUID.randomUUID().toString());


        deployServiceRepository.save(deployService);

    }
}
