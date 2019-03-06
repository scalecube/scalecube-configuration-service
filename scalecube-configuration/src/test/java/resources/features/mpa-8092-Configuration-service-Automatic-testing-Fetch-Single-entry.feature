@Configuration-service-production-ready

Feature: Integration tests for configuration service.

  Configuration service enable specified users which are granted with manager's access (related API keys: Owner&Admin) to create and manage
  the separate repositories purposed for entries collection (record) and storage. Potential customers are granted by relevant permission level (API key: Member)
  just to be able to observe the stored data in the relevant repository.

  For example managers could:

  - add a new instrument instance with the some structure
  - edit the following metadata for instrument instance: DecimalPrecision, Rounding, name, rounding
  - remove and existing instrument instance configuration
  - read (get) the specified and all the entries which were stored in the related repo

  For example customers (members) could only:
  - get the specified and all the entries which were stored in the related repo by the managers


  #GET (FETCH) THE SINGLE ENTRY

  #__________________________________________________POSITIVE___________________________________________________________


  #MPA-8092 (#3)
  Scenario: Successful get of a specific entry from the related Repo applying the all related API keys: "Owner", "Admin", "Member"
    Given the specified name "repository" was created and stored in DB with following entries
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |
      | JPY          | Yen    | 2                | down     | KEY-FOR-CURRENCY-999       |
    And the user have been granted with valid "tokens" (API keys) assigned by "Owner", "Admin" and "Member" roles
    When this user requested to get (fetch) the existent entry set by "KEY-FOR-PRECIOUS-METAL-123" key in the relevant "repository" applying each of the API keys
    Then for each request user should get only one specified key with related entry
      | instrumentId | name   | DecimalPrecision | Rounding | key                        |
      | XAG          | Silver | 4                | down     | KEY-FOR-PRECIOUS-METAL-123 |


  ##MPA-8092 (#3.1)
  #Scenario: Successful get the list of the stored entries from the Repo
  #  Given the users have got a valid "tokens" (API key) with "Owner" and "Admin" and "member" assigned roles
  #  And the relevant specified name "repository" with relevant (at least one) entries (keys and values) were created and stored in DB
  #  When each of these users requested to get the list of the existent entries (keys and values) which already exists in the relevant "repository" specified name
  #  Then each of these users should receive successful response with list of existent entries (keys and values)
#
#
  ##MPA-8092 (#3.2)
  #Scenario: Successfully get nothing upon stored entry was deleted from the Repo
  #  Given a user have got a valid "token" (API key) with assigned "Owner" role
  #  And the relevant specified name "repository" with some entry was created and stored in DB
  #  And this user deleted the "specific" entry (key and value) in the relevant "repository" specified name
  #  When this user requested to get the recently deleted entry (key and value) from the relevant "repository" specified name
  #  Then this user should receive successful response with empty object
#
#
  ##MPA-8092 (#3.3)
  #Scenario: Fail to get a non-existent entry from the Repo
  #  Given the users have got a valid "token" (API key) with "member" assigned role
  #  And there is no "repository" created and stored in DB
  #  When this user requested to get the non-existent entry (key) from non-existent "repository" specified name
  #  Then this user shouldn't receive any of the stored entries and get the error message: "repository: "specified" repository doesn't exist"
#
#
  ##MPA-8092 (#2.9)
  #Scenario: Fail to save (edit) the specific entry in the Repo upon the Owner deleted the Organization with related "Owner" API key
  #  Given the organizations "organizationId" with specified names "Org-1" and "Org-2" and emails already created
  #  And related "Owner" API key was stored in the organization with specified name "Org-1"
  #  And related "Admin" API key was stored in the organization with specified name "Org-2"
  #  And "repository" with specified name "Repo-1" was created and stored in DB by applying related "Owner" API key
  #  When the user requested to save some entry in the "repository" name "Repo-1" applying the "Admin" API key from organization with name "Org-2"
  #  Then no entry shouldn't be recorded to the relevant "repository" name "Repo-1"
  #  And the user should get the "errorMessage":"Permission denied"
#
  ##MPA-8092 (#3.4)
  #Scenario: Fail to save (edit) the value for specific entry upon the valid API key with "Owner" role was deleted with relevant organization
#
  ##MPA-8092 (#3.5)
  #Scenario: Fail to save (edit) the value for specific entry upon the issuer of valid API key with "Admin" role was downgraded to "Member"

