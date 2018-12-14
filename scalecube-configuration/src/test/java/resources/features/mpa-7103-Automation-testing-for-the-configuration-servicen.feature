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


  As a user I would like to manage instruments instances in the configuration service

  For example:

  - add a new instrument instance with the same structure
  - edit the following metadata for instrument instance: DecimalPrecision, Rounding, name, rounding
  - remove and existing instrument instance configuration

  #_____________________________________________________CRUD____________________________________________________________

  #MPA-7103 (#1)
  Scenario: Successful Repo creation
    Given a user have got a valid "token" (API key) with assigned "owner" role
    When this user requested to create the "repository" with "specified" name
    Then new "repository" should be created and stored in DB


  #MPA-7103 (#1.1)
  Scenario: Fail to create the Repo upon access permission is restricted
    Given the users have got the valid "tokens" (API key) with "admin" and "member" assigned roles
    When each of these users requested to create the "repository" with "specified" name
    Then any of repositories shouldn't be created and each of the user should get an error message: "role: "userId" not in role Owner"


  #MPA-7103 (#1.2)
  Scenario: Fail to create the Repo upon a "token" is invalid (expired)
    Given a user have got an invalid "token" (API key)
    When this user requested to create the "repository" with "specified" name
    Then new "repository" shouldn't be created and the user should get an error message: "Token verification failed"


  #MPA-7103 (#1.3)
  Scenario: Fail to create the Repo with the name which already exist (duplicate)
    Given a user have got a valid "token" (API key) with assigned "owner" role
    And "repository" with "specified" name already created and stored in DB
    When this user requested to create the "repository" with the same "specified" name which already exists
    Then new "repository" shouldn't be created and the user should get an error message: "repository": "specified" name already exists"




  #MPA-7103 (#2)
  Scenario: Successful save (edit) the value for specific entity assigned by "Owner" either "Admin" roles
    Given the users "A"and "B" have got a valid "tokens" (API key) with "owner" and "admin" assigned roles accordingly
    And the relevant specified name "repository" was created and stored in DB
    When each of these users requested to save the "specific" entity (key and value) in the relevant "repository" specified name
    Then both new entities (keys and values) should be created and stored in the relevant "repository" of the DB


  #MPA-7103 (#2.1)
  Scenario: Successful update (override - edit) the value for specific entity which already exist
    Given a user have got a valid "tokens" (API key) with assigned "admin" and "owner" roles
    And relevant "repositories" with "specified" names "Repo1" and "Repo2" already created and stored in DB with some "specific" entities
    When this user requested to save (update) the these "specific" entities (key or value) in the relevant "repositories" name "Repo1" and "Repo2" applying the "admin" and "owner" tokens
    Then a new values should be set to the given keys and stored in the relevant "repositories" of the DB


  #MPA-7103 (#2.2)
  Scenario: Fail to save (edit) the value for specific entity assigned by "Member" role
    Given a user have got a valid "token" (API key) with assigned "member" role
    And the relevant specified name "repository" was created and stored in DB
    When this user requested to save the "specific" entity (key and value) in the relevant "repository" specified name
    Then this new entity (key and value) shouldn't be created and the user should get an error message: "role: "userId" not in role Owner or Admin"


  #MPA-7103 (#2.3)
  Scenario: Fail to save (edit) the value for specific entity upon the Repo doesn't exist
    Given a user have got a valid "token" (API key) with assigned "admin" role
    And there is no "repository" created and stored in DB
    When this user requested to save the entity (key and value) to non-existent "repository" "specified" name
    Then this new entity (key and value) shouldn't be created and the user should get the error message: "repository: "specified" repository doesn't exist"



  #MPA-7103 (#3)
  Scenario: Successful get of a specific entity from the Repo
    Given the users have got a valid "tokens" (API key) with "owner" and "admin" and "member" assigned roles
    And the relevant specified name "repository" with relevant entity (key and value) were created and stored in DB
    When each of these users requested to get the existent entity (key and value) stored in the relevant "repository" specified name
    Then each of these users should receive successful response with existent entity (key and value)


  #MPA-7103 (#3.1)
  Scenario: Successful get the list of the stored entities from the Repo
    Given the users have got a valid "tokens" (API key) with "owner" and "admin" and "member" assigned roles
    And the relevant specified name "repository" with relevant (at least one) entities (keys and values) were created and stored in DB
    When each of these users requested to get the list of the existent entities (keys and values) which already exists in the relevant "repository" specified name
    Then each of these users should receive successful response with list of existent entities (keys and values)


  #MPA-7103 (#3.2)
  Scenario: Successfully get nothing upon stored entity was deleted from the Repo
    Given a user have got a valid "token" (API key) with assigned "Owner" role
    And the relevant specified name "repository" with some entity was created and stored in DB
    And this user deleted the "specific" entity (key and value) in the relevant "repository" specified name
    When this user requested to get the recently deleted entity (key and value) from the relevant "repository" specified name
    Then this user should receive successful response with empty object


  #MPA-7103 (#3.3)
  Scenario: Fail to get a non-existent entity from the Repo
    Given the users have got a valid "token" (API key) with "member" assigned role
    And there is no "repository" created and stored in DB
    When this user requested to get the non-existent entity (key) from non-existent "repository" specified name
    Then this user shouldn't receive any of the stored entities and get the error message: "repository: "specified" repository doesn't exist"




  #MPA-7103 (#4)
  Scenario: Successful delete (remove) of a specific entity from the Repo upon Admin either Owner permission was granted
    Given the users have got a valid "tokens" (API key) with "owner" and "admin" assigned roles
    And the relevant specified name "repository" with relevant entities (keys and values) were created and stored in DB
    When each of these users requested to delete some of the existent entities (key and value) which is stored in the relevant "repository" specified name
    Then each of these users should receive successful response with empty object


  #MPA-7103 (#4.1)
  Scenario: Fail to delete (remove) a specific entity from the Repo upon the restricted permission as Member
    Given the user have got a valid "token" (API key) with "member" assigned role
    And the relevant specified name "repository" with relevant entity (key and value) were created and stored in DB
    When this user requested to delete the existent entity (key and value) which is stored in the relevant "repository" specified name
    Then this entity shouldn't be removed from the "repository" and this user should receive an error message: "role: permission denied"


  #MPA-7103 (#4.2)
  Scenario: Fail to delete (remove) a non-existent entity from the Repo
    Given the user have got a valid "token" (API key) with "admin" assigned role
    And the relevant specified name "repository" with relevant entity (key and value) were created and stored in DB
    When this user requested to delete the non-existent entity (key and value)
    Then any of the existent entity shouldn't be removed from the "repository" and this user should receive an error message: "role: "userId" not in role Owner or Admin"


  #___________________________________________________INSTANCE__________________________________________________________

  As a user I would like to manage instruments instances in the configuration service

  For example:

  - add a new instrument instance with the same structure
  - edit the following metadata for instrument instance: DecimalPrecision, Rounding, name, rounding
  - remove and existing instrument instance configuration

  #MPA-7103 (#5)
  Scenario: Successful save of specific instrument instance entity
    Given the user have been granted with one of the valid "tokens" (API key) assigned by "owner" either "admin" role
    And the relevant specified name "repository" was created and stored in DB
    When this user requested to save the "instrumentInstance" entity in the relevant "repository" specified name with following details
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And this user requested to save another entity "value" in the relevant "repository" specified name with following details
      | instrumentId | name      | DecimalPrecision | Rounding | key                        |
      | USD          | USDollar  | 4                | down     | KEY-FOR-CURRENCY-456       |
      | XPD          | Palladium | 4                | down     | KEY-FOR-PRECIOUS-METAL-789 |
    Then all new entities should be created and stored in the relevant "repository" of the DB


  #MPA-7103 (#5.1)
  Scenario: Successful save the same instrument instance entity values for different Repos
    Given  relevant "repositories" with "specified" names "Repo1" and "Repo2" already created and stored in DB without any entity
    And the user have been granted with valid "tokens" (API key) with "owner" and "admin" assigned roles accordingly
    When this user assigned by "admin" role requested to save the following "instrumentInstance" entity in "repository" name "Repo1"
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And this user assigned by "owner" role requested to save the following "instrumentInstance" entity in "repository" name "Repo2"
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    Then all new entities should be created and stored in the relevant "repositories" of the DB


  #MPA-7103 (#5.2)
  Scenario: Successful save/update (override - edit) the value for existent instrument instance entity in single Repo
    Given the user have been granted with valid "token" (API key) assigned by "admin" role
    And the relevant specified name "repository" was created and stored in DB
    And following "instrumentInstance" entity already stored in this "repository" name
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to save the "instrumentInstance" entity in the relevant "repository" name with following details
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    Then existent entity values shouldn't be duplicated thus new values should be set to the given keys and stored in the relevant "repositories" of the DB


 #MPA-7103 (#5.3)
  Scenario: Successful save the instrument instance entity with values that reach over a 1000 chars (no quantity validation for input)
    Given the user have been granted with one of the valid "tokens" (API key) assigned by "owner" either "admin" role
    And the relevant specified name "repository" was created and stored in DB
    When this user requested to save the "instrumentInstance" entity in the relevant specified name "repository" with following details
      | instrumentId                                                                                           | name     | DecimalPrecision | Rounding | key                        |
      | XPTTTTTTTTTTTTT(_OVER 1000 CHARS)TTTTTTTTXPTTTTTTTTTTTTTTTTTTTTTTTXPTTTTTTTTTTTTTTTTTTTTTTTXPTTTTT.... | Platinum | 5                | up       | KEY-FOR-PRECIOUS-METAL-123 |
    Then new entitiy should be created and stored in the relevant "repository" of the DB


 #MPA-7103 (#5.4)
  Scenario: Successful save the instrument instance entity with values which chats are symbols and spaces (no chars validation for input)
    Given the user have been granted with one of the valid "tokens" (API key) assigned by "owner" either "admin" role
    And the relevant specified name "repository" was created and stored in DB
    When this user requested to save the "instrumentInstance" entity in the relevant specified name "repository" with following details
      | instrumentId | name        | DecimalPrecision | Rounding    | key                        |
      | #!=`   ~/.*  | #!=   `~/.* | #!=`   ~/.*      | #!=`~   /.* | KEY-FOR-PRECIOUS-METAL-123 |
    Then new entitiy should be created and stored in the relevant "repository" of the DB


  #MPA-7103 (#5.5)
  Scenario: Fail to save a specific instrument instance entity upon the restricted permission for Member
    Given the user have been granted with valid "token" (API key) assigned by "member" role
    And the relevant specified name "repository" was created and stored in DB
    When this user requested to save the "instrumentInstance" entity in the relevant "repository" specified name with following details
      | instrumentId | name | DecimalPrecision | Rounding | key                  |
      | JPY          | Yen  | 4                | down     | KEY-FOR-CURRENCY-999 |
    Then this new entity shouldn't be created and the user should get an error message: "role: "userId" not in role Owner or Admin"


 #MPA-7103 (#5.6)
  Scenario: Fail to update a non-existent instrument instance entity
    Given the user have been granted with valid "token" (API key) assigned by "owner" either "admin" role
    And the is no repository was created and stored in the DB
    And only single specific "instrumentInstance" entity stored in this "repository"
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to delete non-existent "instrumentInstance" entity from the relevant specified name "repository"
      | key          |
      | NON-EXISTENT |
    Then any of the existent entity shouldn't be removed from the "repository" and this user should get the error message: "key: "specified" name doesn't exist"


 
  #MPA-7103 (#6)
  Scenario: Successful get of the existent instrument instance entity
    Given the user have been granted with one of the valid "tokens" (API key) assigned by "owner" either "admin" either "member" role
    And the relevant specified name "repository" was created and stored in DB
    And specific "instrumentInstance" entity already updated and stored in this "repository"
      | instrumentId | name     | DecimalPrecision | Rounding | key                        |
      | XPT          | Platinum | 5                | up       | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to get this "instrumentInstance" entity from the relevant specified name "repository"
      | key                        |
      | KEY-FOR-PRECIOUS-METAL-123 |
    Then this user should receive successful response with relevant entity which is stored in the relevant specified name "repository" of the DB


  #MPA-7103 (#6.1)
  Scenario: Fail to get the non-existent instrument instance entity
    Given the user have been granted with one of the valid "tokens" (API key) assigned by "owner" either "admin" either "member" role
    And the relevant specified name "repository" was created and stored in DB
    And only single specific "instrumentInstance" entity stored in this "repository"
      | instrumentId | name     | DecimalPrecision | Rounding | key                        |
      | XPT          | Platinum | 5                | up       | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to get non-existent "instrumentInstance" entity from the relevant specified name "repository"
      | key          |
      | NON-EXISTENT |
    Then this user shouldn't receive any of the stored entities and get the error message: "key: "specified" name doesn't exist"


  #MPA-7103 (#6.2)
  Scenario: Successful get of the all existent entities list
    Given the user have been granted with one of the valid "tokens" (API key) assigned by "owner" either "admin" either "member" role
    And the relevant specified name "repository" was created and stored in DB
    And specific entities already stored in this "repository"
      | instrumentId | name      | DecimalPrecision | Rounding | key                        |
      | XPT          | Platinum  | 5                | up       | KEY-FOR-PRECIOUS-METAL-123 |
      | USD          | USDollar  | 4                | down     | KEY-FOR-CURRENCY-456       |
      | XPD          | Palladium | 4                | down     | KEY-FOR-PRECIOUS-METAL-789 |
    When this user requested to get the list of all the stored entities from the relevant specified name "repository"
    Then this user should receive successful response with the list of all the entities which are stored in the relevant specified name "repository" of the DB



  #MPA-7103 (#7)
  Scenario: Successful delete of the specific instrument instance entity
    Given the user have been granted with valid "token" (API key) assigned by "owner" either "admin" role
    And the relevant specified name "repository" was created and stored in DB
    And specific "instrumentInstance" entity stored in the relevant "repository" specified name
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to delete this specific "instrumentInstance" entity from the relevant specified name "repository"
    Then this user should receive successful response with empty object


  #MPA-7103 (#7.1)
  Scenario: Fail to delete the specific instrument instance entity
    Given the user have got a valid "token" (API key) with "member" assigned role
    And the relevant specified name "repository" was created and stored in DB
    And specific "instrumentInstance" entity stored in the relevant "repository" specified name
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to delete this specific "instrumentInstance" entity from the relevant specified name "repository"
    Then this entity shouldn't be removed from the "repository" and this user should receive an error message: "role: "userId" not in role Owner or Admin"


  #MPA-7103 (#7.2)
  Scenario: Fail to delete a non-existent instrument instance entity
    Given the user have been granted with valid "token" (API key) assigned by "owner" either "admin" role
    And the relevant specified name "repository" was created and stored in DB
    And only single specific "instrumentInstance" entity stored in this "repository"
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to delete non-existent "instrumentInstance" entity from the relevant specified name "repository"
      | key          |
      | NON-EXISTENT |
    Then any of the existent entity shouldn't be removed from the "repository" and this user should get the error message: "key: "specified" name doesn't exist"
