@Configuration-service-production-ready

Feature: Basic CRUD tests for configuration service.

  We need to enable the following operation to use. Note that delete&write could the owner/admin
  and the member can read and list (owner and admin are members).

  Enable to add new configuration values:
  - Add
  - Edit (save permission)
  - Get a specific value
  - Get list
  - Remove

  Authorization:

  - try to add an entry you have permission to add and succeed with the change
  - try to add an entry you don't have permission to add but have permission to view and receive an error

  - try to edit a value you permission to edit and succeed with the change
  - try to edit a value you don't have permission to edit but have permission to view and receive an error

  - try to get the value you don't have permission to access and receive an error (a member can read and list (owner and admin are members))

  - try to remove an entry you permission to remove and succeed with the change
  - try to remove an entry you don't have permission to remove but have permission to view and receive an error


  #MPA-7103 (#1)
  Scenario: Successful Repo creation
    Given a user have got a valid token (API key) with assigned "owner" role
    When this user requested to create the repository with "specified" name
    Then new repository should be created and stored in DB


  #MPA-7103 (#1.1)
  Scenario: Fail to create the Repo upon access permission is restricted
    Given the users have got the valid tokens (API key) with "admin" and "member" assigned roles
    When each of these users requested to create the repository with "specified" name
    Then any of repositories shouldn't be created and each of the user should get an error message: "not the owner"


  #MPA-7103 (#1.2)
  Scenario: Fail to create the Repo upon a token is invalid (expired)
    Given a user have got an invalid token (API key)
    When this user requested to create the repository with "specified" name
    Then new repository shouldn't be created and the user should get an error message: "Token verification failed"


  #MPA-7103 (#1.3)
  Scenario: Fail to create the Repo with the name which already exist (duplicate)
    Given a user have got a valid token (API key) with assigned "owner" role
    And repository with "specified" name already created and stored in DB
    When this user requested to create the repository with the same "specified" name which already exists
    Then new repository shouldn't be created and the user should get an error message: "repository: "specified" name already exists"




  #MPA-7103 (#2)
  Scenario: Successful save (edit) the write permission for specific entity assigned by "Owner" and "Admin" roles
    Given the users have got a valid tokens (API key) with "owner" and "admin" assigned roles
    And the relevant "specified" name repository was created and stored in DB
    When each of these users requested to save the "specific" entity (key and value) with write permission for the relevant repository "specified" name
    Then both new entities (keys and values) should be created and stored in the relevant repository of the DB


  #MPA-7103 (#2.1)
  Scenario: Fail to save (edit) the write permission for specific entity assigned by "Member" role
    Given a user have got a valid token (API key) with assigned "member" role
    And the relevant "specified" name repository was created and stored in DB
    When this user requested to save the "specific" entity (key and value) with write permission for the relevant repository "specified" name
    Then this new entity (key and value) shouldn't be created and the user should get an error message: "role: permission denied"


  #MPA-7103 (#2.2)
  Scenario: Fail to save (edit) the write permission for specific entity which already exist (duplicate)
    Given a user have got a valid token (API key) with assigned "owner" or "admin" roles
    And repository with "specified" name already created and stored in DB
    When this user requested to save the same entity (key or value) which already exists with write permission for the relevant repository "specified" name
    Then this new entity (key or value) shouldn't be created
    And the user should get one of the error messages: "key: "specified" key name already exists" or "value": "specified" value name already exists"


  #MPA-7103 (#2.3)
  Scenario: Fail to save (edit) the write permission for specific entity upon the Repo doesn't exist
    Given a user have got a valid token (API key) with assigned "admin" role
    And some relevant repository with "specified" name already created and stored in DB
    When this user requested to save the entity (key and value) to non-existent repository "specified" name
    Then this new entity (key and value) shouldn't be created and the user should get the empty object



  #MPA-7103 (#3)
  Scenario: Successful get of a specific entity from the Repo
    Given the users have got a valid tokens (API key) with "owner" and "admin" and "member" assigned roles
    And the relevant "specified" name repository with relevant entity (key and value) were created and stored in DB
    When each of these users requested to get the existent entity (key and value) stored in the relevant repository "specified" name
    Then each of these users should receive successful response with existent entity (key and value)


  #MPA-7103 (#3.1)
  Scenario: Successful get the list of a the stored entities from the Repo
    Given the users have got a valid tokens (API key) with "owner" and "admin" and "member" assigned roles
    And the relevant "specified" name repository with relevant (at least one) entities (keys and values) were created and stored in DB
    When each of these users requested to get the list of the existent entities (keys and values) which already exists in the relevant repository "specified" name
    Then each of these users should receive successful response with list of existent entities (keys and values)


  #MPA-7103 (#3.2)
  Scenario: Fail to get a non-existent entity from the Repo
    Given the users have got a valid token (API key) with "member" assigned role
    And the relevant "specified" name repository with relevant entity (key and value) were created and stored in DB
    When this user requested to get the non-existent entity (key) from the relevant repository "specified" name
    Then this user shouldn't receive any of the stored entities and get the empty object




  #MPA-7103 (#4)
  Scenario: Successful delete (remove) of a specific entity from the Repo upon the write permission was granted
    Given the users have got a valid tokens (API key) with "owner" and "admin" assigned roles
    And the relevant "specified" name repository with relevant entity (key and value) were created and stored in DB
    When each of these users requested to delete the existent entity (key and value) which is stored in the relevant repository "specified" name
    Then each of these users should receive successful response with empty object


  #MPA-7103 (#4.1)
  Scenario: Fail to delete (remove) a specific entity from the Repo upon the write permission was granted
    Given the user have got a valid token (API key) with "member" assigned role
    And the relevant "specified" name repository with relevant entity (key and value) were created and stored in DB
    When this user requested to delete the existent entity (key and value) which is stored in the relevant repository "specified" name
    Then this entity shouldn't be removed from the DB and this user should receive an error message: "role: permission denied"


  #MPA-7103 (#4.2)
  Scenario: Fail to delete (remove) a non-existent entity from the Repo
    Given the user have got a valid token (API key) with "admin" assigned role
    And the relevant "specified" name repository with relevant entity (key and value) were created and stored in DB
    When this user requested to delete the non-existent entity (key and value)
    Then any of the existent entity shouldn't be removed from the DB and this user should receive an empty object