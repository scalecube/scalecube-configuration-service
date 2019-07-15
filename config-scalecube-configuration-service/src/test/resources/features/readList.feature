@Configuration-service-integration-tests

Feature: Integration tests for configuration service - readList.

  All organization members can use related API keys roles to get the latest versions of key-entries and/or specific versions from the related Repository.

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

  #READ LIST

  #__________________________________________________POSITIVE___________________________________________________________

  #53
  Scenario: Successful readList (latest key versions) from the related Repository
    When the user requested to readList from the repository name "Repo-1"
      | apiKey      | repository |
      | Owner-Org-1 | Repo-1     |
    Then the user should get the successful response with latest node entries versions
      | key                        | value | instrumentId | name    | DecimalPrecision | Rounding |
      | KEY-FOR-PRECIOUS-METAL-123 |       | XAU          | Gold    | 8                | down     |
      | Crypto                     |       | BTC          | Bitcoin | 10               | down     |


  #54
  Scenario: Successful readList (specific key versions) from the related Repository applying all API keys roles
    When the user requested to readList from the repository name "Repo-1"
      | apiKey       | repository | version |
      | Owner-Org-1  | Repo-1     | 1       |
      | Admin-Org-1  | Repo-1     | 2       |
      | Member-Org-1 | Repo-1     | 3       |
      | Member-Org-1 | Repo-1     | 4       |
      | Member-Org-1 | Repo-1     | 5       |
    Then for each request user should get the successful response with related node entries versions
      | key                        | value | instrumentId | name     | DecimalPrecision | Rounding |
      | KEY-FOR-PRECIOUS-METAL-123 | []    | ---          | ---      | ---              | ---      |
      | Crypto                     |       | LTC          | Litecoin | 4                | down     |
      | KEY-FOR-PRECIOUS-METAL-123 | null  | ---          | ---      | ---              | ---      |
      | Crypto                     |       | ETH          | Ethereum | 10               | up       |
      | KEY-FOR-PRECIOUS-METAL-123 |       | XAG          | Silver   | 4                | down     |
      | Crypto                     |       | BTC          | Bitcoin  | 10               | down     |
      | KEY-FOR-PRECIOUS-METAL-123 |       | XPT          | Platinum | 2                | up       |
      | KEY-FOR-PRECIOUS-METAL-123 |       | XAU          | Gold     | 8                | down     |


  #55
  Scenario: Successful readList of nothing from the related Repository upon no match was found (specific key version doesn't exist)
    When the user requested to readList from the repository name "Repo-1"
      | apiKey      | repository | version |
      | Owner-Org-1 | Repo-1     | 99      |
    Then the user should get the successful response with empty array "[]"


  #__________________________________________________NEGATIVE___________________________________________________________


  #56 (may be add the message - version should be a positive number)
  Scenario: Fail to readList due to invalid (not int) dataType version specified
    When the user requested to readList from the repository name "Repo-1" invalid version
      | apiKey      | repository | key                        | version |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | afafaf  |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | 0       |
      | Owner-Org-1 | Repo-1     | KEY-FOR-PRECIOUS-METAL-123 | -1      |
    Then for each request user should get following error
      | errorCode | errorMessage                                                |
      | 500       | Failed to decode data on message q=/configuration/readList |


  #57
  Scenario: Fail to readList due to specified Repository doesn't exist
    When the user requested to readList from the "non-existent" repository
      | apiKey      | repository   |
      | Owner-Org-1 | non-existent |
    Then the user should get following error
      | errorCode | errorMessage                        |
      | 500       | Repository 'non-existent' not found |


  #58
  Scenario: Fail to readList upon the Owner deleted the "Organization"
    Given the related organization Owner has deleted organization name "Org-2"
    When the user requested to readList applying "Owner" apiKey from the deleted organization "Org-2"
      | apiKey      | repository |
      | Owner-Org-2 | Repo-2     |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #59
  Scenario: Fail to readList upon the "Admin" apiKey was deleted from the Organization
    Given organization "Org-2" Owner has deleted the relevant "Admin" apiKey from it
    When the user requested to readList applying the deleted "Admin" apiKey from organization "Org-2"
      | apiKey      | repository |
      | Admin-Org-2 | Repo-2     |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #60
  Scenario: Fail to readList due to invalid apiKey was applied
    When the user requested to readList applying the "invalid" apiKey
      | apiKey  | repository |
      | invalid | Repo-3     |
    Then the user should get following error
      | errorCode | errorMessage              |
      | 500       | Token verification failed |


  #61
  Scenario: Fail to readList with empty or undefined apiKey
    When the user requested to readList without specifying the apiKey
      | apiKey | repository |
      |        | Repo-3     |
      | null   | Repo-3     |
    And the user requested to readList without "apiKey" at all
      | repository |
      | Repo-3     |
    Then for each request user should get following error
      | errorCode | errorMessage            |
      | 500       | Please specify 'apiKey' |


  #62
  Scenario: Fail to readList with empty or undefined Repository name
    When the user requested to readList without specifying repository name
      | apiKey      | repository |
      | Owner-Org-1 |            |
      | Owner-Org-1 | null       |
    And the user requested to readList without "repository" key name at all
      | apiKey      |
      | Owner-Org-1 |
    Then for each request user should get following error
      | errorCode | errorMessage                |
      | 500       | Please specify 'repository' |