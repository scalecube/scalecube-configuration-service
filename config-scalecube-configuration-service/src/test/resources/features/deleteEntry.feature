@Configuration-service-integration-tests

Feature: Integration tests for configuration service - deleteEntry.

  Only the managers with "Owner" and "Admin" API keys roles should be able to delete the specific entries in the related Repository.

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
      | repository | key                        | value | instrumentId | name    | DecimalPrecision | Rounding |
      | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |       | XAG          | Silver  | 4                | down     |
      | Repo-1     | Currency                   |       | JPY          | Yen     | 2                | up       |
      | Repo-1     | Crypto                     |       | BTC          | Bitcoin | 10               | down     |
    And repository with name "Repo-2" related to organization "Org-2" created with {"version":1} per each key
      | repository | key                        | value | instrumentId | name   | DecimalPrecision | Rounding |
      | Repo-2     | KEY-FOR-PRECIOUS-METAL-123 |       | XAG          | Silver | 4                | down     |


  #DELETE ENTRY

  #__________________________________________________POSITIVE___________________________________________________________

  #32
  Scenario: Successful delete one of the identical keys (entries) from the related Repository applying some of the managers' API keys
    When the user requested to deleteEntry from the repository name "Repo-1"
      | apiKey      | repository | key                        |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |
      | Admin-Org-1 | Repo-1     | Currency                   |
    Then for each request user should get the successful response with the "empty" object
    But the identical entry with related key shouldn't be deleted from the the repository "Repo-2"
      | repository | key                        | value | instrumentId | name   | DecimalPrecision | Rounding |
      | Repo-2     | KEY-FOR-PRECIOUS-METAL-123 |       | XAG          | Silver | 4                | down     |
    And the user requested to get the recently deleted key from the repository "Repo-1"
      | apiKey      | repository | key                        |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 |
    And the user should get following error
      | errorCode | errorMessage                                                   |
      | 500       | Repository 'Repo-1' key 'KEY-FOR-PRECIOUS-METAL-123' not found |


  #__________________________________________________NEGATIVE___________________________________________________________


  #33
  Scenario: Fail to deleteEntry due to restricted permission upon the "Member" API key was applied
    When the user requested to deleteEntry in the relevant repository with following details
      | apiKey       | repository | key    |
      | Member-Org-1 | Repo-1     | Crypto |
    Then the user should get following error
      | errorCode | errorMessage      |
      | 500       | Permission denied |


  #34
  Scenario: Fail to deleteEntry due to specified Repository doesn't exist
    When the user requested to deleteEntry in the "non-existent" repository
      | apiKey      | repository   | key     |
      | Owner-Org-1 | non-existent | new-key |
    Then the user should get following error
      | errorCode | errorMessage                        |
      | 500       | Repository 'non-existent' not found |


  #35
  Scenario: Fail to deleteEntry upon the Owner deleted the "Organization"
    Given the related organization Owner has deleted organization name "Org-2"
    When the user requested to deleteEntry applying "Owner" apiKey from the deleted organization "Org-2"
      | apiKey      | repository | key     |
      | Owner-Org-2 | Repo-2     | new-key |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #36
  Scenario: Fail to deleteEntry upon the "Admin" apiKey was deleted from the Organization
    Given organization "Org-2" Owner has deleted the relevant "Admin" apiKey from it
    When the user requested to deleteEntry applying the deleted "Admin" apiKey from organization "Org-2"
      | apiKey      | repository | key     |
      | Admin-Org-2 | Repo-2     | new-key |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #37
  Scenario: Fail to deleteEntry due to invalid apiKey was applied
    When the user requested to deleteEntry applying the "invalid" apiKey
      | apiKey  | repository | key     |
      | invalid | Repo-3     | new-key |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #38
  Scenario: Fail to deleteEntry with empty or undefined apiKey
    When the user requested to deleteEntry without specifying the apiKey
      | apiKey | repository | key     |
      |        | Repo-3     | new-key |
      | null   | Repo-3     | new-key |
    And the user requested to deleteEntry without "apiKey" at all
      | repository | key     |
      | Repo-3     | new-key |
    Then for each request user should get following error
      | errorCode | errorMessage            |
      | 500       | Please specify 'apiKey' |


  #39
  Scenario: Fail to deleteEntry with empty or undefined Repository name
    When the user requested to deleteEntry without specifying repository name
      | apiKey      | repository | key     |
      | Owner-Org-1 |            | new-key |
      | Owner-Org-1 | null       | new-key |
    And the user requested to deleteEntry without "repository" key name at all
      | apiKey      | key     |
      | Owner-Org-1 | new-key |
    Then for each request user should get following error
      | errorCode | errorMessage                |
      | 500       | Please specify 'repository' |


  #40
  Scenario: Fail to deleteEntry with empty or undefined Key field
    When the user requested to deleteEntry without specifying repository name
      | apiKey      | repository | key  |
      | Owner-Org-1 | Repo-3     |      |
      | Owner-Org-1 | Repo-3     | null |
    And the user requested to deleteEntry without "key" field at all
      | apiKey      | repository |
      | Owner-Org-1 | Repo-3     |
    Then for each request user should get following error
      | errorCode | errorMessage         |
      | 500       | Please specify 'key' |


  #41
  Scenario: Fail to deleteEntry with non-existent Key field
    When the user requested to deleteEntry with non-existent key name
      | apiKey      | repository | key          |
      | Owner-Org-2 | Repo-2     | non-existent |
    Then the user should get following error
      | errorCode | errorMessage                                     |
      | 500       | Repository 'Repo-2' key 'non-existent' not found |