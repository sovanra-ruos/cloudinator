package istad.co.projectservice.feature.sub_workspace;

import istad.co.projectservice.domain.SubWorkspace;
import istad.co.projectservice.domain.Workspace;
import istad.co.projectservice.feature.sub_workspace.dto.SubWorkspaceRequest;
import istad.co.projectservice.feature.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubWorkspaceServiceImpl implements SubWorkspaceService {

    private final SubWorkspaceRepository subWorkspaceRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    public void createSubWorkspace(SubWorkspaceRequest request) {

        SubWorkspace subWorkspace = new SubWorkspace();

        Workspace workspace = workspaceRepository.findByName(request.workspaceName())
                .orElseThrow(() -> new NoSuchElementException("Workspace not found"));

        subWorkspace.setName(request.name());

        subWorkspace.setUuid(UUID.randomUUID().toString());

        subWorkspace.setWorkspace(workspace);

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
}
