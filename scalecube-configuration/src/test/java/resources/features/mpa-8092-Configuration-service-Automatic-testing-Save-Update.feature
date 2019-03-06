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