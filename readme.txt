
CI/CD PIPELINE FOR SELENIUM UI TESTS WITH CROSS-REPOSITORY TRIGGERS



1. OVERVIEW


This project demonstrates a complete CI/CD pipeline for a Selenium Java test suite. The objective was to create a system where UI tests are automatically triggered whenever a code change occurs in a separate portfolio website repository. Test reports are then automatically sent to a Slack channel.

Workflow Visualization:

Portfolio Repo Push -> GitHub Action (Trigger) -> Test Repo GitHub Action (Run Tests) -> Selenium Tests Execute -> Slack Notification


2. TECHNOLOGY STACK


* Test Automation: Selenium WebDriver, Java, TestNG
* CI/CD: GitHub Actions
* Notifications: Slack (via Webhooks)


3. WORKFLOW BREAKDOWN


### Part 1: The Test Automation Workflow (test-repo)

This workflow, located in the test automation repository, is responsible for running the Selenium tests. It is triggered by a `repository_dispatch` event from the portfolio repository.

a. Health Check

Before running the test suite, a health check verifies that the portfolio site is online to save resources and time. This "fail-fast" approach immediately stops the workflow if the application is down.

The script uses `curl` to get the HTTP status code.
* The `-s` flag suppresses the progress meter.
* `-o /dev/null -w "%{http_code}"` discards the response body and outputs only the status code.
* The workflow exits if the status code is not 200.

CODE SNIPPET:
- name: Verify portfolio is accessible
  run: |
    response=$(curl -s -o /dev/null -w "%{http_code}" $PORTFOLIO_URL)
    if [ $response -ne 200 ]; then
      echo "Portfolio site is not accessible (HTTP $response)"
      exit 1
    fi
    echo "Portfolio site is accessible"

b. Test Execution

The workflow sets up the necessary environment, including JDK 21 and Chrome. It then runs the Maven tests in headless mode.

CODE SNIPPET:
- name: Run automation tests
  run: |
    mvn clean test -Dheadless=true

c. Slack Notifications

A notification is sent to Slack upon job completion. The `if: failure()` conditional ensures the failure notification step only runs if a previous step has failed. A JSON payload is sent to a Slack webhook URL using `curl`. This payload is dynamically populated with data received from the triggering repository .

CODE SNIPPET:
- name: Notify failure on Slack
  if: failure()
  run: |
    curl -X POST "$SLACK_WEBHOOK" \
      -H "Content-type: application/json" \
      --data '{
        "text": "🚨 Portfolio tests failed after deployment!",
        "blocks": [
          {
            "type": "header",
            "text": {
              "type": "plain_text",
              "text": "🚨 Portfolio Tests Failed"
            }
          },
          {
            "type": "section",
            "fields": [
              {
                "type": "mrkdwn",
                "text": "*Portfolio URL:*\n<${{ env.PORTFOLIO_URL }}|Check Site>"
              },
              {
                "type": "mrkdwn",
                "text": "*Triggered by:*\n${{ github.event.client_payload.pusher }}"
              },
              {
                "type": "mrkdwn",
                "text": "*Portfolio Commit:*\n`${{ github.event.client_payload.commit_sha }}`"
              },
              {
                "type": "mrkdwn",
                "text": "*Branch:*\n`${{ github.event.client_payload.branch }}`"
              }
            ]
          },
          {
            "type": "section",
            "text": {
              "type": "mrkdwn",
              "text": "*Commit Message:*\n${{ github.event.client_payload.commit_message }}"
            }
          },
          {
            "type": "actions",
            "elements": [
              {
                "type": "button",
                "text": {
                  "type": "plain_text",
                  "text": "View Error Logs"
                },
                "url": "${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}",
                "style": "danger"
              }
            ]
          }
        ]
      }'

### Part 2: The Trigger Workflow (portfolio-repo)

This workflow, located in the portfolio repository, triggers the test workflow whenever there is a code change on the `main` branch or after a deployment workflow completes.

a. Constructing and Sending the Payload

When triggered, the workflow gathers context from the event, such as the commit SHA, message, and author. It uses `jq` to safely construct a `client_payload` JSON object containing this dynamic data.

This payload is then sent to the GitHub API to create a `repository_dispatch` event in the test repository. This API call is authenticated using a Personal Access Token (PAT) stored as `secrets.TEST_REPO_TOKEN`.

CODE SNIPPET:
- name: Trigger tests in test repository
  run: |
    if [ "${{ github.event_name }}" == "workflow_run" ]; then
      COMMIT_SHA="${{ github.event.workflow_run.head_sha }}"
      COMMIT_MESSAGE="${{ github.event.workflow_run.head_commit.message }}"
      BRANCH="${{ github.event.workflow_run.head_branch }}"
      PUSHER="${{ github.event.workflow_run.head_commit.author.name }}"
    else
      COMMIT_SHA="${{ github.sha }}"
      COMMIT_MESSAGE="${{ github.event.head_commit.message }}"
      BRANCH="${{ github.ref_name }}"
      PUSHER="${{ github.event.head_commit.author.name }}"
    fi

    jq -n \
      --arg event_type "portfolio-updated" \
      --arg portfolio_url "https://arijit06.github.io/ArijitSinghaRoy/" \
      --arg commit_sha "$COMMIT_SHA" \
      --arg commit_message "$COMMIT_MESSAGE" \
      --arg branch "$BRANCH" \
      --arg pusher "$PUSHER" \
      '{
        "event_type": $event_type,
        "client_payload": {
          "portfolio_url": $portfolio_url,
          "commit_sha": $commit_sha,
          "commit_message": $commit_message,
          "branch": $branch,
          "pusher": $pusher
        }
      }' > payload.json

    response=$(curl -s -w "HTTP_STATUS:%{http_code}" -X POST \
    -H "Authorization: token ${{ secrets.TEST_REPO_TOKEN }}" \
    -H "Accept: application/vnd.github.v3+json" \
    -H "Content-Type: application/json" \
    https://api.github.com/repos/Arijit06/CI-CD-Integration-with-Selenium-Java-/dispatches \
    --data @payload.json)

    http_status=$(echo $response | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
    if [ "$http_status" -eq 204 ]; then
      echo "SUCCESS: Test trigger sent successfully!"
    else
      echo "ERROR: Failed to trigger tests. HTTP Status: $http_status"
      exit 1
    fi


4. KEY LEARNINGS


* Thread Safety in Parallel Testing: Refactored test code to use `ThreadLocal<WebDriver>` and `ThreadLocal<WebDriverWait>` to enable thread-safe parallel execution of Selenium tests.
* GitHub Actions for CI/CD: Learned to create workflow files with jobs and steps, manage sensitive data like Slack webhook URLs using secrets, and access `client_payload` data from a dispatch event using `github.event.client_payload`.
* Cross-Repository Communication: Mastered the use of `repository_dispatch` events to trigger a workflow in one repository from another. This involved creating a PAT for authorization and constructing a JSON payload with `jq` to pass dynamic context between the workflows.
* Robust CI Practices: Implemented a fail-fast health check using `curl` to avoid wasting runner time on a down application. Also learned to construct rich, context-aware Slack notifications that provide immediate and actionable feedback on test outcomes.