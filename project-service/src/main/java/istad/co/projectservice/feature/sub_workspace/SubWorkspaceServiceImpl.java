package istad.co.projectservice.feature.sub_workspace;

import istad.co.projectservice.domain.SubWorkspace;
import istad.co.projectservice.domain.Workspace;
import istad.co.projectservice.feature.deploy_service.InfraServiceFein;
import istad.co.projectservice.feature.deploy_service.dto.ServiceResponse;
import istad.co.projectservice.feature.sub_workspace.dto.SubWorkspaceRequest;
import istad.co.projectservice.feature.sub_workspace.dto.SubworkspaceResponse;
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
public class SubWorkspaceServiceImpl implements SubWorkspaceService {

    private final SubWorkspaceRepository subWorkspaceRepository;
    private final WorkspaceRepository workspaceRepository;
    private final InfraServiceFein infraServiceFein;
    private final SubWorkspaceMapper subWorkspaceMapper;

    @Override
    public void createSubWorkspace(SubWorkspaceRequest request) {

        SubWorkspace subWorkspace = new SubWorkspace();

        System.out.println(request.workspaceName());

        Workspace workspace = workspaceRepository.findByName(request.workspaceName())
                .orElseThrow(() -> new NoSuchElementException("Workspace not found"));



        subWorkspace.setName(request.name());

        subWorkspace.setType("subworkspace");

        subWorkspace.setUuid(UUID.randomUUID().toString());

        subWorkspace.setWorkspace(workspace);

        if (subWorkspaceRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException("SubWorkspace already exists");
        }else {
            infraServiceFein.createFolder(request.name());
//            infraServiceFein.createMainPipeLine(request.name());
        }

        subWorkspaceRepository.save(subWorkspace);

    }

    @Override
    public void deleteSubWorkspace(String name) {

        SubWorkspace subWorkspace = subWorkspaceRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("SubWorkspace not found"));

        subWorkspaceRepository.delete(subWorkspace);

    }

    @Override
    public void updateSubWorkspace(String name) {

        SubWorkspace subWorkspace = subWorkspaceRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("SubWorkspace not found"));

        subWorkspace.setName(name);

        subWorkspaceRepository.save(subWorkspace);

    }

    @Override
    public CustomPage<SubworkspaceResponse> getSubWorkspaces(String name, int page, int size) {

        Page<SubWorkspace> subWorkspaces = subWorkspaceRepository.findSubWorkspaceByWorkspace_Name(name, PageRequest.of(page, size, Sort.by("id").descending()));

        return customPage(subWorkspaces.map(subWorkspaceMapper::toResponse));
    }

    public CustomPage<SubworkspaceResponse> customPage(Page<SubworkspaceResponse> page){

        CustomPage<SubworkspaceResponse> customPage = new CustomPage<>();

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
