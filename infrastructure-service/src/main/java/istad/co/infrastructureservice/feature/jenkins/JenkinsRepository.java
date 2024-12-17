package istad.co.infrastructureservice.feature.jenkins;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.model.*;
import istad.co.infrastructureservice.feature.jenkins.dto.BuildInfo;
import istad.co.infrastructureservice.feature.jenkins.dto.PipelineInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class JenkinsRepository {

    private final JenkinsServer jenkins;
    private final JenkinsHttpClient client;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(JenkinsRepository.class);

    public JenkinsRepository(
            @Value("${jenkins.url}") String jenkinsUrl,
            @Value("${jenkins.username}") String jenkinsUsername,
            @Value("${jenkins.password}") String jenkinsPassword,
            KafkaTemplate<String, String> kafkaTemplate) throws URISyntaxException {
        this.client = new JenkinsHttpClient(new URI(jenkinsUrl), jenkinsUsername, jenkinsPassword);
        this.jenkins = new JenkinsServer(client);
        this.kafkaTemplate = kafkaTemplate;
    }

    public void createJob(String jobName, String jobConfig) throws IOException {
        jenkins.createJob(jobName, jobConfig);
    }


    public int startBuild(String jobName) throws IOException, InterruptedException {
        JobWithDetails job = jenkins.getJob(jobName);
        QueueReference queueReference = job.build();
        return getBuildNumberFromQueue(queueReference);
    }

    public int startBuildInFolder(String folderName, String jobName, Map<String, String> params) throws IOException, InterruptedException {
        String jobPath = String.format("%s/%s", folderName, jobName);
        JobWithDetails job = jenkins.getJob(jobPath);
        if (job == null) {
            throw new IOException("Job not found: " + jobPath);
        }

        log.info("param for build job: " + params);
        log.info("Starting build for job: " + job);
        log.info("Starting build for job: " + jobPath);
        QueueReference queueReference;
        try {
            queueReference = job.build(params);
        } catch (HttpResponseException e) {
            log.error("Failed to start build for job: " + jobPath, e);
            log.error("HTTP Response: " + e.getStatusCode() + " - " + e.getMessage());
            throw e;
        }

        return getBuildNumberFromQueue(queueReference);
    }

    private int getBuildNumberFromQueue(QueueReference queueReference) throws IOException, InterruptedException {
        QueueItem queueItem = jenkins.getQueueItem(queueReference);
        while (queueItem.getExecutable() == null) {
            Thread.sleep(100);
            queueItem = jenkins.getQueueItem(queueReference);
        }
        return queueItem.getExecutable().getNumber().intValue();
    }

    public Build getBuild(String jobName, int buildNumber) throws IOException {
        JobWithDetails job = jenkins.getJob(jobName);
        return job.getBuildByNumber(buildNumber);
    }

    public Build getBuildInFolder(String folderName, String jobName, int buildNumber) throws IOException {
        String jobPath = String.format("%s/%s", folderName, jobName);
        JobWithDetails job = jenkins.getJob(jobPath);
        if (job == null) {
            throw new IOException("Job not found: " + jobPath);
        }
        return job.getBuildByNumber(buildNumber);
    }

    public void streamBuildLog(String jobName, int buildNumber) throws IOException, InterruptedException {
        Build build = getBuild(jobName, buildNumber);
        BuildWithDetails buildDetails = build.details();
        String topicName = String.format("%s-%d", jobName, buildNumber);
        int lastPosition = 0;

        while (buildDetails.isBuilding()) {
            String consoleOutput = buildDetails.getConsoleOutputText();
            if (consoleOutput.length() > lastPosition) {
                String newContent = consoleOutput.substring(lastPosition);
                kafkaTemplate.send(topicName, newContent);
                lastPosition = consoleOutput.length();
            }
            Thread.sleep(1000);
            buildDetails = build.details();
        }

        // Send final log content if any
        String finalOutput = buildDetails.getConsoleOutputText();
        if (finalOutput.length() > lastPosition) {
            String newContent = finalOutput.substring(lastPosition);
            kafkaTemplate.send(topicName, newContent);
        }
    }

    public void deleteJob(String jobName) throws IOException {
        jenkins.deleteJob(jobName, true);
    }

    public void disableJob(String jobName) throws IOException {
        jenkins.disableJob(jobName);
    }

    public void enableJob(String jobName) throws IOException {
        jenkins.enableJob(jobName);
    }


    public JobWithDetails getJob(String jobName) throws IOException {
        return jenkins.getJob(jobName);
    }

    public List<BuildInfo> getBuildsInfo(String jobName) throws IOException {
        JobWithDetails job = jenkins.getJob(jobName);
        List<BuildInfo> buildInfos = new ArrayList<>();

        for (Build build : job.getBuilds()) {
            BuildWithDetails buildDetails = build.details();
            String status = buildDetails.getResult() != null ? buildDetails.getResult().toString() : "BUILDING";
            String log = buildDetails.getConsoleOutputText();
            buildInfos.add(new BuildInfo(build.getNumber(), status));
        }

        return buildInfos;
    }

    public List<BuildInfo> getBuildsInfoInFolder(String folderName, String jobName) throws IOException {
        String jobPath = String.format("%s/%s", folderName, jobName);
        log.info("Job path: " + jobPath);

        JobWithDetails job = jenkins.getJob(jobPath);
        if (job == null) {
            throw new IOException("Job not found: " + jobPath);
        }

        List<BuildInfo> buildInfos = new ArrayList<>();
        for (Build build : job.getBuilds()) {
            BuildWithDetails buildDetails = build.details();
            String status = buildDetails.getResult() != null ? buildDetails.getResult().toString() : "BUILDING";
            buildInfos.add(new BuildInfo(build.getNumber(), status));
        }

        return buildInfos;
    }

    public PipelineInfo getPipelineInfo(String jobName) throws IOException {
        JobWithDetails jobDetails = jenkins.getJob(jobName);

        if (jobDetails == null) {
            throw new IOException("Job not found");
        }

        String jobConfigXml = getJobConfigXml(jobName);
        String pipelineScript = extractPipelineScriptFromConfig(jobConfigXml);

        return new PipelineInfo(pipelineScript);
    }

    private String getJobConfigXml(String jobName) throws IOException {
        try {
            String path = "/job/" + jobName + "/config.xml";
            return client.get(path);
        } catch (HttpResponseException e) {
            throw new IOException("Failed to fetch job configuration", e);
        }
    }

    private String extractPipelineScriptFromConfig(String jobConfigXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(jobConfigXml.getBytes()));

            NodeList scriptNodes = document.getElementsByTagName("script");
            if (scriptNodes.getLength() > 0) {
                Element scriptElement = (Element) scriptNodes.item(0);
                return scriptElement.getTextContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void updateForDelete(String jobName, String namespace) throws JenkinsException {
        try {
            // Get the current job configuration XML
            String configXml = client.get(String.format("/job/%s/config.xml", jobName));

            log.info("Original XML: " + configXml);

            // Parse the existing configuration
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(configXml.getBytes()));

            // Find the 'script' section within the configuration
            NodeList scriptNodes = document.getElementsByTagName("script");
            if (scriptNodes.getLength() > 0) {
                Element scriptElement = (Element) scriptNodes.item(0);
                String scriptContent = scriptElement.getTextContent();

                // Override the NAMESPACE value
                String namespacePattern = "NAMESPACE = '";
                int namespaceIndex = scriptContent.indexOf(namespacePattern);
                if (namespaceIndex != -1) {
                    int startQuote = namespaceIndex + namespacePattern.length();
                    int endQuote = scriptContent.indexOf("'", startQuote);

                    if (startQuote != -1 && endQuote != -1) {
                        String updatedScriptContent = scriptContent.substring(0, startQuote) +
                                namespace + scriptContent.substring(endQuote);

                        scriptElement.setTextContent(updatedScriptContent);

                        log.info("Updated NAMESPACE value: " + namespace);
                    }
                }
            }

            // Convert the modified document back to string
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            String updatedConfig = writer.toString();

            log.info("Updated XML: " + updatedConfig);

            // Update the job configuration
            client.post_xml(String.format("/job/%s/config.xml", jobName), updatedConfig);

        } catch (Exception e) {
            throw new JenkinsException("Failed to update pipeline configuration: " + e.getMessage(), e);
        }
    }

    public void updateJobPipeline(String folderName, String jobName, String[] serviceNames) throws JenkinsException {

        log.info("Service names: " + Arrays.toString(serviceNames));
        log.info("Folder name: " + folderName);

        try {
            // Get the current job configuration XML
            String jobPath = String.format("%s/job/%s", folderName, jobName);
            String configXml = client.get(String.format("/job/%s/config.xml", jobPath));

            log.info("Original XML: " + configXml);

            // Parse the existing configuration
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(configXml.getBytes()));

            // Find the 'parameters' section within the script
            NodeList scriptNodes = document.getElementsByTagName("script");
            if (scriptNodes.getLength() > 0) {
                Element scriptElement = (Element) scriptNodes.item(0);
                String scriptContent = scriptElement.getTextContent();

                // Parse the script content to find the SERVICES parameter
                int servicesIndex = scriptContent.indexOf("SERVICES', defaultValue:");
                if (servicesIndex != -1) {
                    int startQuote = scriptContent.indexOf("'", servicesIndex + "SERVICES', defaultValue:".length());
                    int endQuote = scriptContent.indexOf("'", startQuote + 1);

                    if (startQuote != -1 && endQuote != -1) {
                        // Join the array of service names into a comma-separated string
                        String updatedServices = String.join(",", serviceNames);

                        // Replace the old services list with the updated one
                        String updatedScriptContent = scriptContent.substring(0, startQuote + 1) +
                                updatedServices + scriptContent.substring(endQuote);

                        scriptElement.setTextContent(updatedScriptContent);

                        log.info("Updated services list: " + updatedServices);
                    }
                }
            }

            // Convert the modified document back to string
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            String updatedConfig = writer.toString();

            log.info("Updated XML: " + updatedConfig);

            // Update the job configuration
            client.post_xml(String.format("/job/%s/config.xml", jobPath), updatedConfig);

        } catch (Exception e) {
            throw new JenkinsException("Failed to update pipeline configuration: " + e.getMessage(), e);
        }
    }

    public void updateJobPipelineDependencies(String folderName, String jobName, String[] dependencyServices) throws JenkinsException {
        try {
            // Get the current job configuration XML
            String jobPath = String.format("%s/job/%s", folderName, jobName);
            String configXml = client.get(String.format("/job/%s/config.xml", jobPath));

            log.info("Original XML: " + configXml);

            // Parse the existing configuration
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(configXml.getBytes()));

            // Find the 'parameters' section within the script
            NodeList scriptNodes = document.getElementsByTagName("script");
            if (scriptNodes.getLength() > 0) {
                Element scriptElement = (Element) scriptNodes.item(0);
                String scriptContent = scriptElement.getTextContent();

                // Parse the script content to find the DEPENDENCIES parameter
                int dependenciesIndex = scriptContent.indexOf("DEPENDENCIES = [");
                if (dependenciesIndex != -1) {
                    int startBracket = scriptContent.indexOf("[", dependenciesIndex);
                    int endBracket = scriptContent.indexOf("]", startBracket);

                    if (startBracket != -1 && endBracket != -1) {
                        // Join the array of dependency services into a comma-separated string
                        String updatedDependencies = String.join(",", dependencyServices);

                        // Replace the old dependencies list with the updated one
                        String updatedScriptContent = scriptContent.substring(0, startBracket + 1) +
                                updatedDependencies + scriptContent.substring(endBracket);

                        scriptElement.setTextContent(updatedScriptContent);

                        log.info("Updated dependencies list: " + updatedDependencies);
                    }
                }
            }

            // Convert the modified document back to string
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            String updatedConfig = writer.toString();

            log.info("Updated XML: " + updatedConfig);

            // Update the job configuration
            client.post_xml(String.format("/job/%s/config.xml", jobPath), updatedConfig);

        } catch (Exception e) {
            throw new JenkinsException("Failed to update pipeline configuration: " + e.getMessage(), e);
        }
    }

    public static class JenkinsException extends Exception {
        public JenkinsException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public String  getJobXml(FolderJob folderName, String jobName) throws IOException {
        return jenkins.getJobXml(folderName, jobName);
    }


    public void createFolder(String folderName) throws IOException {
        jenkins.createFolder(folderName);
    }
}

