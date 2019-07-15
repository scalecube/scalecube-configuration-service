@Configuration-service-integration-tests

Feature: Integration tests for configuration service - readEntry.

  All organization members can use related API keys roles to get the latest entry version and/or specific version from the related Repository.

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
    And repository with name "Repo-1" related to organization "Org-1" created with following versions per "KEY-FOR-PRECIOUS-METAL-123" key
      | version | value | instrumentId | name     | DecimalPrecision | Rounding |
      | 1       | []    | ---          | ---      | ---              | ---      |
      | 2       | null  | ---          | ---      | ---              | ---      |
      | 3       |       | XAG          | Silver   | 4                | down     |
      | 4       |       | XPT          | Platinum | 2                | up       |
      | 5       |       | XAU          | Gold     | 8                | down     |
    And repository with name "Repo-2" related to organization "Org-2" created with following versions per "Currency" key
      | version | value | instrumentId | name | DecimalPrecision | Rounding |
      | 1       |       | JPY          | Yen  | 4                | down     |


  #READ ENTRY

  #__________________________________________________POSITIVE___________________________________________________________

  #42
  Scenario: Successful readEntry (latest version) from the related Repository applying all API keys roles
    When the user requested to readEntry from the repository name "Repo-1"
      | apiKey       | repository | key                        |
      | Owner-Org-1  | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |
      | Admin-Org-1  | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |
      | Member-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |
    Then for each request user should get the successful response with latest entries version
      | key                        | value | instrumentId | name | DecimalPrecision | Rounding |
      | KEY-FOR-PRECIOUS-METAL-123 |       | XAU          | Gold | 8                | down     |


  #43
  Scenario: Successful readEntry (specific version) from the related Repository
    When the user requested to readEntry from the repository name "Repo-1"
      | apiKey       | repository | key                        | version |
      | Owner-Org-1  | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | 1       |
      | Admin-Org-1  | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | 2       |
      | Member-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | 3       |
      | Member-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | 4       |
      | Member-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | 5       |
    Then for each request user should get the successful response with related entries version
      | key                        | value | instrumentId | name     | DecimalPrecision | Rounding |
      | KEY-FOR-PRECIOUS-METAL-123 | []    | ---          | ---      | ---              | ---      |
      | KEY-FOR-PRECIOUS-METAL-123 | null  | ---          | ---      | ---              | ---      |
      | KEY-FOR-PRECIOUS-METAL-123 |       | XAG          | Silver   | 4                | down     |
      | KEY-FOR-PRECIOUS-METAL-123 |       | XPT          | Platinum | 2                | up       |
      | KEY-FOR-PRECIOUS-METAL-123 |       | XAU          | Gold     | 8                | down     |


  #__________________________________________________NEGATIVE___________________________________________________________


  #44
  Scenario: Fail to readEntry due to non-existent version specified
    When the user requested to readEntry from the repository name "Repo-1" non-existent version
      | apiKey      | repository | key                        | version |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | 99      |
    Then the user should get following error
      | errorCode | errorMessage                                            |
      | 500       | Key 'KEY-FOR-PRECIOUS-METAL-123' version '99' not found |

  #44.1 (may be add the message - version should be a positive number)
  Scenario: Fail to readEntry due to invalid (not int) dataType version specified
    When the user requested to readEntry from the repository name "Repo-1" invalid version
      | apiKey      | repository | key                        | version |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | afafaf  |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | 0       |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | -1      |
    Then for each request user should get following error
      | errorCode | errorMessage                                                |
      | 500       | Failed to decode data on message q=/configuration/readEntry |


  #45
  Scenario: Fail to readEntry due to specified Repository doesn't exist
    When the user requested to readEntry from the "non-existent" repository
      | apiKey      | repository   | key     |
      | Owner-Org-1 | non-existent | new-key |
    Then the user should get following error
      | errorCode | errorMessage                        |
      | 500       | Repository 'non-existent' not found |


  #46
  Scenario: Fail to readEntry upon the Owner deleted the "Organization"
    Given the related organization Owner has deleted organization name "Org-2"
    When the user requested to readEntry applying "Owner" apiKey from the deleted organization "Org-2"
      | apiKey      | repository | key     |
      | Owner-Org-2 | Repo-2     | new-key |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #47
  Scenario: Fail to readEntry upon the "Admin" apiKey was deleted from the Organization
    Given organization "Org-2" Owner has deleted the relevant "Admin" apiKey from it
    When the user requested to readEntry applying the deleted "Admin" apiKey from organization "Org-2"
      | apiKey      | repository | key     |
      | Admin-Org-2 | Repo-2     | new-key |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #48
  Scenario: Fail to readEntry due to invalid apiKey was applied
    When the user requested to readEntry applying the "invalid" apiKey
      | apiKey  | repository | key     |
      | invalid | Repo-3     | new-key |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #49
  Scenario: Fail to readEntry with empty or undefined apiKey
    When the user requested to readEntry without specifying the apiKey
      | apiKey | repository | key     |
      |        | Repo-3     | new-key |
      | null   | Repo-3     | new-key |
    And the user requested to readEntry without "apiKey" at all
      | repository | key     |
      | Repo-3     | new-key |
    Then for each request user should get following error
      | errorCode | errorMessage            |
      | 500       | Please specify 'apiKey' |


  #50
  Scenario: Fail to readEntry with empty or undefined Repository name
    When the user requested to readEntry without specifying repository name
      | apiKey      | repository | key     |
      | Owner-Org-1 |            | new-key |
      | Owner-Org-1 | null       | new-key |
    And the user requested to readEntry without "repository" key name at all
      | apiKey      | key     |
      | Owner-Org-1 | new-key |
    Then for each request user should get following error
      | errorCode | errorMessage                |
      | 500       | Please specify 'repository' |


  #51
  Scenario: Fail to readEntry with empty or undefined Key field
    When the user requested to readEntry without specifying key name
      | apiKey      | repository | key  |
      | Owner-Org-1 | Repo-3     |      |
      | Owner-Org-1 | Repo-3     | null |
    And the user requested to readEntry without "key" field at all
      | apiKey      | repository |
      | Owner-Org-1 | Repo-3     |
    Then for each request user should get following error
      | errorCode | errorMessage         |
      | 500       | Please specify 'key' |


  #52
  Scenario: Fail to readEntry with non-existent Key field
    When the user requested to readEntry with non-existent key name
      | apiKey      | repository | key          |
      | Owner-Org-2 | Repo-2     | non-existent |
    Then the user should get following error
      | errorCode | errorMessage                                     |
      | 500       | Repository 'Repo-2' key 'non-existent' not found |