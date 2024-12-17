package istad.co.projectservice.feature.project;

import istad.co.projectservice.domain.*;
import istad.co.projectservice.feature.deploy_service.InfraServiceFein;
import istad.co.projectservice.feature.deploy_service.dto.ServiceResponse;
import istad.co.projectservice.feature.gitlab.GroupRepository;
import istad.co.projectservice.feature.gitlab.PersonalRepository;
import istad.co.projectservice.feature.project.dto.ProjectRequest;
import istad.co.projectservice.feature.project.dto.ProjectResponse;
import istad.co.projectservice.feature.repository.UserRepository;
import istad.co.projectservice.feature.sub_workspace.SubWorkspaceRepository;
import istad.co.projectservice.utils.CustomPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService{

    private final UserRepository userRepository;
    private final PersonalRepository personalRepository;
    private final GroupRepository groupRepository;
    private final ProjectRepository projectRepository;
    private final SubWorkspaceRepository subWorkspaceRepository;
    private final ProjectMapper projectMapper;

    @Override
    public void createSpringService(ProjectRequest projectRequest, Authentication authentication) {

        MicroService microService = new MicroService();

        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;

        var idToken = jwtAuthenticationToken.getToken().getId();


        User user = userRepository.findByUsername(idToken)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        log.info("User found: " + user.getUsername());

        PersonalToken personalToken = personalRepository.findByUser_Username(idToken)
                .orElseThrow(() -> new NoSuchElementException("Personal token not found"));

        log.info("Personal token found: " + personalToken.getToken());

        Group groupEntity = groupRepository.findByUser_Username(idToken)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));

        log.info("Group found: " + groupEntity.getProjectId());

        SubWorkspace subWorkspace = subWorkspaceRepository.findByName(projectRequest.folder())
                .orElseThrow(() -> new NoSuchElementException("SubWorkspace not found"));


        microService.setBranch("main");
        microService.setName(projectRequest.name());
        microService.setNamespace(projectRequest.folder());
        microService.setUuid(UUID.randomUUID().toString());
        microService.setSubWorkspace(subWorkspace);
        microService.setGit("https://git.shinoshike.studio/" + groupEntity.getGroupName() + "/" + projectRequest.name() + ".git");

        projectRepository.save(microService);

//        infraServiceFein.updateJob(folder,folder,name);

        // Validate inputs
        if (projectRequest.name() == null || projectRequest.name().isEmpty() || projectRequest.group() == null || projectRequest.group().isEmpty() || projectRequest.folder() == null || projectRequest.folder().isEmpty()) {
            throw new IllegalArgumentException("Project name, group, and folder must not be null or empty.");
        }

        try {
            // Build the shell command
            List<String> command = new ArrayList<>();
            command.add("./project-service/service.sh");
            command.add(projectRequest.name());
            command.add(projectRequest.group());
            command.add(projectRequest.folder());
            command.add(personalToken.getToken());
            command.add(String.valueOf(groupEntity.getProjectId()));

            if (projectRequest.servicesNames() != null && !projectRequest.servicesNames().isEmpty()) {
                command.add(String.join(",", projectRequest.servicesNames()));
            }

            // Append dependencies as arguments, if provided
            if (projectRequest.dependencies() != null && !projectRequest.dependencies().isEmpty()) {
                command.add(String.join(",", projectRequest.dependencies()));
            }

            // Configure the process builder
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Capture and log the script's output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                System.out.println("Shell script output:");
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // Wait for the process to complete and check the exit code
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Shell script executed successfully.");
            } else {
                System.err.println("Shell script failed with exit code: " + exitCode);
            }

        } catch (Exception e) {
            // Log any errors encountered during execution
            System.err.println("An error occurred while executing the shell script:");
            e.printStackTrace();
        }
    }

    @Override
    public CustomPage<ProjectResponse> getSpringServices(String name, int page, int size) {

        Page<MicroService> microServices = projectRepository.findBySubWorkspace_Name(name, PageRequest.of(page, size, Sort.by("id").descending()));


        return customPage(microServices.map(projectMapper::toResponse));
    }

    @Override
    public ProjectResponse getSpringService(String name) {

        MicroService microService = projectRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Service not found"));

        return projectMapper.toResponse(microService);
    }

    public CustomPage<ProjectResponse> customPage(Page<ProjectResponse> page){

        CustomPage<ProjectResponse> customPage = new CustomPage<>();

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
