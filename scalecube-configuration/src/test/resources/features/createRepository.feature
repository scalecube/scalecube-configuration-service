@Configuration-service-integration-tests

Feature: Integration tests for configuration service - createRepository.

  Configuration service enable specified users which are granted with manager's access (related apiKeys: Owner&Admin) to create
  the separate repositories purposed for entries collection (record) and storage.

  Only the manager with "Owner" apiKey should be able to create the specific Repository


  Background: no repositories are stored in the system but having apiKeys
    Given the are no repositories stored in the system
    And organizations with specified names "Org-1" and "Org-2" already created
    And the related apiKeys with "Owner", "Admin" and "Member" roles are stored in Organization "Org-1"
    And the related apiKey with "Owner" role stored in Organization "Org-2"
  #CREATE REPO

  #__________________________________________________POSITIVE___________________________________________________________

  #1
  Scenario: Successful Repository creation applying the "Owner" apiKey
    When the user requested to createRepository with following details
      | apiKey      | repository |
      | Owner-Org-1 | Repo-1     |
    Then new "repository" should be created
    And the user should get the successful response with the "empty" object


  #2
  Scenario: Successful Repositories creation with identical names by different organizations applying "Owner" apiKey
    When the user requested to create repositories with identical name applying "Owner" apiKeys from organization "Org-1" and "Org-2"
      | apiKey      | repository |
      | Owner-Org-1 | Repo-2     |
      | Owner-Org-2 | Repo-2     |
    Then two repositories with identical names "Repo-2" should be created
    And for each request the user should get the successful response with the "empty" object


  #__________________________________________________NEGATIVE___________________________________________________________

  #3
  Scenario: Fail to create the Repository upon access permission is restricted for the "Admin" either "Member" apiKey
    When a user requested to create the "repository" applying the "Admin" and "Member" apiKeys
      | apiKey       | repository |
      | Admin-Org-1  | Repo-1     |
      | Member-Org-1 | Repo-1     |
    Then for each request user should get following error
      | errorCode | errorMessage      |
      | 500       | Permission denied |

  #4
  Scenario: Fail to create the Repository with duplicate name for a single Organization applying the "Owner" apiKey
    Given the repository with name "Repo-1" already created
    When the user requested to create repository with identical name applying "Owner" apiKey from organization "Org-1"
      | apiKey      | repository |
      | Owner-Org-1 | Repo-1     |
    Then the user should get following error
      | errorCode | errorMessage                                     |
      | 500       | Repository with name: 'repo-name' already exists |


  #5
  Scenario: Fail to create the Repository upon the Owner deleted the "Organization"
    Given the related organization Owner has deleted organization name "Org-2"
    When the user requested to create repository applying "Owner" apiKey from the deleted organization "Org-2"
      | apiKey      | repository |
      | Owner-Org-2 | Repo-3     |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #6
  Scenario: Fail to create the Repository upon the "Owner" apiKey was deleted from the Organization
    Given organization "Org-1" Owner has deleted the relevant "Owner" apiKey from it
    When the user requested to create repository applying the deleted "Owner" apiKey from organization "Org-1"
      | apiKey      | repository |
      | Owner-Org-1 | Repo-3     |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #7
  Scenario: Fail to create the Repository due to invalid apiKey was applied
    When the user requested to createRepository applying the "invalid" apiKey
      | apiKey  | repository |
      | invalid | Repo-3     |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #8
  Scenario: Fail to create Repository with empty or undefined name
    When the user requested to createRepository without specifying its name
      | apiKey      | repository |
      | Owner-Org-1 |            |
      | Owner-Org-1 | null       |
    And the user requested to createRepository without "repository" key name at all
      | apiKey      |
      | Owner-Org-1 |
    Then for each request user should get following error
      | errorCode | errorMessage                |
      | 500       | Please specify 'repository' |


  #9
  Scenario: Fail to create Repository with empty or undefined apiKey
    When the user requested to createRepository without specifying the apiKey
      | apiKey | repository |
      |        | Repo-3     |
      | null   | Repo-3     |
    And the user requested to createRepository without "apiKey" key at all
      | repository |
      | Repo-3     |
    Then for each request user should get following error
      | errorCode | errorMessage            |
      | 500       | Please specify 'apiKey' |