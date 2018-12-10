ConfigurationService:
* createRepository(CreateRepositoryRequest request)
  * Request to create a configuration repository and requires a write level permissions.
  * param: CreateRepositoryRequest:
    * String repository: The repository name
    * Object token: The request token
  * returns Acknowledgment
* fetch(FetchRequest request)
  * Fetch request
    * requires read level permissions to get entry object from the store.
  * param FetchRequest:
    * String repository: The repository name
    * Object token: The request token
    * String key: The requested data key (a configuration setting)
  * returns FetchResponse:
    * String key: fetch key
    * Object value: fetch value
* entries(FetchRequest request)
  * requires read level permissions to get entry object from the store.
  * 
