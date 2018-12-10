# API documentation for the Configuration Service
## Getting a token (API key) is done via the Organization Service which provide the permission level to potential consumer according to the granted role (Member/Admin/Owner)
# ConfigurationService:
* createRepository(CreateRepositoryRequest request)
  * Request to create a configuration repository and requires a write level permissions granted for owner role only.
  * param: CreateRepositoryRequest:
    * String repository: The repository name (custom the name of the created repository)
    * Object token: The request token (API key)
  * returns Acknowledgment
* fetch(FetchRequest request)
  * Fetch request requires read level permissions to get entry object for a specific entity from the store which enabled for each of the accessible role (Member/Admin/Owner).
  * param FetchRequest:
    * String repository: The repository name (already stored name of the created repository)
    * Object token: The request token (API key)
    * String key: The requested data key (a configuration setting which is already stored as the key name for that purpose)
  * returns FetchResponse:
    * String key: fetch key (already stored key name)
    * Object value: fetch value (already stored configuration node name in the repository)
* entries(FetchRequest request)
  * Entries request requires read level permissions to get entry object for the all entities from the store which enabled for each of the accessible role (Member/Admin/Owner).
  * param FetchRequest:
    * String repository: The repository name
    * Object token: The request token (API key)
    * String key: The requested data keys (configuration settings which are already stored as the key names for that purpose)
  * returns a collection of FetchResponse:
    * entries (array)
      * String key: fetch key
      * Object value: fetch value
