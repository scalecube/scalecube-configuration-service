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
      | apiKey      | repository | key                        | value | instrumentId | name   | DecimalPrecision | Rounding |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |       | XAG          | Silver | 4                | down     |
    Then new entry should be stored in the relevant "repository"
    And the user should get the successful response with fixed version for each new key-entry
      | version |
      | 1       |

  #10
  Scenario: Successful creation of identical entries for different Repositories applying the "Owner" and Admin" API keys
    When the user requested to createEntry the following specified entries in the separate repositories
      | apiKey      | repository | key             | value | instrumentId | name   | DecimalPrecision | Rounding |
      | Owner-Org-1 | Repo-1     | PRECIOUS-METALS |       | XAG          | Silver | 4                | down     |
      | Admin-Org-2 | Repo-2     | PRECIOUS-METALS |       | AG           | Silver | 4                | down     |
    Then new entries should be stored in the relevant repositories "Repo-1" and "Repo-2"
    And for each request user should get the successful response with fixed version for each new key-entry
      | version |
      | 1       |


  #11
  Scenario: Successful entry creation (no validation for input) enabling to save following values:
  - values that reach at least a 1000 chars
  - values which chars are symbols and spaces
  - JsonArray
  - null / empty string
    When the user requested to createEntry in the relevant repository with following details
      | apiKey      | repository | key         | value | instrumentId                                   | name                                  | DecimalPrecision       |
      | Owner-Org-1 | Repo-1     | someChars   |       | XPTTTTTTTTTTTTTTTTTXPTTTTTTTTT....>=1000 chars | Silvergskjfhsksuhff......>=1000 chars | 4444444...>=1000 chars |
      | Admin-Org-1 | Repo-1     | someSymbols |       | #!=`   ~/.*                                    | #!=   `~/.*                           | #!=`   ~/.*            |
      | Owner-Org-1 | Repo-1     | Int         | 1     | ---                                            | ---                                   | ---                    |
      | Admin-Org-1 | Repo-1     | blankJson   | {}    | ---                                            | ---                                   | ---                    |
      | Owner-Org-1 | Repo-1     | JsonArray   | []    | ---                                            | ---                                   | ---                    |
      | Admin-Org-1 | Repo-1     | undefined   | null  | ---                                            | ---                                   | ---                    |
      | Owner-Org-1 | Repo-1     | empty       |       | ---                                            | ---                                   | ---                    |
    And the user requested to createEntry in the relevant repository without "value" key at all
      | apiKey      | repository | key   |
      | Admin-Org-1 | Repo-1     | empty |
    Then new entries should be stored in the relevant repository "Repo-1"
    And for each request user should get the successful response with fixed version for each new key-entry
      | version |
      | 1       |


  #__________________________________________________NEGATIVE___________________________________________________________


  #12
  Scenario: Fail to createEntry due to restricted permission upon the "Member" API key was applied
    When the user requested to createEntry in the relevant repository with following details
      | apiKey       | repository | key                  | value | instrumentId | name | DecimalPrecision | Rounding |
      | Member-Org-1 | Repo-1     | KEY-FOR-CURRENCY-999 |       | JPY          | Yen  | 4                | down     |
    Then the user should get following error
      | errorCode | errorMessage      |
      | 500       | Permission denied |


  #13
  Scenario: Fail to createEntry due to Key name duplication
    Given following entry was successfully saved in the related repository
      | repository | key                        | value | instrumentId | name   | DecimalPrecision | Rounding |
      | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |       | XAG          | Silver | 4                | down     |
    When the user requested to createEntry in the relevant repository with the same Ket name
      | apiKey      | repository | key                        | value | instrumentId | name | DecimalPrecision | Rounding |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |       | XAU          | Gold | 4                | down     |
    Then the user should get following error
      | errorCode | errorMessage                                                        |
      | 500       | Repository 'Repo-1' key 'KEY-FOR-PRECIOUS-METAL-123' already exists |


  #14
  Scenario: Fail to createEntry due to specified Repository doesn't exist
    When the user requested to createEntry in the "non-existent" repository
      | apiKey      | repository   | key     | value |
      | Owner-Org-1 | non-existent | new-key |       |
    Then the user should get following error
      | errorCode | errorMessage                        |
      | 500       | Repository 'non-existent' not found |


  #15
  Scenario: Fail to createEntry upon the Owner deleted the "Organization"
    Given the related organization Owner has deleted organization name "Org-2"
    When the user requested to createEntry applying "Owner" apiKey from the deleted organization "Org-2"
      | apiKey      | repository | key     | value |
      | Owner-Org-2 | Repo-2     | new-key |       |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #16
  Scenario: Fail to createEntry upon the "Admin" apiKey was deleted from the Organization
    Given organization "Org-1" Owner has deleted the relevant "Admin" apiKey from it
    When the user requested to createEntry applying the deleted "Admin" apiKey from organization "Org-1"
      | apiKey      | repository | key     | value |
      | Admin-Org-1 | Repo-1     | new-key |       |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #17
  Scenario: Fail to createEntry due to invalid apiKey was applied
    When the user requested to createEntry applying the "invalid" apiKey
      | apiKey  | repository | key     | value |
      | invalid | Repo-3     | new-key |       |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #18
  Scenario: Fail to createEntry with empty or undefined Repository name
    When the user requested to createEntry without specifying repository name
      | apiKey      | repository | key     | value |
      | Owner-Org-1 |            | new-key |       |
      | Owner-Org-1 | null       | new-key |       |
    And the user requested to createEntry without "repository" key name at all
      | apiKey      |
      | Owner-Org-1 |
    Then for each request user should get following error
      | errorCode | errorMessage                |
      | 500       | Please specify 'repository' |


  #19
  Scenario: Fail to createEntry with empty or undefined apiKey
    When the user requested to createEntry without specifying the apiKey
      | apiKey | repository | key     | value |
      |        | Repo-3     | new-key |       |
      | null   | Repo-3     | new-key |       |
    And the user requested to createEntry without "apiKey" at all
      | repository | key     | value |
      | Repo-3     | new-key |       |
    Then for each request user should get following error
      | errorCode | errorMessage            |
      | 500       | Please specify 'apiKey' |


  #20
  Scenario: Fail to createEntry with empty or undefined Key field
    When the user requested to createEntry without specifying repository name
      | apiKey      | repository | key  | value |
      | Owner-Org-1 | Repo-3     |      |       |
      | Owner-Org-1 | Repo-3     | null |       |
    And the user requested to createEntry without "key" field at all
      | apiKey      | repository | value |
      | Owner-Org-1 | Repo-3     |       |
    Then for each request user should get following error
      | errorCode | errorMessage         |
      | 500       | Please specify 'key' |