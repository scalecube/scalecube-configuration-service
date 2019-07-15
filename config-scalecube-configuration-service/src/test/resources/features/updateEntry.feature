@Configuration-service-integration-tests

Feature: Integration tests for configuration service - updateEntry.

  Only the managers with "Owner" and "Admin" API keys roles should be able to update the specific entries in the related Repository.

  Background: repositories with entries are stored in the system and having apiKeys
    Given organizations with specified names "Org-1" and "Org-2" already created
    And related apiKeys with "Owner", "Admin" and "Member" roles are stored in Organization "Org-1"
      | apiKey       |
      | Owner-Org-1  |
      | Admin-Org-1  |
      | Member-Org-1 |
    And related apiKeys with "Owner" and "Admin" roles are stored in Organization "Org-2"
      | apiKey      |
      | Owner-Org-2 |
      | Admin-Org-2 |
    And repository with name "Repo-1" related to organization "Org-1" created with {"version":1} per each key
      | repository | key                        | value | instrumentId | name   | DecimalPrecision | Rounding |
      | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |       | XAG          | Silver | 4                | down     |
    And repository with name "Repo-2" related to organization "Org-2" created with {"version":1} per each key
      | repository | key                        | value | instrumentId | name   | DecimalPrecision | Rounding |
      | Repo-2     | KEY-FOR-PRECIOUS-METAL-123 |       | XAG          | Silver | 4                | down     |


  #UPDATE ENTRY

  #__________________________________________________POSITIVE___________________________________________________________

  #21
  Scenario: Successful updateEntry by one of the identical keys in the different Repositories applying the "Owner" API key
    When the user requested to updateEntry in the repository name "Repo-1" with following details
      | apiKey      | repository | key                        | value | instrumentId | name   | DecimalPrecision | Rounding |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |       | XAG          | Silver | 4                | down     |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |       | JPY          | Yen    | 2                | up       |
    Then new entries related for the existing "key" should be stored in the "Repo-1" which belongs to organization "Org-1"
    And for each request user should get the successful response with new version for each entry
      | version |
      | 2       |
      | 3       |
    But any entry in the repository "Repo-1" shouldn't be overwritten for related key "KEY-FOR-PRECIOUS-METAL-123"
      | version | value | instrumentId | name   | DecimalPrecision | Rounding |
      | 1       |       | XAG          | Silver | 4                | down     |
      | 2       |       | XAG          | Silver | 4                | down     |
      | 3       |       | JPY          | Yen    | 2                | up       |
    And existent entry in the repository name "Repo-2" related to organization "Org-2" shouldn't be updated
      | key                        | value | instrumentId | name   | DecimalPrecision | Rounding |
      | KEY-FOR-PRECIOUS-METAL-123 |       | XAG          | Silver | 4                | down     |


  #22
  Scenario: Successful updateEntry (no validation for input) enabling to save following values:
  - values that reach at least a 1000 chars
  - values which chars are symbols and spaces
  - JsonArray
  - null / empty string
    When the user requested to updateEntry in the relevant repository with following details
      | apiKey      | repository | key                          | value               | instrumentId                                   | name                                  | DecimalPrecision       |
      | Owner-Org-2 | Repo-2     | some Chars                   |                     | XPTTTTTTTTTTTTTTTTTXPTTTTTTTTT....>=1000 chars | Silvergskjfhsksuhff......>=1000 chars | 4444444...>=1000 chars |
      | Admin-Org-2 | Repo-2     | some Symbols ~!`$%^&*(_)-+=© |                     | #!=`   ~/.*                                    | #!=   `~/.*                           | #!=`   ~/.*            |
      | Owner-Org-2 | Repo-2     | Int 12345                    | 10                  | ---                                            | ---                                   | ---                    |
      | Admin-Org-2 | Repo-2     | blankJsonObject              | {}                  | ---                                            | ---                                   | ---                    |
      | Owner-Org-2 | Repo-2     | blankJsonArray               | []                  | ---                                            | ---                                   | ---                    |
      | Admin-Org-2 | Repo-2     | dataJsonArray                | [99, "some string"] | ---                                            | ---                                   | ---                    |
      | Owner-Org-2 | Repo-2     | undefined                    | null                | ---                                            | ---                                   | ---                    |
      | Admin-Org-2 | Repo-2     | empty                        |                     | ---                                            | ---                                   | ---                    |
    And the user requested to updateEntry in the relevant repository without "value" key at all
      | apiKey      | repository | key        |
      | Owner-Org-1 | Repo-2     | lost value |
    Then new entries should be stored in the relevant repository "Repo-2"
    And for each request user should get the successful response with new version for each entry
      | version |
      | 2       |
      | 3       |
      | 4       |
      | 5       |
      | 6       |
      | 7       |
      | 8       |
      | 9       |
      | 10      |
     

  #__________________________________________________NEGATIVE___________________________________________________________


  #23
  Scenario: Fail to updateEntry due to restricted permission upon the "Member" API key was applied
    When the user requested to updateEntry in the relevant repository with following details
      | apiKey       | repository | key                        | value | instrumentId | name | DecimalPrecision | Rounding |
      | Member-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |       | JPY          | Yen  | 4                | down     |
    Then the user should get following error
      | errorCode | errorMessage      |
      | 500       | Permission denied |


  #24
  Scenario: Fail to updateEntry due to specified Repository doesn't exist
    When the user requested to updateEntry in the "non-existent" repository
      | apiKey      | repository   | key     | value |
      | Owner-Org-1 | non-existent | new-key |       |
    Then the user should get following error
      | errorCode | errorMessage                        |
      | 500       | Repository 'non-existent' not found |


  #25
  Scenario: Fail to updateEntry upon the Owner deleted the "Organization"
    Given the related organization Owner has deleted organization name "Org-2"
    When the user requested to updateEntry applying "Admin" apiKey from the deleted organization "Org-2"
      | apiKey      | repository | key     | value |
      | Admin-Org-2 | Repo-2     | new-key |       |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #26
  Scenario: Fail to updateEntry upon the "Admin" apiKey was deleted from the Organization
    Given organization "Org-1" Owner has deleted the relevant "Member" apiKey from it
    When the user requested to updateEntry applying the deleted "Member" apiKey from organization "Org-1"
      | apiKey       | repository | key     | value |
      | Member-Org-1 | Repo-1     | new-key |       |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #27
  Scenario: Fail to updateEntry due to invalid apiKey was applied
    When the user requested to updateEntry applying the "invalid" apiKey
      | apiKey  | repository | key     | value |
      | invalid | Repo-3     | new-key |       |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #28
  Scenario: Fail to updateEntry with empty or undefined apiKey
    When the user requested to updateEntry without specifying the apiKey
      | apiKey | repository | key     | value |
      |        | Repo-3     | new-key |       |
      | null   | Repo-3     | new-key |       |
    And the user requested to updateEntry without "apiKey" at all
      | repository | key     | value |
      | Repo-3     | new-key |       |
    Then for each request user should get following error
      | errorCode | errorMessage            |
      | 500       | Please specify 'apiKey' |


  #29
  Scenario: Fail to updateEntry with empty or undefined Repository name
    When the user requested to updateEntry without specifying repository name
      | apiKey      | repository | key     | value |
      | Owner-Org-1 |            | new-key |       |
      | Owner-Org-1 | null       | new-key |       |
    And the user requested to updateEntry without "repository" key name at all
      | apiKey      | key     | value |
      | Owner-Org-1 | new-key |       |
    Then for each request user should get following error
      | errorCode | errorMessage                |
      | 500       | Please specify 'repository' |


  #30
  Scenario: Fail to updateEntry with empty or undefined Key field
    When the user requested to updateEntry without specifying key name
      | apiKey      | repository | key  | value |
      | Owner-Org-1 | Repo-3     |      |       |
      | Owner-Org-1 | Repo-3     | null |       |
    And the user requested to updateEntry without "key" field at all
      | apiKey      | repository | value |
      | Owner-Org-1 | Repo-3     |       |
    Then for each request user should get following error
      | errorCode | errorMessage         |
      | 500       | Please specify 'key' |


  #31
  Scenario: Fail to updateEntry with non-existent Key field
    When the user requested to updateEntry with non-existent key name
      | apiKey      | repository | key          | value |
      | Owner-Org-1 | Repo-3     | non-existent | {}    |
    Then the user should get following error
      | errorCode | errorMessage                                     |
      | 500       | Repository 'Repo-3' key 'non-existent' not found |