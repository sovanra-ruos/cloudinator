package istad.co.infrastructureservice.feature.jenkins;

import istad.co.infrastructureservice.feature.domain.SubDomainNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class JenkinsServiceImpl implements JenkinsService{

    private final JobRepository jobRepository;
    private final JenkinsRepository jenkinsRepository;
    private final SubDomainNameService subDomainNameService;

    @Override
    public void createMonolithicJob(String name, String gitUrl, String branch,String subdomain, String token) throws Exception {
        // Debug statement to check the value of subdomain
        System.out.println("Subdomain before creating pipeline: " + subdomain);

        System.out.println("Creating pipeline" + name + gitUrl + branch + token + subdomain);

        try {
            subDomainNameService.createSubdomain(subdomain, "178.128.81.207");

            // Debug statement to check the value of subdomain after subdomain creation
            System.out.println("Subdomain after creating subdomain: " + subdomain);

            if (subdomain != null) {
                String jobConfig = createJobConfig(name, gitUrl, branch, token, subdomain);
                jenkinsRepository.createJob(name, jobConfig);
            } else {
                throw new Exception("Subdomain is required");
            }

        } catch (Exception e) {
            throw new Exception("Error while creating pipeline", e);
        }
    }

    @Override
    public int startMonolithicBuild(String name) throws Exception {

        try {
            return jenkinsRepository.startBuild(name);
        } catch (Exception e) {
            throw new Exception("Error while starting build", e);
        }

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
        String containerName = name + "-app";

        // Debug statement to check the value of subdomain
        System.out.println("Subdomain: " + subdomain);

        String domain = subdomain.toLowerCase().replace(".", "-") + ".psa-khmer.world";

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
                        "        IMAGE = '${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}'\n" +
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
                        "}",
                gitUrl, branch, tokenizedGit, imageName, containerName, containerName, domain
        );
    }
}
