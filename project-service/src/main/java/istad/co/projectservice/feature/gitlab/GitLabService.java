package istad.co.projectservice.feature.gitlab;

import istad.co.projectservice.feature.gitlab.dto.CreateUserRequest;
import istad.co.projectservice.feature.gitlab.dto.CreateUserResponse;

public interface GitLabService {
    void createUser(String username, String email, String password);
}
