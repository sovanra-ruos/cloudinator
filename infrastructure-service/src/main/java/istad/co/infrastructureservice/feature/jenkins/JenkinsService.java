package istad.co.infrastructureservice.feature.jenkins;

import java.util.Map;

public interface JenkinsService {

    void createMonolithicJob(String name, String gitUrl, String branch,String subdomain, String token) throws Exception;

    int startMonolithicBuild(String name) throws Exception;


}
