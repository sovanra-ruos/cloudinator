package istad.co.projectservice.feature.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabGroupResponse {
    private int id;
}
