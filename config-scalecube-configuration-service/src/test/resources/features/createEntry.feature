@Configuration-service-integration-tests

Feature: Integration tests for configuration service - createEntry.

  Only the managers with "Owner" and "Admin" API keys roles should be able to save the specific entries in the related Repository.


  Background: repositories are stored in the system and having apiKeys
    Given the "repositories" with specified names "Repo-1" and "Repo-2" already created without any entry
    And organizations with specified names "Org-1" and "Org-2" already created
    And related apiKeys with "Owner", "Admin" and "Member" roles are stored in Organization "Org-1"
    And related apiKeys with "Owner", "Admin" and "Member" roles are stored in Organization "Org-2"
    And repositories with "specified" names "Repo-1" and "Repo-2" already created  without any entry


  #CREATE ENTRY

  #__________________________________________________POSITIVE___________________________________________________________


  #9
  Scenario: Successful entry creation applying the "Owner" API key
    When the user requested to createEntry in the relevant repository with following details
      | apiKey      | repository | key                        | instrumentId | name   | DecimalPrecision | Rounding |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | XAG          | Silver | 4                | down     |
    Then new entry should be stored in the relevant "repository"
    And the user should get the successful response with fixed version for each new key-entry
      | version |
      | 1       |

  #10
  Scenario: Successful creation of identical entries for different Repositories applying the "Owner" and Admin" API keys
    When the user requested to createEntry the following specified entries in the separate repositories
      | apiKey      | repository | key             | instrumentId | name   | DecimalPrecision | Rounding |
      | Owner-Org-1 | Repo-1     | PRECIOUS-METALS | XAG          | Silver | 4                | down     |
      | Admin-Org-2 | Repo-2     | PRECIOUS-METALS | XAG          | Silver | 4                | down     |
    Then new entries should be stored in the relevant repositories "Repo-1" and "Repo-2"
    And for each request user should get the successful response with fixed version for each new key-entry
      | version |
      | 1       |


  #11
  Scenario: Successful entry creation (no quantity validation for input) enabling to save:
  - values that reach at least a 1000 chars
  - values which chars are symbols and spaces
    When this user requested to save the entries in the relevant specified name "repository" with following details
      | apiKey      | repository | key         | instrumentId                                   | name                                  | DecimalPrecision       |
      | Owner-Org-1 | Repo-1     | someChars   | XPTTTTTTTTTTTTTTTTTXPTTTTTTTTT....>=1000 chars | Silvergskjfhsksuhff......>=1000 chars | 4444444...>=1000 chars |
      | Admin-Org-1 | Repo-1     | someSymbols | #!=`   ~/.*                                    | #!=   `~/.*                           | #!=`   ~/.*            |
    Then new entries should be stored in the relevant repository "Repo-1"
    And for each request user should get the successful response with fixed version for each new key-entry
      | version |
      | 1       |


  #__________________________________________________NEGATIVE___________________________________________________________


  #12
  Scenario: Fail to createEntry due to restricted permission upon the "Member" API key was applied
    When the user requested to createEntry in the relevant repository with following details
      | apiKey       | repository | key                  | instrumentId | name | DecimalPrecision | Rounding |
      | Member-Org-1 | Repo-1     | KEY-FOR-CURRENCY-999 | JPY          | Yen  | 4                | down     |
    Then the user should get following error
      | errorCode | errorMessage      |
      | 500       | Permission denied |

  #13
  Scenario: Fail to createEntry due to Key name duplication
    Given following entry was successfully saved in the related repository
      | repository | key                        | instrumentId | name   | DecimalPrecision | Rounding |
      | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | XAG          | Silver | 4                | down     |
    When the user requested to createEntry in the relevant repository with the same Ket name
      | apiKey      | repository | key                        | instrumentId | name | DecimalPrecision | Rounding |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | XAU          | Gold | 4                | down     |
    Then the user should get following error
      | errorCode | errorMessage                                                        |
      | 500       | Repository 'Repo-1' key 'KEY-FOR-PRECIOUS-METAL-123' already exists |

  #16
  Scenario: Fail to createEntry due to specified Repository doesn't exist
    When this user requested to save the entry (key and value) to  specified name
    When the user requested to createEntry in the "non-existent" repository
      | apiKey      | repository   | key     | instrumentId | name | DecimalPrecision | Rounding |
      | Owner-Org-1 | non-existent | new-key | JPY          | Yen  | 4                | down     |
    Then the user should get following error
      | errorCode | errorMessage                        |
      | 500       | Repository 'non-existent' not found |




  #17
  Scenario: Fail to save (edit) the specific entry in the Repository upon the "token" is invalid (expired)
    Given a user have got an invalid "token" (API key)
    When this user requested to save some entry in the some specified name "repository"
    Then no entry shouldn't be created and the user should get the "errorMessage": "Token verification failed"


  #18
  Scenario: Fail to save (edit) the specific entry in the Repository upon the Owner deleted the Organization with related "Owner" API key
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" API key which is stored there
    And the specified name "repository" was created  without any stored entry applying the related "Owner" API key
    And the related organization "Owner" has deleted this organization "organizationId"
    When the user requested to save some entry in the relevant specified name "repository" applying this deleted "Owner" API key
    Then no entry shouldn't be recorded to the relevant name "repository"
    And the user should get the "errorMessage": "Token verification failed"


  #19
  Scenario: Fail to save (edit) the specific entry in the Repository upon the Owner applied some of the manager's API key from another Organization
    Given the organizations "organizationId" with specified names "Org-1" and "Org-2" and emails already created
    And related "Owner" API key was stored in the organization with specified name "Org-1"
    And related "Admin" API key was stored in the organization with specified name "Org-2"
    And "repository" with specified name "Repo-1" was created  by applying related "Owner" API key
    When the user requested to save some entry in the "repository" name "Repo-1" applying the "Admin" API key from organization with name "Org-2"
    Then no entry shouldn't be recorded to the relevant "repository" name "Repo-1"
    And the user should get the "errorMessage":"repository:'Name' not found"


  #20
  Scenario: Fail to save (edit) the specific entry in the Repository upon the Owner "token" (API key) was deleted from the Organization
    Given an organization "organizationId" with specified "name" and "email" already created with related "Owner" API key which is stored there
    And the specified name "repository" was created  without any stored entry by applying related "Owner" API key
    And the related organization "Owner" has deleted the relevant "Owner" API key from the organization "organizationId"
    When the user requested to save some entry in the relevant specified name "repository" applying this deleted "Owner" API key
    Then no entry should be stored in the related repository
    And the user should get an error message: "Token verification failed"


  #21
  Scenario: Fail to save/update the entry upon the related key is empty or undefined (null)
    Given the specified name "repository" was created without any entries
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to save some entries in the relevant "repository" with empty and undefined keys
      | value | key  | repository |
      | XAG   |      | repository |
      | JPY   | null | repository |
    Then no entry should be stored in the related repository
    And for each request the user should get an error message: "Please specify a key name"


  #22
  Scenario: Fail to save/update the entry upon the repository name is empty or undefined (null)
    Given no "repository" was created
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to save some entries in the some "repository" with empty and undefined name
      | value | key      | repository |
      | XAG   | metal    |            |
      | JPY   | currency | null       |
    Then no entry should be stored
    And for each request the user should get an error message: "Please specify a Repository name"


  #23
  Scenario: Fail to save/update the entry upon the related "key" name is missed
    Given the specified name "repository" was created without any entries
    And the user have been granted with valid "token" (API key) assigned by "Owner" role
    When this user requested to save an entry in the relevant "repository" without related "key" at all
      | value | repository |
      | XAG   | repository |
    Then no entry should be stored in the related repository
    And the user should get an error message: "Please specify a key name"


  #24
  Scenario: Fail to save/update the entry upon the "repository" key is missed
    Given no "repository" was created
    And the user have been granted with valid "token" (API key) assigned by "Admin" role
    When this user requested to save an entry without related "repository" key at all
      | value | key   |
      | XAG   | metal |
    Then no entry should be stored
    And the user should get an error message: "Please specify a Repository name"