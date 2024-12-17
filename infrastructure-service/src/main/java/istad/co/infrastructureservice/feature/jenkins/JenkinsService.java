package istad.co.infrastructureservice.feature.jenkins;

import istad.co.infrastructureservice.exception.JenkinsException;
import istad.co.infrastructureservice.feature.jenkins.dto.BuildInfo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface JenkinsService {

    /**
     * Creates a new monolithic Jenkins job
     *
     * @param name      The name of the job
     * @param gitUrl    The Git repository URL
     * @param branch    The Git branch name
     * @param subdomain The subdomain for deployment
     * @param token     The GitHub token
     * @throws JenkinsException if job creation fails
     */
    void createMonolithicJob(String name, String gitUrl, String branch, String subdomain, String token) throws JenkinsException;

    /**
     * Starts a monolithic build
     *
     * @param name The name of the job
     * @return The build number
     * @throws JenkinsException if build start fails
     */
    int startMonolithicBuild(String name) throws JenkinsException;

    /**
     * Starts a build in a folder
     *
     * @param folderName The name of the folder
     * @param jobName    The name of the job
     * @param serviceName The name of the service
     * @return The build number
     * @throws JenkinsException if build start fails
     */
    int StartBuildJobInFolder(String folderName, String jobName,String[] serviceName) throws JenkinsException;

    /**
     * Streams build logs to Kafka
     *
     * @param jobName     The name of the job
     * @param buildNumber The build number
     * @throws JenkinsException if streaming fails
     */
    void streamBuildLog(String jobName, int buildNumber) throws JenkinsException;

    /**
     * Streams build logs via SSE
     *
     * @param jobName     The name of the job
     * @param buildNumber The build number
     * @return SseEmitter for streaming
     * @throws JenkinsException if streaming fails
     */
    SseEmitter streamLog(String jobName, int buildNumber) throws IOException ,InterruptedException;


    /**
     * Streams build logs via SSE
     *
     * @param folderName     The name of the folder
     * @param jobName     The name of the job
     * @param buildNumber The build number
     * @return SseEmitter for streaming
     * @throws JenkinsException if streaming fails
     */
    SseEmitter streamLogInFolder(String folderName, String jobName, int buildNumber) throws IOException ,InterruptedException;

    /**
     * Gets the build status
     *
     * @param folderName     The name of the job
     * @return The build status
     * @throws JenkinsException if getting build status fails
     */
    void createFolder(String folderName);


    /**
     * *Creates database service
     * @param dbName
     * @param dbUser
     * @param dbPassword
     * @param dbType
     * @throws JenkinsException
     */
    void deployDatabaseService(String dbName, String dbUser, String dbPassword,String dbType) throws JenkinsException;


    /**
     * Disables a job
     * @param jobName
     * @throws JenkinsException
     */
    void disableJob(String jobName) throws JenkinsException;


    /**
     * Enables a job
     * @param jobName
     * @throws JenkinsException
     */
    void enableJob(String jobName) throws JenkinsException;

    /**
     * Deletes a job
     * @param jobName
     * @throws JenkinsException
     */
    void deleteJob(String jobName) throws JenkinsException;


    /**
     * Deletes a folder
     * @param folderName
     * @throws JenkinsException
     */
    void deleteFolder(String folderName) throws JenkinsException;

    /**
     * Disables a folder
     * @param folderName
     * @throws JenkinsException
     */
    void disableFolder(String folderName) throws JenkinsException;

    /**
     * Enables a folder
     * @param folderName
     * @throws JenkinsException
     */
    void enableFolder(String folderName) throws JenkinsException;


    /**
     * Updates the pipeline configuration of a Jenkins job
     *
     * @param folderName The name of the folder containing the job
     * @param jobName    The name of the job
     * @param serviceName   The new pipeline configuration
     * @throws JenkinsException if updating the job pipeline fails
     */
    void updateJobPipeline(String folderName, String jobName, String[] serviceName) throws JenkinsException;


    void updateJobDependency(String folderName, String jobName, String[] dependency) throws JenkinsException;

    /**
     * Gets all the jobs
     * @return
     * @throws JenkinsException
     */
    List<BuildInfo> getBuildsInfo(String jobName) throws IOException,InterruptedException;


    /**
     * Gets all the jobs in a folder
     * @return
     * @throws JenkinsException
     */
    List<BuildInfo> getBuildsInfoInFolder(String folderName,String jobName) throws IOException,InterruptedException;

    void deleteService(String namespace);

}
