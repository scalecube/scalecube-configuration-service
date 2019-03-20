@Configuration-service-production-ready

Feature: Integration tests for configuration service - SAVE/UPDATE.

  Configuration service enable specified users which are granted with manager's access (related API keys: Owner&Admin) to create and manage
  the separate repositories purposed for entries collection (record) and storage. Potential customers are granted by relevant permission level (API key: Member)
  just to be able to observe the stored data in the relevant repository.

  Only the managers with "Owner" and "Admin" API keys should be able to save and update the specific entries in the related Repository.



  #SAVE (UPDATE/EDIT) THE ENTRY IN THE REPO

  #__________________________________________________POSITIVE___________________________________________________________


  #MPA-8092 (#7)
  Scenario: Successful save of specific entry (instrument) applying the "Owner" API key
    Given the specified name "repository" was created
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to save the specified entry in the relevant "repository" specified name with following details
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    Then all new entries should be stored in the relevant "repository"
    And the user should get the successful response with the "empty" object


  #MPA-8092 (#8)
  Scenario: Successful save the identical entries for different Repositories applying the "Admin" API key
    Given  the "repositories" with "specified" names "Repo-1" and "Repo-2" already created  without any entry
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to save the following specified entry in repository with name "Repo-1"
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And this user requested to save the following specified entry in repository with name "Repo-2"
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    Then both identical entries should be stored in the relevant repositories "Repo-1" and "Repo-2"
    And for the each request user should get the successful response with the "empty" object


  #MPA-8092 (#9)
  Scenario: Successful update (save/override - edit) one of the identical entries in the different Repositories applying the "Owner" API key
    Given the repository with "specified" name "Repo-1" already created with following entry
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And the repository with "specified" name "Repo-2" already created with following entry
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to update (save) one from identical entries in the repository with name "Repo-1" setting following details
      | instrumentId | name     | DecimalPrecision | Rounding | key                        |
      | XPT          | Platinum | 2                | up       | KEY-FOR-PRECIOUS-METAL-123 |
    Then new entry should be set to the given "key" and stored in the relevant repository with name "Repo-1"
    And the user should get the successful response with the "empty" object
    But existent entry in the repository name with name "Repo-2" shouldn't be updated


  #MPA-8092 (#10)
  Scenario: No change for the successful update (save/override - edit) of the existing entry with the same values applying the "Admin" API key
    Given the specified name "repository" was created  with following entry
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to update (save) the existent entry in the relevant "repository" with the same values
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    Then existent entry shouldn't be duplicated thus new values should be set to the given key and stored in the relevant "repository"
    And the user should get the successful response with the "empty" object


  #MPA-8211 (#10.1)
  Scenario: Successful update (save/override - edit) of the existing entry with empty or undefined (null) value which is set by specified key
    Given the specified name "repository" was created with following entries
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
      | JPY          | Yen    | 4                | down     | KEY-FOR-CURRENCY-999       |
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to update (save) the existent entries in the relevant "repository" with empty and undefined values
      | value | key                        |
      |       | KEY-FOR-PRECIOUS-METAL-123 |
      | null  | KEY-FOR-CURRENCY-999       |
    Then the related entries which are set to the related key should be set to the given key
    And for each request user should get the successful response with the "empty" object
    And this user requested to get all recently updated entries to verify that keys and values weren't deleted
    Then the user should get all the entries stored in the related "repository"
      | value | key                        |
      |       | KEY-FOR-PRECIOUS-METAL-123 |
      | null  | KEY-FOR-CURRENCY-999       |


  #MPA-8092 (#11)
  Scenario: Successful save the specific entries applying the "Owner" API key for:
  - values that reach at least a 1000 chars (no quantity validation for input)
  - values which chars are symbols and spaces (no chars validation for input)
    Given the specified name "repository" was created  without any stored entry
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to save the entries in the relevant specified name "repository" with following details
      | instrumentId                                                                         | name        | DecimalPrecision | Rounding    | key                        |
      | XPTTTTTTTTTTTTTTTTTTTTTXPTTTTTTTTTTTTTTTTTTTTTTTXPTTTTTTTTTTTTTTTTTTTTTTTXPTTTTT.... | Platinum    | 5                | up          | KEY-FOR-PRECIOUS-METAL-123 |
      | #!=`   ~/.*                                                                          | #!=   `~/.* | #!=`   ~/.*      | #!=`~   /.* | KEY-FOR-PRECIOUS-METAL-124 |
    Then new entries should be stored in the relevant "repository"
    And for each request the user should get the successful response with the "empty" object


  #__________________________________________________NEGATIVE___________________________________________________________


  #MPA-8092 (#12)
  Scenario: Fail to save a specific entry upon the restricted permission due to applying the "Member" API key
    Given the specified name "repository" was created  without any stored entry
    And the user have been granted with valid "token" (API key) assigned by "Member" role
    When this user requested to save the entries in the relevant specified name "repository" with following details
      | instrumentId | name | DecimalPrecision | Rounding | key                  |
      | JPY          | Yen  | 4                | down     | KEY-FOR-CURRENCY-999 |
    Then this new entry shouldn't be created and the user should get the "errorMessage":"Permission denied"


  #MPA-8092 (#13)
  Scenario: Fail to save (edit) the specific entry applying the "Admin" either "Owner" API key upon the specified Repository doesn't exist
    Given there is no "repository" was created
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to save the entry (key and value) to non-existent "repository" specified name
    Then the user should get the "errorMessage":"repository: 'Name' not found"


  #MPA-8092 (#14)
  Scenario: Fail to save (edit) the specific entry in the Repository upon the "token" is invalid (expired)
    Given a user have got an invalid "token" (API key)
    When this user requested to save some entry in the some specified name "repository"
    Then no entry shouldn't be created and the user should get the "errorMessage": "Token verification failed"


  #MPA-8092 (#15)
  Scenario: Fail to save (edit) the specific entry in the Repository upon the Owner deleted the Organization with related "Owner" API key
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" API key which is stored there
    And the specified name "repository" was created  without any stored entry applying the related "Owner" API key
    And the related organization "Owner" has deleted this organization "organizationId"
    When the user requested to save some entry in the relevant specified name "repository" applying this deleted "Owner" API key
    Then no entry shouldn't be recorded to the relevant name "repository"
    And the user should get the "errorMessage": "Token verification failed"


  #MPA-8092 (#16)
  Scenario: Fail to save (edit) the specific entry in the Repository upon the Owner applied some of the manager's API key from another Organization
    Given the organizations "organizationId" with specified names "Org-1" and "Org-2" and emails already created
    And related "Owner" API key was stored in the organization with specified name "Org-1"
    And related "Admin" API key was stored in the organization with specified name "Org-2"
    And "repository" with specified name "Repo-1" was created  by applying related "Owner" API key
    When the user requested to save some entry in the "repository" name "Repo-1" applying the "Admin" API key from organization with name "Org-2"
    Then no entry shouldn't be recorded to the relevant "repository" name "Repo-1"
    And the user should get the "errorMessage":"repository:'Name' not found"


  #MPA-8092 (#17) - logic will be implemented by Architect as the nature of the API key (token) is some expiration interim
  Scenario: Fail to save (edit) the specific entry in the Repository upon the Owner "token" (API key) was deleted from the Organization
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" API key which is stored there
    And the specified name "repository" was created  without any stored entry by applying related "Owner" API key
    And the related organization "Owner" has deleted the relevant "Owner" API key from the organization "organizationId"
    When the user requested to save some entry in the relevant specified name "repository" applying this deleted "Owner" API key
    Then no entry should be stored in the related repository
    And the user should get an error message: "Token verification failed"


  #MPA-8211 (#17.1)
  Scenario: Fail to save the entry upon the related key is empty or undefined (null)
    Given the specified name "repository" was created without any entries
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to save some entries in the relevant "repository" with empty and undefined keys
      | value | key  |
      | XAG   |      |
      | JPY   | null |
    Then no entry should be stored in the related repository
    And the user should get an error message: "Please specify a key name"