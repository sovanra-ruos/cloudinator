package istad.co.projectservice.feature.workspace;

import istad.co.projectservice.domain.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Override
    public void createWorkspace(String name) {

        Workspace workspace = new Workspace();
        workspace.setUuid(UUID.randomUUID().toString());
        workspace.setName(name);

        workspaceRepository.save(workspace);

    }

    @Override
    public void deleteWorkspace(String name) {

        Workspace workspace = workspaceRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Workspace not found"));

        workspaceRepository.delete(workspace);

    }

    @Override
    public void updateWorkspace(String name) {

        Workspace workspace = workspaceRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Workspace not found"));

        workspace.setName(name);

        workspaceRepository.save(workspace);

    }




}
