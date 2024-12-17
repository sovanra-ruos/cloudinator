#!/bin/bash

# Input parameter: folder_name
folder_name=$1

# Hardcoded values
JENKINS_URL="http://34.142.187.195:8080"
JENKINS_USER="asura"
JENKINS_API_TOKEN="11cae77a9e032f7f2ebd9b82e75aeb087e"

# Function to create Jenkins job
create_jenkins_job() {
    local folder_name=$1

    local job_name_with_prefix="microservices-deployment-pipeline"

    # Jenkins pipeline script with actual variable substitution
    local pipeline_script=$(cat <<EOF
@Library('cloudinator-microservices') _

pipeline {
    agent any
    parameters {
        string(name: 'SERVICES', defaultValue: '', description: 'Comma-separated list of services to deploy in order')
    }
    stages {
        stage('Deploy Microservices') {
            steps {
                script {
                    def services = params.SERVICES.split(',')
                    for (service in services) {
                        stage("Deploy \${service}") {
                            build job: "\${service}-pipeline", wait: true
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            echo 'Deployment of all services completed'
        }
    }
}
EOF
)

    # Job config XML
    local job_config_xml=$(cat <<EOF
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job">
  <actions/>
  <description>${job_name_with_prefix} Pipeline</description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition" plugin="workflow-cps">
    <script><![CDATA[${pipeline_script}]]></script>
    <sandbox>true</sandbox>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
EOF
)

    # Create Jenkins job via API
    curl -X POST "${JENKINS_URL}/job/${folder_name}/createItem?name=${folder_name}" \
        --user "${JENKINS_USER}:${JENKINS_API_TOKEN}" \
        -H "Content-Type: application/xml" \
        --data-raw "${job_config_xml}" || { echo "Failed to create Jenkins job in folder ${folder_name}"; exit 1; }
}

# Call function to create the job
create_jenkins_job "$folder_name"
