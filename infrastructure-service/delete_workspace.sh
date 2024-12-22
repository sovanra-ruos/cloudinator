#!/bin/bash

# Variables
JENKINS_URL="http://34.142.187.195:8080"
JENKINS_USER="asura"
JENKINS_API_TOKEN="11cae77a9e032f7f2ebd9b82e75aeb087e"


if [ -z "$1" ]; then
  echo "Usage: $0 <folder-name>"
  exit 1
fi

FOLDER_NAME="$1"

# Get CSRF crumb using XML
CRUMB=$(curl -s -u "$JENKINS_USER:$JENKINS_API_TOKEN" "$JENKINS_URL/crumbIssuer/api/xml" | grep -oPm1 "(?<=<crumb>)[^<]+")

if [ -z "$CRUMB" ]; then
  echo "Failed to retrieve CSRF crumb. Check your credentials and Jenkins configuration."
  exit 1
fi

# Jenkins API endpoint for deleting a job or folder
API_ENDPOINT="$JENKINS_URL/job/$FOLDER_NAME/doDelete"

# Send the DELETE request with the CSRF crumb
response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_ENDPOINT" \
  --user "$JENKINS_USER:$JENKINS_API_TOKEN" \
  -H "Jenkins-Crumb:$CRUMB" \
  -H "Content-Type: application/xml")

# Check the response
if [ "$response" -eq 200 ] || [ "$response" -eq 201 ] || [ "$response" -eq 302 ]; then
  echo "Folder '$FOLDER_NAME' deleted successfully."
elif [ "$response" -eq 403 ]; then
  echo "Permission denied. Check your credentials and permissions."
elif [ "$response" -eq 404 ]; then
  echo "Folder '$FOLDER_NAME' not found."
else
  echo "Failed to delete folder '$FOLDER_NAME'. HTTP response code: $response"
fi