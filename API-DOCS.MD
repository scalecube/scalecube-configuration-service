# API documentation for the Configuration Service
## Getting a token is done via the Organization Service 
# ConfigurationService:
* createRepository(CreateRepositoryRequest request)
  * Request to create a configuration repository and requires a write level permissions.
  * param: CreateRepositoryRequest:
    * String repository: The repository name
    * Object token: The request token
  * returns Acknowledgment
* fetch(FetchRequest request)
  * Fetch request requires read level permissions to get entry object from the store.
  * param FetchRequest:
    * String repository: The repository name
    * Object token: The request token
    * String key: The requested data key (a configuration setting)
  * returns FetchResponse:
    * String key: fetch key
    * Object value: fetch value
* entries(FetchRequest request)
  * Entries request requires read level permissions to get entry object from the store.
  * param FetchRequest:
    * String repository: The repository name
    * Object token: The request token
    * String key: The requested data keys (configuration settings)
  * returns a collection of FetchResponse:
    * entries (array)
      * String key: fetch key
      * Object value: fetch value
