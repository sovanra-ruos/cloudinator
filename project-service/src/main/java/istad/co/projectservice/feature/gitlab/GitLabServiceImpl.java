package istad.co.projectservice.feature.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import istad.co.projectservice.config.GitLabConfig;
import istad.co.projectservice.feature.gitlab.dto.CreateUserRequest;
import istad.co.projectservice.feature.gitlab.dto.GitLabGroupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GitLabServiceImpl implements GitLabService {


    private final GitLabConfig gitLabConfig;
    private final RestTemplate restTemplate;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + gitLabConfig.getPersonalAccessToken());
        headers.set("Content-Type", "application/json");
        return headers;
    }


    @Override
    public void createUser(String username, String email, String password) {

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

            int userId = gitLabGroupResponse.getId();

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
            int groupId = gitLabGroupResponse.getId();
            System.out.println(groupId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
