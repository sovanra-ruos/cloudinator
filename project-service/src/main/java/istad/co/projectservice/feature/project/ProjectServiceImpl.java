package istad.co.projectservice.feature.project;

import istad.co.projectservice.domain.Group;
import istad.co.projectservice.domain.PersonalToken;
import istad.co.projectservice.domain.User;
import istad.co.projectservice.feature.gitlab.GroupRepository;
import istad.co.projectservice.feature.gitlab.PersonalRepository;
import istad.co.projectservice.feature.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService{

    private final UserRepository userRepository;
    private final PersonalRepository personalRepository;
    private final GroupRepository groupRepository;


    @Override
    public void createSpringService(String name, String group, String folder, List<String> dependencies, Authentication authentication) {

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



        // Validate inputs
        if (name == null || name.isEmpty() || group == null || group.isEmpty() || folder == null || folder.isEmpty()) {
            throw new IllegalArgumentException("Project name, group, and folder must not be null or empty.");
        }

        try {
            // Build the shell command
            List<String> command = new ArrayList<>();
            command.add("./project-service/service.sh");
            command.add(name);
            command.add(group);
            command.add(folder);
            command.add(personalToken.getToken());
            command.add(String.valueOf(groupEntity.getProjectId()));

            // Append dependencies as arguments, if provided
            if (dependencies != null && !dependencies.isEmpty()) {
                command.add(String.join(",", dependencies));
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

}
