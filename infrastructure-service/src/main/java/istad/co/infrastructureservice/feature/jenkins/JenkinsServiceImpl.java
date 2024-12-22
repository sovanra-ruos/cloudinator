package istad.co.infrastructureservice.feature.jenkins;

import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import istad.co.infrastructureservice.exception.JenkinsException;
import istad.co.infrastructureservice.feature.domain.SubDomainNameService;
import istad.co.infrastructureservice.feature.jenkins.dto.BuildInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsServiceImpl implements JenkinsService {
    private final JenkinsRepository jenkinsRepository;
    private final SubDomainNameService subDomainNameService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ExecutorService executor = Executors.newCachedThreadPool();


    @Override
    public void createMonolithicJob(String name, String gitUrl, String branch, String subdomain, String token) {
        log.info("Creating monolithic job: name={}, gitUrl={}, branch={}, subdomain={}", name, gitUrl, branch, subdomain);

        try {
            validateJobParameters(name, gitUrl, branch, subdomain);
            subDomainNameService.createSubdomain(subdomain, "35.187.253.47");

            String jobConfig = createJobConfig(name, gitUrl, branch, token, subdomain);
            jenkinsRepository.createJob(name, jobConfig);

            log.info("Successfully created monolithic job: {}", name);
        } catch (Exception e) {
            log.error("Failed to create monolithic job: {}", name, e);
            throw new JenkinsException("Failed to create monolithic job: " + name, e);
        }
    }

    @Override
    public int startMonolithicBuild(String name) {
        log.info("Starting monolithic build for job: {}", name);

        try {
            int buildNumber = jenkinsRepository.startBuild(name);
            log.info("Successfully started build #{} for job: {}", buildNumber, name);
            return buildNumber;
        } catch (Exception e) {
            log.error("Failed to start build for job: {}", name, e);
            throw new JenkinsException("Failed to start build: " + name, e);
        }
    }

    @Override
    public int StartBuildJobInFolder(String folderName, String jobName, String[] serviceName) throws JenkinsException {

        log.info("Starting build for folder {} and name {}", folderName, jobName);
        log.info("Service names: {}", (Object) serviceName);

        try {
            jenkinsRepository.updateJobPipeline(folderName, jobName, serviceName);
            HashedMap map = new HashedMap();
            for (int i = 0; i < serviceName.length; i++) {
                map.put("service" + (i + 1), serviceName[i]);
            }
            int buildNumber = jenkinsRepository.startBuildInFolder(folderName, jobName, map);
            log.info("Successfully started build #{} for job: {}", buildNumber, jobName);
            return buildNumber;
        } catch (Exception e) {
            log.error("Failed to start build for job: {}", jobName, e);
            throw new JenkinsException("Failed to start build: " + jobName, e);
        }
    }

    @Override
    public void streamBuildLog(String jobName, int buildNumber) throws JenkinsException {

    }


    @Override
    public SseEmitter streamLog(String jobName, int buildNumber) throws IOException, InterruptedException {
        SseEmitter emitter = new SseEmitter(1800000L);
        executor.execute(() -> {
            try {
                Build build = jenkinsRepository.getBuild(jobName, buildNumber);
                int lastReadPosition = 0;

                while (true) {
                    String newLogs = getNewLogs(build, lastReadPosition);
                    if (!newLogs.isEmpty()) {
                        emitter.send(SseEmitter.event().data(newLogs));
                        lastReadPosition += newLogs.length();
                    }
                    if (!build.details().isBuilding()) {
                        break; // Exit loop if build is complete
                    }
                    Thread.sleep(1000);
                }
                // Send remaining logs and complete the emitter
                String remainingLogs = getNewLogs(build, lastReadPosition);
                if (!remainingLogs.isEmpty()) {
                    emitter.send(SseEmitter.event().data(remainingLogs));
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @Override
    public SseEmitter streamLogInFolder(String folderName, String jobName, int buildNumber) throws IOException, InterruptedException {

        SseEmitter emitter = new SseEmitter(1800000L);
        executor.execute(() -> {
            try {
                Build build = jenkinsRepository.getBuildInFolder(folderName, jobName, buildNumber);
                int lastReadPosition = 0;

                while (true) {
                    String newLogs = getNewLogs(build, lastReadPosition);
                    if (!newLogs.isEmpty()) {
                        emitter.send(SseEmitter.event().data(newLogs));
                        lastReadPosition += newLogs.length();
                    }
                    if (!build.details().isBuilding()) {
                        break; // Exit loop if build is complete
                    }
                    Thread.sleep(1000);
                }
                // Send remaining logs and complete the emitter
                String remainingLogs = getNewLogs(build, lastReadPosition);
                if (!remainingLogs.isEmpty()) {
                    emitter.send(SseEmitter.event().data(remainingLogs));
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;

    }


    private String getNewLogs(Build build, int lastReadPosition) throws IOException {
        String fullLog = build.details().getConsoleOutputText();
        if (lastReadPosition < fullLog.length()) {
            return fullLog.substring(lastReadPosition);
        }
        return "";
    }


    private void validateJobParameters(String name, String gitUrl, String branch, String subdomain) {
        if (name == null || name.trim().isEmpty()) {
            throw new JenkinsException("Job name is required");
        }
        if (gitUrl == null || gitUrl.trim().isEmpty()) {
            throw new JenkinsException("Git URL is required");
        }
        if (branch == null || branch.trim().isEmpty()) {
            throw new JenkinsException("Branch name is required");
        }
        if (subdomain == null || subdomain.trim().isEmpty()) {
            throw new JenkinsException("Subdomain is required");
        }
    }

    @Override
    public void createFolder(String folderName) {
        try {
            // First create the folder
            jenkinsRepository.createFolder(folderName);

            // Execute the shell script with the folder name as a parameter
            ProcessBuilder processBuilder = new ProcessBuilder("./infrastructure-service/create-workspace.sh", folderName);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read the output from the script
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Shell script execution failed with exit code " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Failed to create folder and execute script: {}", folderName, e);
            throw new JenkinsException("Failed to create folder and execute script: " + folderName, e);
        }
    }



    @Override
    public void disableJob(String jobName) throws JenkinsException {

        try {

            jenkinsRepository.disableJob(jobName);
            log.info("Successfully disabled job: {}", jobName);

        }catch (Exception e) {
            log.error("Failed to disable job: {}", jobName, e);
            throw new JenkinsException("Failed to disable job: " + jobName, e);
        }

    }

    @Override
    public void enableJob(String jobName) throws JenkinsException {

        try {

            jenkinsRepository.enableJob(jobName);
            log.info("Successfully enabled job: {}", jobName);

        }catch (Exception e) {
            log.error("Failed to enable job: {}", jobName, e);
            throw new JenkinsException("Failed to enable job: " + jobName, e);
        }

    }

    @Override
    public void deleteJob(String jobName) throws JenkinsException {

        try {

            jenkinsRepository.deleteJob(jobName);
            log.info("Successfully deleted job: {}", jobName);

        }catch (Exception e) {
            log.error("Failed to delete job: {}", jobName, e);
            throw new JenkinsException("Failed to delete job: " + jobName, e);
        }

    }

    @Override
    public void deleteFolder(String folderName) throws JenkinsException {

        try {
            // Execute the shell script with the folder name as a parameter
            ProcessBuilder processBuilder = new ProcessBuilder("./infrastructure-service/delete_workspace.sh", folderName);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read the output from the script
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Shell script execution failed with exit code " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Failed to create folder and execute script: {}", folderName, e);
            throw new JenkinsException("Failed to create folder and execute script: " + folderName, e);
        }

    }

    @Override
    public void disableFolder(String folderName) throws JenkinsException {

    }

    @Override
    public void enableFolder(String folderName) throws JenkinsException {

    }

    @Override
    public void updateJobPipeline(String folderName, String jobName, String[] serviceName) throws JenkinsException {

        try {
            jenkinsRepository.updateJobPipeline(folderName,jobName,serviceName);
            log.info("Successfully updated pipeline configuration with new services");
        } catch (JenkinsRepository.JenkinsException e) {
            log.error("Failed to update pipeline configuration", e);
        }

    }

    @Override
    public void updateJobDependency(String folderName, String jobName, String[] dependency) throws JenkinsException {

        try {
            jenkinsRepository.updateJobPipelineDependencies(folderName,jobName,dependency);

            log.info("Successfully updated the dependency configuration with new services");

        } catch (JenkinsRepository.JenkinsException e) {
            log.error("Failed to update pipeline configuration", e);
        }

    }

    @Override
    public List<BuildInfo> getBuildsInfo(String jobName) throws IOException, InterruptedException {

        try {
            List<BuildInfo> buildInfoList = jenkinsRepository.getBuildsInfo(jobName);
            log.info("Successfully retrieved build information for job: {}", jobName);
            return buildInfoList;
        } catch (Exception e) {
            log.error("Failed to retrieve build information for job: {}", jobName, e);
            throw new JenkinsException("Failed to retrieve build information for job: " + jobName, e);
        }


    }

    @Override
    public List<BuildInfo> getBuildsInfoInFolder(String folderName, String jobName) throws IOException, InterruptedException {

        log.info("Retrieving build information for job: {}", jobName);
        log.info("Retrieving build information for folder: {}", folderName);


        try {
            List<BuildInfo> buildInfoList = jenkinsRepository.getBuildsInfoInFolder(folderName, jobName);
            log.info("Successfully retrieved build information for job: {}", jobName);
            return buildInfoList;
        } catch (Exception e) {
            log.error("Failed to retrieve build information for job: {}", jobName, e);
            throw new JenkinsException("Failed to retrieve build information for job: " + jobName, e);
        }
    }

    @Override
    public void deleteService(String namespace,Integer count) {
        try {
            // Update the pipeline for deletion
            jenkinsRepository.updateForDelete("delete-service-pipeline", namespace,count);

            // Start the build for the delete-service-pipeline
            int buildNumber = jenkinsRepository.startBuild("delete-service-pipeline");
            log.info("Successfully started build #{} for delete-service-pipeline", buildNumber);
        } catch (Exception e) {
            log.error("Failed to delete service: {}", namespace, e);
            throw new JenkinsException("Failed to delete service: " + namespace, e);
        }
    }

    @Override
    public void rollbackService(String name, Integer tag) {

        try {
            // Update the pipeline for rollback
            jenkinsRepository.rollbackVersion("rollback-service-pipeline", name, tag);
            // Start the build for the rollback-service-pipeline
            int buildNumber = jenkinsRepository.startBuild("rollback-service-pipeline");
            log.info("Successfully started build #{} for rollback-service-pipeline", buildNumber);
        } catch (Exception e) {
            log.error("Failed to rollback service: {}", name, e);
            throw new JenkinsException("Failed to rollback service: " + name, e);
        }

    }

    @Override
    public void deleteJobInFolder(String folderName, String jobName) throws JenkinsException {

        try {
            jenkinsRepository.deleteJobInFolder(folderName, jobName);
            log.info("Successfully deleted job: {}", jobName);
        } catch (Exception e) {
            log.error("Failed to delete job: {}", jobName, e);
            throw new JenkinsException("Failed to delete job: " + jobName, e);
        }

    }


    private String generateRandomDomain(String dbName) {
        int randomNumber = new Random().nextInt(900000) + 100000; // Generate a 6-digit random number
        return dbName + randomNumber;
    }

    @Override
    public void deployDatabaseService(String dbName, String dbUser, String dbPassword, String dbType) throws JenkinsException {

        log.info("Deploying database service: dbName={}, dbUser={}", dbName, dbUser);

        try {
            // Step 1: Generate the job configuration XML
            String jobConfig = generateXml(dbName, dbUser, dbPassword, dbType);

            // Step 2: Create the Jenkins job
            jenkinsRepository.createJob(dbName, jobConfig);
            log.info("Successfully created Jenkins job for database service: {}", dbName);

            // Step 3: Trigger the Jenkins job immediately
            jenkinsRepository.startBuild(dbName);
            log.info("Successfully triggered Jenkins job for database service: {}", dbName);

        } catch (Exception e) {
            log.error("Failed to deploy database service: {}", dbName, e);
            throw new JenkinsException("Failed to deploy database service: " + dbName, e);
        }
    }

    public String generateXml(String dbName, String dbUser, String dbPassword, String dbType) {
        String jobConfig = createDatabasePipeline(dbName, dbUser, dbPassword, dbType);

        return String.format(
                "<?xml version='1.1' encoding='UTF-8'?>\n" +
                        "<flow-definition plugin=\"workflow-job@2.40\">\n" +
                        "  <description>%s</description>\n" +
                        "  <keepDependencies>false</keepDependencies>\n" +
                        "  <properties>\n" +
                        "    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>\n" +
                        "      <triggers>\n" +
                        "        <com.cloudbees.jenkins.GitHubPushTrigger plugin=\"github@1.29.4\">\n" +
                        "          <spec></spec>\n" +
                        "        </com.cloudbees.jenkins.GitHubPushTrigger>\n" +
                        "      </triggers>\n" +
                        "    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>\n" +
                        "  </properties>\n" +
                        "  <definition class=\"org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition\" plugin=\"workflow-cps@2.90\">\n" +
                        "    <script>%s</script>\n" +
                        "    <sandbox>true</sandbox>\n" +
                        "  </definition>\n" +
                        "  <triggers>\n" +
                        "    <com.cloudbees.jenkins.GitHubPushTrigger plugin=\"github@1.29.4\">\n" +
                        "      <spec></spec>\n" +
                        "    </com.cloudbees.jenkins.GitHubPushTrigger>\n" +
                        "  </triggers>\n" +
                        "  <disabled>false</disabled>\n" +
                        "</flow-definition>",
                dbName,
                jobConfig
        );
    }

    public String createDatabasePipeline(String dbName, String dbUser, String dbPassword, String dbType) {

        String randomDomain = generateRandomDomain(dbName);

        subDomainNameService.createSubdomain(randomDomain, "35.187.253.47");

        String domain = randomDomain + ".psa-khmer.world";

        String namespace = dbName + randomDomain.substring(dbName.length(), dbName.length() + 6);

        String storageSize = "1Gi";

        String email = "vannraruos@gmail.com";

        return String.format(
                "@Library('monolithic') _\n" +
                        "\n" +
                        "pipeline {\n" +
                        "    agent any\n" +
                        "    parameters {\n" +
                        "        string(name: 'DB_TYPE', defaultValue: '%s', description: 'Database type (postgres or mongodb)')\n" +
                        "        string(name: 'NAMESPACE', defaultValue: '%s', description: 'Kubernetes namespace')\n" +
                        "        string(name: 'STORAGE_SIZE', defaultValue: '%s', description: 'Database storage size')\n" +
                        "    }\n" +
                        "    environment {\n" +
                        "        DOMAIN_NAME = '%s'\n" +
                        "        EMAIL = '%s'\n" +
                        "        INVENTORY_FILE = 'inventory/inventory.ini'\n" +
                        "        PLAYBOOK_FILE = 'playbooks/deploy-database.yml'\n" +
                        "        DB_NAME = '%s'\n" +
                        "        DB_USERNAME = '%s'\n" +
                        "        DB_PASSWORD = '%s'\n" +
                        "        DB_IMAGE = \"${params.DB_TYPE == 'mongodb' ? 'mongo:4.4' : 'postgres:13'}\"\n" +
                        "    }\n" +
                        "    stages {\n" +
                        "        stage('Clone Infrastructure') {\n" +
                        "            steps {\n" +
                        "                git branch: 'main', url: 'https://github.com/devoneone/infra.git'\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Deploy Database') {\n" +
                        "            steps {\n" +
                        "                script {\n" +
                        "                    sh \"\"\"\n" +
                        "                    ansible-playbook -i ${INVENTORY_FILE} ${PLAYBOOK_FILE} \\\n" +
                        "                    -e \"DB_NAME=${DB_NAME}\" \\\n" +
                        "                    -e \"DB_IMAGE=${DB_IMAGE}\" \\\n" +
                        "                    -e \"NAMESPACE=${params.NAMESPACE}\" \\\n" +
                        "                    -e \"DB_USERNAME=${DB_USERNAME}\" \\\n" +
                        "                    -e \"DB_PASSWORD=${DB_PASSWORD}\" \\\n" +
                        "                    -e \"DOMAIN_NAME=${DOMAIN_NAME}\" \\\n" +
                        "                    -e \"EMAIL=${EMAIL}\" \\\n" +
                        "                    -e \"STORAGE_SIZE=${params.STORAGE_SIZE}\"\n" +
                        "                    \"\"\"\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "    post {\n" +
                        "        success {\n" +
                        "            echo 'Database deployed successfully.'\n" +
                        "        }\n" +
                        "        failure {\n" +
                        "            echo 'Deployment failed.'\n" +
                        "        }\n" +
                        "        always {\n" +
                        "            cleanWs()\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n",
                dbType, namespace, storageSize, domain, email, dbName, dbUser, dbPassword
        );
    }


    private String createJobConfig(String name, String gitUrl, String branch, String token, String subdomain) {
        String jobConfig = createMonolithicPipeline(name, gitUrl, branch, token, subdomain);
        return String.format(
                "<?xml version='1.1' encoding='UTF-8'?>\n" +
                        "<flow-definition plugin=\"workflow-job@2.40\">\n" +
                        "  <description>%s</description>\n" +
                        "  <keepDependencies>false</keepDependencies>\n" +
                        "  <properties>\n" +
                        "    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>\n" +
                        "      <triggers>\n" +
                        "        <com.cloudbees.jenkins.GitHubPushTrigger plugin=\"github@1.29.4\">\n" +
                        "          <spec></spec>\n" +
                        "        </com.cloudbees.jenkins.GitHubPushTrigger>\n" +
                        "      </triggers>\n" +
                        "    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>\n" +
                        "  </properties>\n" +
                        "  <definition class=\"org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition\" plugin=\"workflow-cps@2.90\">\n" +
                        "    <script>%s</script>\n" +
                        "    <sandbox>true</sandbox>\n" +
                        "  </definition>\n" +
                        "  <triggers>\n" +
                        "    <com.cloudbees.jenkins.GitHubPushTrigger plugin=\"github@1.29.4\">\n" +
                        "      <spec></spec>\n" +
                        "    </com.cloudbees.jenkins.GitHubPushTrigger>\n" +
                        "  </triggers>\n" +
                        "  <disabled>false</disabled>\n" +
                        "</flow-definition>",
                name,
                jobConfig
        );
    }



    private String createMonolithicPipeline(String name, String gitUrl, String branch, String token, String subdomain) {
        String containerName = name;

        // Debug statement to check the value of subdomain
        System.out.println("Subdomain: " + subdomain);

        String domain = subdomain.toLowerCase().replace(".", "-") + ".cloudinator.cloud";

        // Debug statement to check the value of domain
        System.out.println("Domain: " + domain);

        System.out.println("Creating pipeline" + name + gitUrl + branch + token + domain);

        String tokenizedGit = token;

        String imageName = "sovanra/" + name.toLowerCase().replace("_", "-");

        return String.format(
                "@Library('monolithic') _\n" +
                        "\n" +
                        "pipeline {\n" +
                        "    agent any\n" +
                        "    environment {\n" +
                        "        GIT_REPO_URL = '%s'\n" +
                        "        GIT_BRANCH = '%s'\n" +
                        "        GITHUB_TOKEN = '%s'\n" +
                        "        WEBHOOK_URL = 'https://jenkin.psa-khmer.world/github-webhook/'\n" +
                        "        DOCKER_IMAGE_NAME = '%s'\n" +
                        "        DOCKER_IMAGE_TAG = '${BUILD_NUMBER}'\n" +
                        "        DOCKER_CREDENTIALS_ID = 'docker'\n" +
                        "        GIT_INFRA_URL = 'https://github.com/sovanra-ruos/infra.git'\n" +
                        "        INVENTORY_FILE = 'inventory/inventory.ini'\n" +
                        "        PLAYBOOK_FILE = 'playbooks/deploy-with-k8s.yml'\n" +
                        "        HELM_FILE = 'playbooks/setup-helm.yml'\n" +
                        "        APP_NAME = '%s'\n" +
                        "        FILE_Path = 'deployments/${APP_NAME}'\n" +
                        "        IMAGE = \"${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}\"\n" +
                        "        NAMESPACE = '%s'\n" +
                        "        DOMAIN_NAME = '%s'\n" +
                        "        EMAIL = 'your-email@example.com'\n" +
                        "        TRIVY_SEVERITY = 'HIGH,CRITICAL'\n" +
                        "        TRIVY_EXIT_CODE = '0'\n" +
                        "        TRIVY_IGNORE_UNFIXED = 'true'\n" +
                        "        VULN_THRESHOLD = '500'\n" +
                        "    }\n" +
                        "    stages {\n" +
                        "        stage('Checkout') {\n" +
                        "            steps {\n" +
                        "                git branch: env.GIT_BRANCH, url: env.GIT_REPO_URL\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Generate Dockerfile') {\n" +
                        "            steps {\n" +
                        "                script {\n" +
                        "                    projectInfo = detectProjectType(\"${env.WORKSPACE}\")\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Update Dependencies') {\n" +
                        "            steps {\n" +
                        "                script {\n" +
                        "                    updateDependencies()\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Docker Login') {\n" +
                        "            steps {\n" +
                        "                script {\n" +
                        "                    withCredentials([string(credentialsId: 'docker', variable: 'DOCKER_PWD')]) {\n" +
                        "                        sh 'echo $DOCKER_PWD | docker login -u sovanra --password-stdin'\n" +
                        "                    }\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Build Docker Image') {\n" +
                        "            steps {\n" +
                        "                script {\n" +
                        "                    dockerBuild(\"${DOCKER_IMAGE_NAME}\", \"${DOCKER_IMAGE_TAG}\")\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Trivy Scan') {\n" +
                        "            steps {\n" +
                        "                script {\n" +
                        "                    def vulnerabilitiesCount = trivyScan(\n" +
                        "                        DOCKER_IMAGE_NAME,\n" +
                        "                        DOCKER_IMAGE_TAG,\n" +
                        "                        TRIVY_SEVERITY,\n" +
                        "                        TRIVY_EXIT_CODE,\n" +
                        "                        TRIVY_IGNORE_UNFIXED,\n" +
                        "                        VULN_THRESHOLD.toInteger()\n" +
                        "                    )\n" +
                        "                    echo 'Total vulnerabilities found: ${vulnerabilitiesCount}'\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Push Image to Registry') {\n" +
                        "            steps {\n" +
                        "                script {\n" +
                        "                    dockerPush(\"${DOCKER_IMAGE_NAME}\", \"${DOCKER_IMAGE_TAG}\")\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Clone infra') {\n" +
                        "            steps {\n" +
                        "                git branch: env.GIT_BRANCH, url: env.GIT_INFRA_URL\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Deploy to Kubernetes') {\n" +
                        "            steps {\n" +
                        "                script {\n" +
                        "                    deployToKubernetes(\n" +
                        "                        INVENTORY_FILE,\n" +
                        "                        PLAYBOOK_FILE,\n" +
                        "                        APP_NAME,\n" +
                        "                        IMAGE,\n" +
                        "                        NAMESPACE,\n" +
                        "                        FILE_Path,\n" +
                        "                        DOMAIN_NAME,\n" +
                        "                        EMAIL,\n" +
                        "                        GIT_REPO_URL\n" +
                        "                    )\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('set up helm') {\n" +
                        "            steps {\n" +
                        "                setUpHelm(\n" +
                        "                    INVENTORY_FILE,\n" +
                        "                    HELM_FILE,\n" +
                        "                    APP_NAME,\n" +
                        "                    DOCKER_IMAGE_NAME,\n" +
                        "                    NAMESPACE,\n" +
                        "                    DOCKER_IMAGE_TAG,\n" +
                        "                    DOMAIN_NAME,\n" +
                        "                    GIT_REPO_URL\n" +
                        "                )\n" +
                        "            }\n" +
                        "        }\n" +
                        "        stage('Setup GitHub Webhook') {\n" +
                        "            steps {\n" +
                        "                script {\n" +
                        "                    createGitHubWebhook(env.GIT_REPO_URL, env.WEBHOOK_URL, env.GITHUB_TOKEN)\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "    post {\n" +
                       "        always {\n" +
                        "            cleanWs()\n" +
                        "        }\n" +
                        "    }\n" +
                        "}",
                gitUrl, branch, tokenizedGit, imageName, containerName, containerName, domain
        );
    }
}
