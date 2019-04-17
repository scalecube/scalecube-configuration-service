@Configuration-service-production-ready

Feature: Integration tests for configuration service - ENTRIES (All entries).

  Configuration service enable specified users which are granted with manager's access (related API keys: Owner&Admin) to create and manage
  the separate repositories purposed for entries collection (record) and storage. Potential customers are granted by relevant permission level (API key: Member)
  just to be able to observe the stored data in the relevant repository.

  Mangers (with Owner/admin  API keys) and potential customers (with Member API keys) of the Configuration service should be able
  to get/read all the entries stored in the related Repository


  #GET ALL ENTRIES

  #__________________________________________________POSITIVE___________________________________________________________

  #MPA-8092 (#26)
  Scenario: Successful get of the all existent entries list from the related Repository applying the all related API keys: "Owner", "Admin", "Member"
    Given the specified name "repository" was created with following entries
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
      | JPY          | Yen    | 2                | down     | KEY-FOR-CURRENCY-999       |
    And the user have been granted with valid "tokens" (API keys) assigned by "Owner", "Admin" and "Member" roles
    When this user requested to get all existent entries from the relevant "repository" applying each of the API keys
    Then for each request user should get all the entries stored in the related "repository"
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
      | JPY          | Yen    | 2                | down     | KEY-FOR-CURRENCY-999       |


  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-8092 (#27)
  Scenario: Fail to get any entry from the non-existent Repository applying some of the accessible API keys: "Owner", "Admin", "Member"
    Given no "repository" was created
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to get all existent entries set by "non-existent" Repository name
    Then the user should get the "errorMessage":"repository:'Name' not found"


  #MPA-8211 (#27.1)
  Scenario: Fail to get any entry upon the repository name is empty or undefined (null)
    Given no "repository" was created
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to get all entries in the some "repository" with empty and undefined name
      | repository |
      |            |
      | null       |
    Then the user should get an error message: "Please specify a Repository name"


  #MPA-8211 (#27.2)
  Scenario: Fail to get any entry upon the "repository" key is missed
    Given no "repository" was created
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to get entries without related "repository" key at all
    Then the user should get an error message: "Please specify a Repository name"


  #MPA-8092 (#28)
  Scenario: Fail to get any entry from the Repository upon the "token" is invalid (expired)
    Given a user have got an invalid "token" (API key)
    When this user requested to get all existent entries from some "repository" name
    Then the user should get the "errorMessage": "Token verification failed"


  #MPA-8092 (#29)
  Scenario: Fail to get any entry from the Repository upon the Owner deleted the Organization with related "Member" API key
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" and "Member" API keys which are stored there
    And the specified name "repository" was created without any stored entry applying the related "Owner" API key
    And the related organization "Owner" has deleted this organization "organizationId"
    When the user requested to get all existent entries from the relevant specified name "repository" applying the deleted "Member" API key
    Then the user should get "errorMessage": "Token verification failed"


  #MPA-8092 (#30)
  Scenario: Fail to get any entry from the Repository upon the Owner applied some of the API keys from another Organization
    Given the organizations "organizationId" with specified names "Org-1" and "Org-2" and emails already created
    And related "Owner" API key was stored in the organization with specified name "Org-1"
    And related "Admin" API key was stored in the organization with specified name "Org-2"
    And "repository" with specified name "Repo-1" was created by applying related "Owner" API key
    When the user requested to get all entries from the "repository" name "Repo-1" applying the "Admin" API key from organization with name "Org-2"
    Then the user should get the "errorMessage":"Repository:'Name' not found"


  #MPA-8092 (#31) - logic will be implemented by Architect as the nature of the API key (token) is some expiration interim (MPA-8260/8057)
  Scenario: Fail to get any entry from the Repository upon the Member "token" (API key) was deleted from the Organization
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" and "Member" API keys which are stored there
    And the specified name "repository" was created without any stored entry by applying related "Owner" API key
    And the related organization "Owner" has deleted the relevant "Member" API key from the organization "organizationId"
    When the user requested to get some entry from the relevant specified name "repository" applying this deleted "Member" API key
    Then the user should get "errorMessage": "Token verification failed"