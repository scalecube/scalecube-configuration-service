@Configuration-service-production-ready

Feature: Integration tests for configuration service - FETCH (Single entry).

  Configuration service enable specified users which are granted with manager's access (related API keys: Owner&Admin) to create and manage
  the separate repositories purposed for entries collection (record) and storage. Potential customers are granted by relevant permission level (API key: Member)
  just to be able to observe the stored data in the relevant repository.

  Only the managers with "Owner" and "Admin" API keys should be able to delete the specific entries in the related Repository.


  #DELETE THE SPECIFIC ENTRY

  #__________________________________________________POSITIVE___________________________________________________________

  #MPA-8092 (#32)
  Scenario: Successful delete of the specific entry from the related Repository applying managers' API keys: "Owner" and "Admin"
    Given the specified name "repository" was created with following entries
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
      | JPY          | Yen    | 2                | down     | KEY-FOR-CURRENCY-999       |
    And the user have been granted with valid "tokens" (API keys) assigned by "Owner" and "Admin" roles
    When this user requested to delete the entry set by "KEY-FOR-PRECIOUS-METAL-123" key from the relevant "repository" applying the "Owner" API key
    And this user requested to delete the entry set by "KEY-FOR-CURRENCY-999" key from the relevant "repository" applying the "Admin" API key
    Then for each request user should get the successful response with the "empty" object


  #MPA-8092 (#32.1)
  Scenario: Successful delete one of the identical keys (entries) from the related Repository applying some of the managers' API keys
    Given the repository with "specified" name "Repo-1" created applying "Owner" API key with following entry
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And the repository with "specified" name "Repo-2" created applying "Owner" API key with following entry
      | instrumentId | name     | DecimalPrecision | Rounding | key                        |
      | XPT          | Platinum | 2                | up       | KEY-FOR-PRECIOUS-METAL-123 |
    When this user requested to delete existent entry set by "KEY-FOR-PRECIOUS-METAL-123" key from the repository "Repo-1" name
    Then user should get the successful response with the "empty" object
    But the identical entry with related key shouldn't be deleted from the the repository "Repo-2" name
      | instrumentId | name     | DecimalPrecision | Rounding | key                        |
      | XPT          | Platinum | 2                | up       | KEY-FOR-PRECIOUS-METAL-123 |


  #MPA-8092 (#33)
  Scenario: Successfully get nothing upon stored entry was deleted from the Repository (check the deletion)
    Given the specified name "repository" was created with following entries
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    And this user requested to delete the entry set by "key" from the relevant "repository"
    When this user requested to get the recently deleted entry "key" from the relevant "repository" specified name
    Then the user should get the "errorMessage":"key:'Name' not found"


  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-8092 (#34)
  Scenario: Fail to delete a specific entry upon the restricted permission due to applying the "Member" API key
    Given the specified name "repository" was created with following entries
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And the user have been granted with valid "token" (API key) assigned by "Member" role
    When this user requested to delete the entry set by "key" in the relevant specified name "repository"
    Then existing entry shouldn't be deleted
    And the user should get the "errorMessage":"Permission denied"


  #MPA-8092 (#35)
  Scenario: Fail to delete a non-existent entry from the related Repository applying the "Admin" API key
    Given the specified name "repository" was created with following entries
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to delete "non-existent" key from the relevant specified name "repository"
    Then the user should get the "errorMessage":"key:'Name' not found"


  #MPA-8092 (#36)
  Scenario: Fail to delete specific entry from the Repository upon the "token" is invalid (expired)
    Given a user have got an invalid "token" (API key)
    When this user requested to delete specific entry from some "repository" name
    Then the user should get the "errorMessage": "Token verification failed"


  #MPA-8092 (#37)
  Scenario: Fail to delete specific entry from the Repository upon the Owner deleted the Organization with related "Owner" API key
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" API key which is stored there
    And the specified name "repository" was created without any stored entry applying the related "Owner" API key
    And the related organization "Owner" has deleted this organization "organizationId"
    When the user requested to delete specific entry from the relevant specified name "repository" applying the deleted "Owner" API key
    Then the user should get "errorMessage": "Token verification failed"


  #MPA-8092 (#38)
  Scenario: Fail to delete specific entry from the Repository upon the Owner applied some of the API keys from another Organization
    Given the organizations "organizationId" with specified names "Org-1" and "Org-2" and emails already created
    And related "Owner" API key was stored in the organization with specified name "Org-1"
    And related "Admin" API key was stored in the organization with specified name "Org-2"
    And "repository" with specified name "Repo-1" was created by applying related "Owner" API key
    When the user requested to delete specific entry from the "repository" name "Repo-1" applying the "Admin" API key from organization with name "Org-2"
    Then the user should get the "errorMessage":"repository:'Name' not found"


  #MPA-8092 (#39) - logic will be implemented by Architect as the nature of the API key (token) is some expiration interim
  Scenario: Fail to delete specific entry from the Repository upon the Owner "token" (API key) was deleted from the Organization
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" API key which is stored there
    And the specified name "repository" was created without any stored entry by applying related "Owner" API key
    And the related organization "Owner" has deleted the relevant "Owner" API key from the organization "organizationId"
    When the user requested to delete some entry from the relevant specified name "repository" applying this deleted "Owner" API key
    Then the user should get "errorMessage": "Token verification failed"