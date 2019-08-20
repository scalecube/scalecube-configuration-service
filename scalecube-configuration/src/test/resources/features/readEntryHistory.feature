@Configuration-service-integration-tests

Feature: Integration tests for configuration service - readEntryHistory.

  All organization members can use related API keys roles to get the entries versions history per specific key from the related Repository.

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
    And repository with name "Repo-1" related to organization "Org-1" created with following versions per "Crypto" key
      | version | value | instrumentId | name     | DecimalPrecision | Rounding |
      | 1       |       | LTC          | Litecoin | 4                | down     |
      | 2       |       | ETH          | Ethereum | 10               | up       |
      | 3       |       | BTC          | Bitcoin  | 10               | down     |
    And repository with name "Repo-2" related to organization "Org-2" created with following versions per "Currency" key
      | version | value | instrumentId | name | DecimalPrecision | Rounding |
      | 1       |       | JPY          | Yen  | 4                | down     |

  #READ ENTRY HISTORY

  #__________________________________________________POSITIVE___________________________________________________________

  #63
  Scenario: Successful readEntryHistory (all existent versions) from the related Repository applying all API keys roles
    When the user requested to readEntryHistory from the repository name "Repo-1"
      | apiKey       | repository | key                        |
      | Owner-Org-1  | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |
      | Admin-Org-1  | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |
      | Member-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |
    Then for each request the user should get the successful response with node entries of all existing versions
      | version | value | instrumentId | name     | DecimalPrecision | Rounding |
      | 1       | []    | ---          | ---      | ---              | ---      |
      | 2       | null  | ---          | ---      | ---              | ---      |
      | 3       |       | XAG          | Silver   | 4                | down     |
      | 4       |       | XPT          | Platinum | 2                | up       |
      | 5       |       | XAU          | Gold     | 8                | down     |


  #__________________________________________________NEGATIVE___________________________________________________________


  #64
  Scenario: Fail to readEntryHistory due to specified Repository doesn't exist
    When the user requested to readEntryHistory from the "non-existent" repository
      | apiKey      | repository   | key     |
      | Owner-Org-1 | non-existent | new-key |
    Then the user should get following error
      | errorCode | errorMessage                        |
      | 500       | Repository 'non-existent' not found |


  #65
  Scenario: Fail to readEntryHistory upon the Owner deleted the "Organization"
    Given the related organization Owner has deleted organization name "Org-2"
    When the user requested to readEntryHistory applying "Owner" apiKey from the deleted organization "Org-2"
      | apiKey      | repository | key     |
      | Owner-Org-2 | Repo-2     | new-key |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #66
  Scenario: Fail to readEntryHistory upon the "Admin" apiKey was deleted from the Organization
    Given organization "Org-2" Owner has deleted the relevant "Admin" apiKey from it
    When the user requested to readEntryHistory applying the deleted "Admin" apiKey from organization "Org-2"
      | apiKey      | repository | key     |
      | Admin-Org-2 | Repo-2     | new-key |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #67
  Scenario: Fail to readEntryHistory due to invalid apiKey was applied
    When the user requested to readEntryHistory applying the "invalid" apiKey
      | apiKey  | repository | key     |
      | invalid | Repo-3     | new-key |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #68
  Scenario: Fail to readEntryHistory with empty or undefined apiKey
    When the user requested to readEntryHistory without specifying the apiKey
      | apiKey | repository | key     |
      |        | Repo-3     | new-key |
      | null   | Repo-3     | new-key |
    And the user requested to readEntryHistory without "apiKey" at all
      | repository | key     |
      | Repo-3     | new-key |
    Then for each request user should get following error
      | errorCode | errorMessage            |
      | 500       | Please specify 'apiKey' |


  #69
  Scenario: Fail to readEntryHistory with empty or undefined Repository name
    When the user requested to readEntryHistory without specifying repository name
      | apiKey      | repository | key     |
      | Owner-Org-1 |            | new-key |
      | Owner-Org-1 | null       | new-key |
    And the user requested to readEntryHistory without "repository" key name at all
      | apiKey      | key     |
      | Owner-Org-1 | new-key |
    Then for each request user should get following error
      | errorCode | errorMessage                |
      | 500       | Please specify 'repository' |


  #70
  Scenario: Fail to readEntryHistory with empty or undefined Key field
    When the user requested to readEntryHistory without specifying key name
      | apiKey      | repository | key  |
      | Owner-Org-1 | Repo-3     |      |
      | Owner-Org-1 | Repo-3     | null |
    And the user requested to readEntryHistory without "key" field at all
      | apiKey      | repository |
      | Owner-Org-1 | Repo-3     |
    Then for each request user should get following error
      | errorCode | errorMessage         |
      | 500       | Please specify 'key' |


  #71
  Scenario: Fail to readEntryHistory with non-existent Key field
    When the user requested to readEntryHistory with non-existent key name
      | apiKey      | repository | key          |
      | Owner-Org-2 | Repo-2     | non-existent |
    Then the user should get following error
      | errorCode | errorMessage                                     |
      | 500       | Repository 'Repo-2' key 'non-existent' not found |