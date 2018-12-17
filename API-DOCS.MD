# API documentation for the Configuration Service
## Getting a token (API key) is done via the Organization Service which provide the permission level to potential consumer according to the granted role (Member/Admin/Owner)
# ConfigurationService:
* createRepository(CreateRepositoryRequest request)
  * Request to create a configuration repository and requires a write level permissions granted for owner role only.
  * param: CreateRepositoryRequest:
    * String repository: The repository name (customed name of the repository)
    * Object token: The request token (API key)
  * returns Acknowledgment

* save(SaveRequest request)
  * Request to save an entry in a repository and requires a write level permissions granted for owner and admin role only.
  * param: CreateRepositoryRequest:
    * String repository: The repository name (already stored name of the created repository)
    * Object token: The request token (API key)
    * String key: key name (customed name for specified configuration setting in the repository)
    * Object value: node name (customed name for specified configuration setting in the repository)
  * returns Acknowledgment

* fetch(FetchRequest request)
  * Fetch request requires read level permissions to get entry object for a specific entity from the store which enabled for each of the accessible role (Member/Admin/Owner).
  * param FetchRequest:
    * String repository: The repository name (already stored name of the created repository)
    * Object token: The request token (API key)
    * String key: The requested data key (a configuration setting which is already stored as the key name for that purpose)
  * returns FetchResponse:
    * String key: fetch key
    * Object value: fetch value

* entries(FetchRequest request)
  * Entries request requires read level permissions to get entry object for the all entities from the store which enabled for each of the accessible role (Member/Admin/Owner).
  * param FetchRequest:
    * String repository: The repository name (already stored name of the created repository)
    * Object token: The request token (API key)
    * String key: The requested data keys (configuration settings which are already stored as the key names for that purpose)
  * returns a collection of FetchResponse:
    * entries (array)
      * String key: fetch key
      * Object value: fetch value

* delete(DeleteRequest request)
  * Request to delete a specified entry from the repository and requires a write level permissions granted for owner and admin role only.
  * param: DeleteRepositoryRequest:
    * String repository: The repository name (already stored name of the created repository)
    * Object token: The request token (API key)
    * String key: key name The requested data key (a configuration setting which is already stored as the key name for that purpose)
  * returns Acknowledgment
