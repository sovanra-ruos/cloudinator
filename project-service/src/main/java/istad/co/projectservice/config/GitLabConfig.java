package istad.co.projectservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitLabConfig {

    @Value("${gitlab.base-url}")
    private String baseUrl;

    @Value("${gitlab.personal-access-token}")
    private String personalAccessToken;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

}
