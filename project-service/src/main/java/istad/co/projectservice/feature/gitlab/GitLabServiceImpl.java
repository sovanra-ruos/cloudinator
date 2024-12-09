package istad.co.projectservice.feature.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import istad.co.projectservice.config.GitLabConfig;
import istad.co.projectservice.domain.Group;
import istad.co.projectservice.domain.PersonalToken;
import istad.co.projectservice.domain.User;
import istad.co.projectservice.feature.gitlab.dto.GitLabGroupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabServiceImpl implements GitLabService {


    private final GitLabConfig gitLabConfig;
    private final RestTemplate restTemplate;
    private final PersonalRepository personalRepository;
    private final IdentityRepository identityRepository;
    private final GroupRepository groupRepository;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + gitLabConfig.getPersonalAccessToken());
        headers.set("Content-Type", "application/json");
        return headers;
    }


    @Override
    public void createUser(String username, String email, String password) {

        log.info("Creating user: " + username);
        log.info("Creating user: " + email);
        log.info("Creating user: " + password);

        String url = gitLabConfig.getBaseUrl() + "/users";

        Map<String, String> requestBody = Map.of(
                "email", email,
                "username", username,
                "name", username,
                "password", password
        );

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, createHeaders());

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        try {

            ObjectMapper objectMapper = new ObjectMapper();

            GitLabGroupResponse gitLabGroupResponse = objectMapper.readValue(response.getBody(), GitLabGroupResponse.class);

            Integer userId = gitLabGroupResponse.getId();

            createAccessToken(userId, username);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createAccessToken(Integer userId, String username) {
        String url = gitLabConfig.getBaseUrl() + "/users/" + userId + "/impersonation_tokens";

        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", gitLabConfig.getPersonalAccessToken());
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = Map.of(
                "name", "default",
                "scopes", "api",
                "expires_at", "2024-12-31T23:59:59Z" // Set to a specific date
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            String token = (String) responseBody.get("token");
            System.out.println("{\"token\":\"" + token + "\"}");
            createGroup(username, token);

            PersonalToken personalToken = new PersonalToken();

            User user = identityRepository.findByUsername(username).orElseThrow(
                    () -> new NoSuchElementException("User not found")
            );

            personalToken.setToken(token);
            personalToken.setUuid(UUID.randomUUID().toString());
            personalToken.setIdUser(userId);
            personalToken.setUser(user);

            log.info("Personal Token: " + user.getUsername() + " " + token);

            personalRepository.save(personalToken);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void createGroup(String groupName, String token) {

        String url = gitLabConfig.getBaseUrl() + "/groups";

        HttpHeaders headers = new    HttpHeaders();
        headers.set("PRIVATE-TOKEN", token);
        headers.set("Content-Type", "application/json");

        String path = groupName.toLowerCase().replace(" ", "-");

        System.out.println(path);

        Map<String, String> requestBody = Map.of(
                "name", groupName,
                "path", "group-" + path,
                "visibility", "private"
        );

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            GitLabGroupResponse gitLabGroupResponse = objectMapper.readValue(response.getBody(), GitLabGroupResponse.class);

            Integer groupId = gitLabGroupResponse.getId();

            Group group = new Group();

            User user = identityRepository.findByUsername(groupName).orElseThrow(
                    () -> new NoSuchElementException("User not found")
            );

            group.setGroupName(groupName);

            group.setPath(path);

            group.setUuid(UUID.randomUUID().toString());

            group.setProjectId(groupId);

            group.setUser(user);

            groupRepository.save(group);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
