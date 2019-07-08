@Configuration-service-integration-tests

Feature: Integration tests for configuration service - createRepository.

  Configuration service enable specified users which are granted with manager's access (related apiKeys: Owner&Admin) to create
  the separate repositories purposed for entries collection (record) and storage.

  Only the manager with "Owner" apiKey should be able to create the specific Repository


  Background: no repositories are stored in the system but having apiKeys
    Given the are no repositories stored in the system
    And organization "organizationId" with "Org-1" name already created
    And the related apiKeys with "Owner", "Admin" and "Member" roles are stored in Organization "Org-1"

  #CREATE REPO

  #__________________________________________________POSITIVE___________________________________________________________

  #1
  Scenario: Successful Repository creation applying the "Owner" apiKey
    When the user requested to create the "repository" with "specified" name applying apiKey with "Owner" role
    Then new "repository" should be created
    And the user should get the successful response with the "empty" object


  #1.1
  Scenario: Successful Repositories creation with identical names by different organizations applying "Owner" apiKey
    Given the user have issued "Owner" apiKey on behalf of organization "Org-2"
    When the user requested to create the "repository" with name "Repo-1" applying "Owner" apiKey from organization "Org-1"
    And the user requested to create the "repository" with the same name "Repo-1" applying "Owner" apiKey from organization "Org-2"
    Then two repositories with identical names "Repo-1" should be created
    And for each request the user should get the successful response with the "empty" object


  #__________________________________________________NEGATIVE___________________________________________________________

  #2
  Scenario: Fail to create the Repository upon access permission is restricted for the "Admin" either "Member" apiKey
    When a user requested to create the "repository" with "specified" name applying the "Admin" and "Member"  apiKeys
    Then for each of the requests user should get the "errorMessage":"Permission denied"


  #3
  Scenario: Fail to create the Repository with duplicate name for a single Organization applying the "Owner" apiKey
    Given some "repository" with "Repo-1" name already created
    When the user requested to create the "repository" with the same "Repo-1" name applying the "Owner" apiKey
    Then the user should get the "errorMessage":"Repository with name: 'repo-name' already exists."


  #4
  Scenario: Fail to create the Repository upon the Owner deleted the Organization
    Given the related organization Owner has deleted organization "Org-1"
    And related apiKeys were automatically deleted
    When the user requested to create the "repository" with "specified" name applying the "Owner" apiKey from deleted Organization
    Then the user should get an error message: "Token verification failed"


  #5
  Scenario: Fail to create the Repository upon the Owner apiKey was deleted from the Organization
    Given organization "Org-1" Owner has deleted the relevant "Owner" apiKey from it
    When the user requested to create the "repository" with "specified" name applying this deleted "Owner" apiKey
    Then the user should get an error message: "Token verification failed"


  #6
  Scenario: Fail to create Repository with empty or undefined name
    When the user requested to create the "repository" without specifying its name
      | apiKey      | repository |
      | valid-Owner |            |
      | valid-Owner | null       |
    And the user requested to create the "repository" without "repository" key name at all
    Then for each request the user should get an error message: "Please specify 'repository'"


  #7
  Scenario: Fail to create Repository with empty or undefined apiKey
    When the user requested to create the "repository" without specifying the apiKey
      | apiKey | repository |
      |        | Repo-1     |
      | null   | Repo-1     |
    And the user requested to create the "repository" without "apiKey" at all
      | repository |
      | Repo-1     |
    Then for each request the user should get an error message: "Please specify 'apiKey'"