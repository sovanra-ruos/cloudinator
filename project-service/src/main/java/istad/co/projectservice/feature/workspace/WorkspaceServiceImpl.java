package istad.co.projectservice.feature.workspace;

import istad.co.projectservice.domain.User;
import istad.co.projectservice.domain.Workspace;
import istad.co.projectservice.feature.repository.UserRepository;
import istad.co.projectservice.feature.workspace.dto.CreateWorkspaceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Override
    public void createWorkspace(CreateWorkspaceRequest request, Authentication authentication) {

        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;

        var idToken = jwtAuthenticationToken.getToken().getId();

        User user = userRepository.findByUsername(idToken)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        System.out.println("User found: " + user.getUsername());

        Workspace workspace = new Workspace();
        workspace.setUuid(UUID.randomUUID().toString());
        workspace.setUser(user);
        workspace.setName(request.name());

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
