@Configuration-service-production-ready

Feature: Integration tests for configuration service.

  Configuration service enable specified users which are granted with manager's access (related API keys: Owner&Admin) to create and manage
  the separate repositories purposed for entries collection (record) and storage. Potential customers are granted by relevant permission level (API key: Member)
  just to be able to observe the stored data in the relevant repository.

  For example managers could:

  - add a new instrument instance with the some structure
  - edit the following metadata for instrument instance: DecimalPrecision, Rounding, name, rounding
  - remove and existing instrument instance configuration
  - read (get) the specified and all the entries which were stored in the related repo

  For example customers (members) could only:
  - get the specified and all the entries which were stored in the related repo by the managers



  #CREATE REPO

  #__________________________________________________POSITIVE___________________________________________________________

  #MPA-8092 (#1)
  Scenario: Successful Repo creation applying the "Owner" API key
    Given a user have got a valid "token" (API key) with assigned "Owner" role
    When this user requested to create the "repository" with "specified" name
    Then new "repository" should be created and stored in DB


  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-8092 (#1.1)
  Scenario: Fail to create the Repo upon access permission is restricted applying the "Admin" either "Member" API key
    Given a user have got the valid "tokens" (API key) with "Admin" and "member" assigned roles
    When a user requested to create the "repository" with "specified" name applying each of the tokens
    Then no repositories shouldn't be created and for each of the requests user should get the "errorMessage":"Permission denied"


  #MPA-8092 (#1.2)
  Scenario: Fail to create the Repo with the name which already exist (duplicate) applying the "Owner" API key
    Given a user have got a valid "token" (API key) with assigned "Owner" role
    And some "repository" with "specified" name already created and stored in DB
    When this user requested to create the "repository" with the same "specified" name which already exists
    Then no "repository" shouldn't be created and the user should get the "errorMessage":"Repository with name: 'repo-name' already exists."


  #MPA-8092 (#1.3)
  Scenario: Fail to create the Repo upon the "token" is invalid (expired)
    Given a user have got an invalid "token" (API key)
    When this user requested to create the "repository" with "specified" name
    Then no repository shouldn't be created and the user should get the "errorMessage": "Token verification failed"


  #MPA-8092 (#1.4)
  Scenario: Fail to create the Repo upon the Owner deleted the Organization applying the "Owner" API key
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" API key which is stored there
    And the related organization "Owner" has deleted this organization
    When the user requested to create the "repository" with "specified" name applying this deleted "Owner" API key
    Then no repository shouldn't be created and the user should get an error message: "Token verification failed"


  #MPA-8092 (#1.5) - logic will be implemented by Architect as the nature of the API key (token) is some expiration interim
  Scenario: Fail to create the Repo upon the Owner "token" (API key) was deleted from the Organization
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" API key which is stored there
    And the related organization "Owner" has deleted the relevant "Owner" API key from it
    When the user requested to create the "repository" with "specified" name applying this deleted "Owner" API key
    Then no repository shouldn't be created and the user should get an error message: "Token verification failed"



  #SAVE (UPDATE/EDIT) THE ENTRY IN THE REPO

  #__________________________________________________POSITIVE___________________________________________________________


  #MPA-8092 (#2)
  Scenario: Successful save of specific entry (instrument) applying the "Owner" API key
    Given the specified name "repository" was created and stored in DB
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to save the specified entry in the relevant "repository" specified name with following details
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    Then all new entries should be created and stored in the relevant "repository" of the DB
    And the user should get the successful response with the "empty" object


  #MPA-8092 (#2.1)
  Scenario: Successful save the entry (instrument) for different Repos applying the "Admin" API key
    Given  the "repositories" with "specified" names "Repo1" and "Repo2" already created and stored in DB without any entry
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to save the following specified entry in repository with name "Repo1"
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And this user requested to save the following specified entry in repository with name "Repo2"
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    Then both identical entries should be created and stored in the relevant repositories "Repo1" and "Repo2" of the DB
    And for the each request user should get the successful response with the "empty" object


  #MPA-8092 (#2.2)
  Scenario: Successful update (save/override - edit) the existing entries in the single Repo applying the "Owner" API key
    Given the "repositories" with "specified" names "Repo1" and "Repo2" already created and stored in DB with following identical entries
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to update (save) one of identical entries in the repository name with name "Repo1" with following details
      | instrumentId | name     | DecimalPrecision | Rounding | key                        |
      | XPT          | Platinum | 2                | up       | KEY-FOR-PRECIOUS-METAL-124 |
    Then new entries should be set to the given "key" and stored in the relevant repository with name "Repo1" in the DB
    And the user should get the successful response with the "empty" object
    But existent entry in the repository name with name "Repo2" shouldn't be updated


  #MPA-8092 (#2.3)
  Scenario: No change for the successful update (save/override - edit) of the existing entry with the same values applying the "Admin" API key
    Given the specified name "repository" was created and stored in DB with following entry
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to update (save) the existent entry in the relevant "repository" with the same values
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    Then existent entry shouldn't be duplicated thus new values should be set to the given key and stored in the relevant "repository" of the DB
    And the user should get the successful response with the "empty" object


  #MPA-8092 (#2.4)
  Scenario: Successful save the specific entries applying the "Owner" API key for:
  - values that reach over (>=) a 1000 chars (no quantity validation for input)
  - values which chars are symbols and spaces (no chars validation for input)
    Given the specified name "repository" was created and stored in DB without any stored entry
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to save the entries in the relevant specified name "repository" with following details
      | instrumentId                                                                                           | name        | DecimalPrecision | Rounding    | key                        |
      | XPTTTTTTTTTTTTT(_OVER 1000 CHARS)TTTTTTTTXPTTTTTTTTTTTTTTTTTTTTTTTXPTTTTTTTTTTTTTTTTTTTTTTTXPTTTTT.... | Platinum    | 5                | up          | KEY-FOR-PRECIOUS-METAL-123 |
      | #!=`   ~/.*                                                                                            | #!=   `~/.* | #!=`   ~/.*      | #!=`~   /.* | KEY-FOR-PRECIOUS-METAL-124 |
    Then new entries should be created and stored in the relevant "repository" of the DB
    And for each request the user should get the successful response with the "empty" object


  #__________________________________________________NEGATIVE___________________________________________________________


  #MPA-8092 (#2.5)
  Scenario: Fail to save a specific entry upon the restricted permission due to applying the for "Member" API key
    Given the specified name "repository" was created and stored in DB without any stored entry
    And the user have been granted with valid "token" (API key) assigned by "Member" role
    When this user requested to save the entries in the relevant specified name "repository" with following details
      | instrumentId | name | DecimalPrecision | Rounding | key                  |
      | JPY          | Yen  | 4                | down     | KEY-FOR-CURRENCY-999 |
    Then this new entry shouldn't be created and the user should get the "errorMessage":"Permission denied"


  #MPA-8092 (#2.6)
  Scenario: Fail to save (edit) the specific entry upon the specified Repo doesn't exist applying the "Admin" either "Owner" API key
    Given there is no "repository" was created and stored in DB
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to save the entry (key and value) to non-existent "repository" specified name
    Then the user should get the "errorMessage": "repository: 'specified' name doesn't exist"


  #MPA-8092 (#2.7)
  Scenario: Fail to save (edit) the specific entry in the Repo upon upon the "token" is invalid (expired)
    Given a user have got an invalid "token" (API key)
    When this user requested to save some entry in the some specified name "repository"
    Then no repository shouldn't be created and the user should get the "errorMessage": "Token verification failed"


  #MPA-8092 (#2.8)
  Scenario: Fail to save (edit) the specific entry in the Repo upon the Owner deleted the Organization with related "Owner" API key
    Given an organization "organizationId" with specified "name" and "email" already created with related "Admin" API key which is stored there
    And the specified name "repository" was created and stored in DB without any stored entry
    And the related organization "Owner" has deleted this organization "organizationId"
    When the user requested to save some entry in the relevant specified name "repository" applying this deleted "Owner" API key
    Then no entry shouldn't be recorded to the relevant name "repository"
    And the user should get an error message: "Token verification failed"


  #MPA-8092 (#2.9)
  Scenario: Fail to save (edit) the specific entry in the Repo upon the Owner applied some of the manager's API key from another Organization
    Given the organizations "organizationId" with specified names "Org-1" and "Org-2" and emails already created
    And related "Owner" API key was stored in the organization with specified name "Org-1"
    And related "Admin" API key was stored in the organization with specified name "Org-2"
    And "repository" with specified name "Repo-1" was created and stored in DB by applying related "Owner" API key
    When the user requested to save some entry in the "repository" name "Repo-1" applying the "Admin" API key from organization with name "Org-2"
    Then no entry shouldn't be recorded to the relevant "repository" name "Repo-1"
    And the user should get the "errorMessage":"Permission denied"


  #MPA-8092 (#2.9) - logic will be implemented by Architect as the nature of the API key (token) is some expiration interim
  Scenario: Fail to save (edit) the specific entry in the Repo upon the Owner "token" (API key) was deleted from the Organization
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" API key which is stored there
    And the specified name "repository" was created and stored in DB without any stored entry
    And the related organization "Owner" has deleted the relevant "Owner" API key from the organization "organizationId"
    When the user requested to save some entry in the relevant specified name "repository" applying this deleted "Owner" API key
    Then no repository shouldn't be created and the user should get an error message: "Token verification failed"





  #GET (FETCH) THE SINGLE ENTRY

  #__________________________________________________POSITIVE___________________________________________________________


  #MPA-8092 (#3)
  Scenario: Successful get of a specific entry from the related Repo applying the all related API keys: "Owner", "Admin", "Member"
    Given the specified name "repository" was created and stored in DB with following entries
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
      | JPY          | Yen    | 2                | down     | KEY-FOR-CURRENCY-999       |
    And the user have been granted with valid "tokens" (API keys) assigned by "Owner", "Admin" and "Member" roles
    When this user requested to get (fetch) the existent entry set by "KEY-FOR-PRECIOUS-METAL-123" key in the relevant "repository" applying each of the API keys
    Then for each request user should get only one specified key with related entry
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |


  #MPA-8092 (#3.1)
  Scenario: Successful get the list of the stored entries from the Repo
    Given the users have got a valid "tokens" (API key) with "Owner" and "Admin" and "member" assigned roles
    And the relevant specified name "repository" with relevant (at least one) entries (keys and values) were created and stored in DB
    When each of these users requested to get the list of the existent entries (keys and values) which already exists in the relevant "repository" specified name
    Then each of these users should receive successful response with list of existent entries (keys and values)


  #MPA-8092 (#3.2)
  Scenario: Successfully get nothing upon stored entry was deleted from the Repo
    Given a user have got a valid "token" (API key) with assigned "Owner" role
    And the relevant specified name "repository" with some entry was created and stored in DB
    And this user deleted the "specific" entry (key and value) in the relevant "repository" specified name
    When this user requested to get the recently deleted entry (key and value) from the relevant "repository" specified name
    Then this user should receive successful response with empty object


  #MPA-8092 (#3.3)
  Scenario: Fail to get a non-existent entry from the Repo
    Given the users have got a valid "token" (API key) with "member" assigned role
    And there is no "repository" created and stored in DB
    When this user requested to get the non-existent entry (key) from non-existent "repository" specified name
    Then this user shouldn't receive any of the stored entries and get the error message: "repository: "specified" repository doesn't exist"


  #MPA-8092 (#2.9)
  Scenario: Fail to save (edit) the specific entry in the Repo upon the Owner deleted the Organization with related "Owner" API key
    Given the organizations "organizationId" with specified names "Org-1" and "Org-2" and emails already created
    And related "Owner" API key was stored in the organization with specified name "Org-1"
    And related "Admin" API key was stored in the organization with specified name "Org-2"
    And "repository" with specified name "Repo-1" was created and stored in DB by applying related "Owner" API key
    When the user requested to save some entry in the "repository" name "Repo-1" applying the "Admin" API key from organization with name "Org-2"
    Then no entry shouldn't be recorded to the relevant "repository" name "Repo-1"
    And the user should get the "errorMessage":"Permission denied"

  #MPA-8092 (#3.4)
  Scenario: Fail to save (edit) the value for specific entry upon the valid API key with "Owner" role was deleted with relevant organization

  #MPA-8092 (#3.5)
  Scenario: Fail to save (edit) the value for specific entry upon the issuer of valid API key with "Admin" role was downgraded to "Member"




  #DELETE THE SPECIFIC ENTRY

  #MPA-8092 (#4)
  Scenario: Successful delete (remove) of a specific entry from the Repo upon Admin either Owner permission was granted
    Given the users have got a valid "tokens" (API key) with "Owner" and "Admin" assigned roles
    And the relevant specified name "repository" with relevant entries (keys and values) were created and stored in DB
    When each of these users requested to delete some of the existent entries (key and value) which is stored in the relevant "repository" specified name
    Then each of these users should receive successful response with empty object


  #MPA-8092 (#4.1)
  Scenario: Fail to delete (remove) a specific entry from the Repo upon the restricted permission as Member
    Given the user have got a valid "token" (API key) with "member" assigned role
    And the relevant specified name "repository" with relevant entry (key and value) were created and stored in DB
    When this user requested to delete the existent entry (key and value) which is stored in the relevant "repository" specified name
    Then this entry shouldn't be removed from the "repository" and this user should receive an error message: "role: permission denied"


  #MPA-8092 (#4.2)
  Scenario: Fail to delete (remove) a non-existent entry from the Repo
    Given the user have got a valid "token" (API key) with "Admin" assigned role
    And the relevant specified name "repository" with relevant entry (key and value) were created and stored in DB
    When this user requested to delete the non-existent entry (key and value)
    Then any of the existent entry shouldn't be removed from the "repository" and this user should receive an error message: "role: "userId" not in role Owner or Admin"


  #___________________________________________________INSTANCE__________________________________________________________










  #GET

  #MPA-8092 (#6)
  Scenario: Successful get of the existent instrument instance entry
    Given the user have been granted with one of the valid "tokens" (API key) assigned by "Owner" either "Admin" either "member" role
    And the relevant specified name "repository" was created and stored in DB
    And specific "instrumentInstance" entry already updated and stored in this "repository"
      | instrumentId | name     | DecimalPrecision | Rounding | key                        |
      | XPT          | Platinum | 5                | up       | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to get this "instrumentInstance" entry from the relevant specified name "repository"
      | key                        |
      | KEY-FOR-PRECIOUS-METAL-123 |
    Then this user should receive successful response with relevant entry which is stored in the relevant specified name "repository" of the DB


  #MPA-8092 (#6.1)
  Scenario: Fail to get the non-existent instrument instance entry
    Given the user have been granted with one of the valid "tokens" (API key) assigned by "Owner" either "Admin" either "member" role
    And the relevant specified name "repository" was created and stored in DB
    And only single specific "instrumentInstance" entry stored in this "repository"
      | instrumentId | name     | DecimalPrecision | Rounding | key                        |
      | XPT          | Platinum | 5                | up       | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to get non-existent "instrumentInstance" entry from the relevant specified name "repository"
      | key          |
      | NON-EXISTENT |
    Then this user shouldn't receive any of the stored entries and get the error message: "key: "specified" name doesn't exist"


  #MPA-8092 (#6.2)
  Scenario: Successful get of the all existent entries list
    Given the user have been granted with one of the valid "tokens" (API key) assigned by "Owner" either "Admin" either "member" role
    And the relevant specified name "repository" was created and stored in DB
    And specific entries already stored in this "repository"
      | instrumentId | name      | DecimalPrecision | Rounding | key                        |
      | XPT          | Platinum  | 5                | up       | KEY-FOR-PRECIOUS-METAL-123 |
      | USD          | USDollar  | 4                | down     | KEY-FOR-CURRENCY-456       |
      | XPD          | Palladium | 4                | down     | KEY-FOR-PRECIOUS-METAL-789 |
    When this user requested to get the list of all the stored entries from the relevant specified name "repository"
    Then this user should receive successful response with the list of all the entries which are stored in the relevant specified name "repository" of the DB



  #MPA-8092 (#7)
  Scenario: Successful delete of the specific instrument instance entry
    Given the user have been granted with valid "token" (API key) assigned by "Owner" either "Admin" role
    And the relevant specified name "repository" was created and stored in DB
    And specific "instrumentInstance" entry stored in the relevant "repository" specified name
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to delete this specific "instrumentInstance" entry from the relevant specified name "repository"
    Then this user should receive successful response with empty object


  #MPA-8092 (#7.1)
  Scenario: Fail to delete the specific instrument instance entry
    Given the user have got a valid "token" (API key) with "member" assigned role
    And the relevant specified name "repository" was created and stored in DB
    And specific "instrumentInstance" entry stored in the relevant "repository" specified name
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to delete this specific "instrumentInstance" entry from the relevant specified name "repository"
    Then this entry shouldn't be removed from the "repository" and this user should receive an error message: "role: "userId" not in role Owner or Admin"


  #MPA-8092 (#7.2)
  Scenario: Fail to delete a non-existent instrument instance entry
    Given the user have been granted with valid "token" (API key) assigned by "Owner" either "Admin" role
    And the relevant specified name "repository" was created and stored in DB
    And only single specific "instrumentInstance" entry stored in this "repository"
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to delete non-existent "instrumentInstance" entry from the relevant specified name "repository"
      | key          |
      | NON-EXISTENT |
    Then any of the existent entry shouldn't be removed from the "repository" and this user should get the error message: "key: "specified" name doesn't exist"
