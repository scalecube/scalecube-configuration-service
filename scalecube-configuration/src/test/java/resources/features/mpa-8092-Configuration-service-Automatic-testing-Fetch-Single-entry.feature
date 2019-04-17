@Configuration-service-production-ready

Feature: Integration tests for configuration service - FETCH (Single entry).

  Configuration service enable specified users which are granted with manager's access (related API keys: Owner&Admin) to create and manage
  the separate repositories purposed for entries collection (record) and storage. Potential customers are granted by relevant permission level (API key: Member)
  just to be able to observe the stored data in the relevant repository.

  Mangers (with Owner/admin  API keys) and potential customers (with Member API keys) of the Configuration service should be able
  to get/read (fetch) only specific entry stored in the related Repository

  #GET (FETCH) THE SINGLE ENTRY

  #__________________________________________________POSITIVE___________________________________________________________


  #MPA-8092 (#18)
  Scenario: Successful get of a specific entry from the related Repository applying the all related API keys: "Owner", "Admin", "Member"
    Given the specified name "repository" was created with following entries
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
      | JPY          | Yen    | 2                | down     | KEY-FOR-CURRENCY-999       |
    And the user have been granted with valid "tokens" (API keys) assigned by "Owner", "Admin" and "Member" roles
    When this user requested to get (fetch) the existent entry set by "KEY-FOR-PRECIOUS-METAL-123" key in the relevant "repository" applying each of the API keys
    Then for each request user should get only one specified key with related entry
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |


  #MPA-8092 (#19)
  Scenario: Successful get one of the identical entries from the related Repository applying some of the related API keys: "Owner", "Admin", "Member"
    Given the repository with "specified" name "Repo-1" already created with following entry
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And the repository with "specified" name "Repo-2" already created with following entry
      | instrumentId | name | DecimalPrecision | Rounding | key                        |
      | JPY          | Yen  | 2                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And the user have been granted with valid "token" (API key) assigned by "Member" role
    When this user requested to get (fetch) the existent entry set by "KEY-FOR-PRECIOUS-METAL-123" key from the repository "Repo-1" name
    Then the user should get only one specified key with related entry
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |


  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-8092 (#20)
  Scenario: Fail to get the non-existent entry from the existent Repository applying some of the accessible API keys: "Owner", "Admin", "Member"
    Given the specified name "repository" was created with following entries
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
      | JPY          | Yen    | 2                | down     | KEY-FOR-CURRENCY-999       |
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to get (fetch) the entry set by "non-existent" key from the relevant "repository"
    Then the user shouldn't receive any of the stored entries
    And the user should get the "errorMessage":"key:'Name' not found"


  #MPA-8211 (#20.1)
  Scenario: Fail to get the entry upon the related key is empty or undefined (null)
    Given the specified name "repository" was created without any entries
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to get specific entry in the relevant "repository" with empty and undefined keys
      | key  | repository |
      |      | repository |
      | null | repository |
    Then for each request the user should get an error message: "Please specify a key name"


  #MPA-8211 (#20.2)
  Scenario: Fail to get the entry upon the repository name is empty or undefined (null)
    Given no "repository" was created
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to get some entries in the some "repository" with empty and undefined name
      | repository |
      |            |
      | null       |
    Then for each request the user should get an error message: "Please specify a Repository name"


  #MPA-8211 (#20.3)
  Scenario: Fail to get the entry upon the related "key" is missed
    Given the specified name "repository" was created without any entries
    And the user have been granted with valid "token" (API key) assigned by "Member" role
    When this user requested to get an entry from the relevant "repository" without related "key" at all
      | value | repository |
      | XAG   | repository |
    Then the user should get an error message: "Please specify a key name"


  #MPA-8211 (#20.4)
  Scenario: Fail to get the entry upon the "repository" key is missed
    Given no "repository" was created
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to get specific entry without related "repository" key at all
      | value | key   |
      | XAG   | metal |
    Then the user should get an error message: "Please specify a Repository name"


  #MPA-8092 (#21)
  Scenario: Fail to get any entry from the non-existent Repository applying some of the accessible API keys: "Owner", "Admin", "Member"
    Given no "repository" was created
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to get (fetch) the entry set by "non-existent" Repository name
    Then the user should get the "errorMessage":"repository:'Name' not found"


  #MPA-8092 (#22)
  Scenario: Fail to get the specific entry from the Repository upon the "token" is invalid (expired)
    Given a user have got an invalid "token" (API key)
    When this user requested to get (fetch) the entry from some "repository" name
    Then the user should get the "errorMessage": "Token verification failed"


  #MPA-8092 (#23)
  Scenario: Fail to get the specific entry from the Repository upon the Owner deleted the Organization with related "Admin" API key
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" and "Admin" API keys which are stored there
    And the specified name "repository" was created without any stored entry applying the related "Owner" API key
    And the related organization "Owner" has deleted this organization "organizationId"
    When the user requested to get some entry from the relevant specified name "repository" applying the deleted "Admin" API key
    Then the user should get "errorMessage": "Token verification failed"


  #MPA-8092 (#24)
  Scenario: Fail to get the specific entry from the Repository upon the Owner applied some of the API keys from another Organization
    Given the organizations "organizationId" with specified names "Org-1" and "Org-2" and emails already created
    And related "Owner" API key was stored in the organization with specified name "Org-1"
    And related "Member" API key was stored in the organization with specified name "Org-2"
    And "repository" with specified name "Repo-1" was created by applying related "Owner" API key
    When the user requested to get some entry from the "repository" name "Repo-1" applying the "Member" API key from organization with name "Org-2"
    Then the user should get the "errorMessage":"repository:'Name' not found"


  #MPA-8092 (#25) - logic will be implemented by Architect as the nature of the API key (token) is some expiration interim (MPA-8260/8057)
  Scenario: Fail to get the specific entry from the Repository upon the Admin "token" (API key) was deleted from the Organization
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" and "Admin" API keys which are stored there
    And the specified name "repository" was created without any stored entry by applying related "Owner" API key
    And the related organization "Owner" has deleted the relevant "Admin" API key from the organization "organizationId"
    When the user requested to get some entry from the relevant specified name "repository" applying this deleted "Admin" API key
    Then the user should get "errorMessage": "Token verification failed"