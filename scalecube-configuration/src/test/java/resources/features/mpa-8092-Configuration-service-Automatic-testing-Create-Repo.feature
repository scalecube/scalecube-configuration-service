@Configuration-service-production-ready

Feature: Integration tests for configuration service - CREATE (Repository).

  Configuration service enable specified users which are granted with manager's access (related API keys: Owner&Admin) to create and manage
  the separate repositories purposed for entries collection (record) and storage. Potential customers are granted by relevant permission level (API key: Member)
  just to be able to observe the stored data in the relevant repository.

  Only the manager with "Owner" API key should be able to create the specific Repository



  #CREATE REPO

  #__________________________________________________POSITIVE___________________________________________________________

  #MPA-8092 (#1)
  Scenario: Successful Repository creation applying the "Owner" API key
    Given a user have got a valid "token" (API key) with assigned "Owner" role
    When this user requested to create the "repository" with "specified" name
    Then new "repository" should be created
    And the user should get the successful response with the "empty" object


  #MPA-8092 (#1.1)
  Scenario: Successful Repositories creation with identical names applying the "Owner" API keys from different organizations
    Given the user have got a valid "token" (API key) with assigned "Owner" role issued on behalf of organization "Org-1"
    And the user have got a valid "token" (API key) with assigned "Owner" role issued on behalf of organization "Org-2"
    When this user requested to create the "repository" with "Repo-1" name applying "Owner" API key from organization "Org-1"
    And this user requested to create the "repository" with the same "Repo-1" name applying "Owner" API key from organization "Org-2"
    Then two repositories with identical names "Repo-1" should be created
    And for each request the user should get the successful response with the "empty" object


  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-8092 (#2)
  Scenario: Fail to create the Repository upon access permission is restricted applying the "Admin" either "Member" API key
    Given a user have got the valid "tokens" (API key) with "Admin" and "member" assigned roles
    When a user requested to create the "repository" with "specified" name applying each of the tokens
    Then no repositories shouldn't be created and for each of the requests user should get the "errorMessage":"Permission denied"


  #MPA-8092 (#3)
  Scenario: Fail to create the Repository with the name which already exist (duplicate) applying the "Owner" API key
    Given a user have got a valid "token" (API key) with assigned "Owner" role
    And some "repository" with "specified" name already created
    When this user requested to create the "repository" with the same "specified" name which already exists
    Then no "repository" shouldn't be created and the user should get the "errorMessage":"Repository with name: 'repo-name' already exists."


  #MPA-8092 (#4)
  Scenario: Fail to create the Repository upon the "token" is invalid (expired)
    Given a user have got an invalid "token" (API key)
    When this user requested to create the "repository" with "specified" name
    Then no repository shouldn't be created and the user should get the "errorMessage": "Token verification failed"


  #MPA-8092 (#5)
  Scenario: Fail to create the Repository upon the Owner deleted the Organization applying the "Owner" API key
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" API key which is stored there
    And the related organization "Owner" has deleted this organization
    When the user requested to create the "repository" with "specified" name applying this deleted "Owner" API key
    Then no repository shouldn't be created and the user should get an error message: "Token verification failed"


  #MPA-8092 (#6) - logic will be implemented by Architect as the nature of the API key (token) is some expiration interim
  Scenario: Fail to create the Repository upon the Owner "token" (API key) was deleted from the Organization
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" API key which is stored there
    And the related organization "Owner" has deleted the relevant "Owner" API key from it
    When the user requested to create the "repository" with "specified" name applying this deleted "Owner" API key
    Then no repository shouldn't be created
    And the user should get an error message: "Token verification failed"


  #MPA-8211 (#6.1)
  Scenario: Fail to create Repository with empty or undefined name
    Given a user have got a valid "token" (API key) with assigned "Owner" role
    When this user requested to create the "repository" with following details related to its name
      | repository |
      |            |
      | null       |
    Then new "repository" shouldn't be created
    And the user should get an error message: "Please specify a Repository name"