package istad.co.infrastructureservice.feature.jenkins;

public interface JenkinsService {

    void createMonolithicJob(String name, String gitUrl, String branch,String subdomain, String token) throws Exception;

}
