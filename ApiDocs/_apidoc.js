/**
 * @api {fetch endpoint} /io.scalecube.configuration.api.ConfigurationService/fetch Fetch (get) specified entry
 * @apiName Fetch
 * @apiGroup Fetch
 * @apiVersion 2.0.17-SNAPSHOT
 * @apiDescription This operation enable you to get specific value (either string or object) by retrieving specified entry (key) from the relevant Repository and enabled for each accessible permission level (read&write) granted for owner either admin or member role
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name for relevant configuration setting in the repository
 *
 * @apiSuccess {String} key Specified key name for relevant configuration setting in the repository
 * @apiSuccess {Object} value Specified node name applied for relevant configuration settings in the repository
 *
 * @apiParamExample {json} Request-Example:
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *     }
 *
 * @apiSuccessExample Success-Response:
 *     {
 *        "key": "specifiedKeyName"
 *        "value": {
 *                    "name": "Gold",
 *                    "instrument": "XAU",
 *                    "DecimalPrecision" : "2",
 *                    "Rounding": "down"
 *                 }
 *     }
 *
 * @apiError {String} field invalid or non-existent key name (entry in the repository)
 * @apiErrorExample {json} Error-Response:
 *     {
 *        errorCode":500,
 *        "errorMessage":"16562665EC17CDF08E97"
 *     }
 */
{
  "name": "Configuration service",
  "version": "2.0.17-SNAPSHOT",
  "description": "Configuration service API",
  "title": "API documentation for the Configuration Service",
  "url" : "Websocket: wss://localhost:port/\nRSocket: wss://localhost:port/\nHTTP: https://localhost:port",
  "template": {
    "forceLanguage" : "en"
  },
  "order": [
    "Overview",
    "GettingStarted",
    "InteractiveAPIExplorer",
    "CreateRepository",
    "Save",
    "Fetch",
    "Entries",
    "Delete"
  ]
}
{
  "name": "Configuration service",
  "version": "2.0.17-SNAPSHOT",
  "description": "Configuration service API",
  "title": "API documentation for the Configuration Service",
  "url" : "wss://$FORMATTED_BRANCH_NAME.genesis.om2.com",
  "template": {
    "forceLanguage" : "en"
  },
  "order": [
    "Overview",
    "GettingStarted",
    "InteractiveAPIExplorer",
    "CreateRepository",
    "Save",
    "Fetch",
    "Entries",
    "Delete"
  ]
}
/**
 * @api {create endpoint} /io.scalecube.configuration.api.ConfigurationService/createRepository Create new Repo
 * @apiVersion 2.0.17-SNAPSHOT
 * @apiName CreateRepository
 * @apiGroup CreateRepository
 *
 * @apiDescription This operation enable you to create the specific Repository for collecting and storing the relevant entries and requires a write level permission granted for owner role only
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 *
 * @apiSuccess Acknowledgment Empty object
 *
 * @apiParamExample {json} Request-Example:
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *     }
 *
 * @apiSuccessExample Success-Response:
 *     {
 *
 *     }
 *
 * @apiError {String} field invalid permission level for specified API key
 * @apiErrorExample {json} Error-Response:
 *     {
 *        errorCode":500,
 *        "errorMessage":"Role 'Admin' has insufficient permissions for the requested operation: CreateRepoitory"
 *     }
 */
 *
/**
 * @api {entries endpoint} /io.scalecube.configuration.api.ConfigurationService/entries Fetch (get) all entries
 * @apiName Entries
 * @apiGroup Entries
 * @apiVersion 2.0.17-SNAPSHOT
 * @apiDescription This operation enable you to get all values (either string or object) by retrieving all the entries (keys) from the relevant Repository and enabled for each accessible permission level (read&write) granted for owner either admin or member role
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name for relevant configuration setting in the repository
 *
 * @apiSuccess {Array} entries List of all entries from the relevant configuration setting in the repository
 * @apiSuccess {String} key Specified key name for relevant configuration setting in the repository
 * @apiSuccess {Object} value Specified node name applied for relevant configuration settings in the repository
 *
 * @apiParamExample {json} Request-Example:
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *     }
 *
 * @apiSuccessExample Success-Response:
 *     {
 *         "key": "specifiedKeyName"
 *         "value": {
 *                    "entries": [
 *                      {
 *                        "instrumentInstance": {
 *                          "name": "Bitcoin",
 *                           "instrument": "BTC",
 *                           "DecimalPrecision" : "2",
 *                           "Rounding": "down"
 *                        },
 *                        "key": "8DFE2CAA35AD62F74E63"
 *                      },
 *                      {
 *                        "value": {
 *                          "name": "Gold",
 *                           "instrument": "XAU",
 *                           "DecimalPrecision" : "2",
 *                           "Rounding": "down"
 *                        },
 *                        "key": "16562665EC17CDF08E97"
 *                      },
 *                      {
 *                        "value": {
 *                          "name": "USOIL",
 *                           "instrument": "OIL",
 *                           "DecimalPrecision" : "2",
 *                           "Rounding": "down"
 *                        },
 *                        "key": "FCE8459CA0A728BC0922"
 *                      }
 *                  }
 *     }
 *@apiError {String} field invalid API key (token)
    * @apiErrorExample {json} Error-Response:
 *     {
 *        errorCode":500,
 *        "errorMessage":"Token verification failed"
 *     }
 */
/**
 * @apiDefine BadRequestError
 * @apiVersion 2.0.17-SNAPSHOT
 *
 * @apiError BadRequestError The request didn't pass validation
 *
 * @apiErrorExample Error-Response:
 * {
 *   "errorCode": "400"
 *   "errorMessage": "Bad request."
 * }
 */

/**
 * @apiDefine InternalServerError
 *
 * @apiError (Error 500) InternalServerError Error happened during request processing
 *
 * @apiErrorExample Error-Response:
 * {
 *   "errorCode": "500"
 *   "errorMessage": "Error message"
 * }
 */

/**
 * @apiDefine ServiceUnavailableError
 *
 * @apiError (Error 503) ServiceUnavailableError Service in not available to accept requests
 *
 * @apiErrorExample Error-Response:
 * {
 *   "errorCode": "503"
 *   "errorMessage": "No reachable member with such service: %s"
 * }
 */
/**
 * @api {delete endpoint} /io.scalecube.configuration.api.ConfigurationService/delete Delete specified entry
 * @apiName Delete
 * @apiGroup Delete
 * @apiVersion 2.0.17-SNAPSHOT
 * @apiDescription This operation enable you to delete a specified entry (key) from the relevant Repository and requires a write level permission granted for owner either admin role only
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name for relevant configuration setting in the repository
 *
 * @apiSuccess Acknowledgment Empty object
 *
 * @apiParamExample {json} Request-Example:
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *     }
 *
 * @apiSuccessExample Success-Response:
 *     {
 *
 *     }
 * @apiError {String} field invalid permission level for specified API key
 * @apiErrorExample {json} Error-Response:
 *     {
 *        errorCode":500,
 *        "errorMessage":"Role 'Member' has insufficient permissions for the requested operation: Delete"
 *     }
 *//**
 * @api {save endpoint} /io.scalecube.configuration.api.ConfigurationService/save SaveUpdate the entry
 * @apiName Save
 * @apiGroup Save
 * @apiVersion 2.0.17-SNAPSHOT
 * @apiDescription This operation enable you to save either to update (edit) a specified entry in the relevant Repository and requires a write level permission granted for owner either admin role only.
 * Upon the specified entry is saved then it could be updated i.e. overwritten by the common method
 *
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name for relevant configuration setting in the repository
 * @apiParam {JsonNode} value Specified node name applied for relevant configuration settings in the repository
 *
 * @apiSuccess Acknowledgment Empty object
 *
 * @apiParamExample {json} Request-Example:
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName",
 *         "value": {
 *                     "name": "Gold",
 *                     "instrument": "XAU",
 *                     "DecimalPrecision" : "2",
 *                     "Rounding": "down"
 *                  }
 *     }
 *
 * @apiSuccessExample Success-Response:
 *     {
 *
 *     }
 *
 * @apiError {String} field invalid or non-existent repository name
 * @apiErrorExample {json} Error-Response:
 *     {
 *        errorCode":500,
 *        "errorMessage":"Failed to open bucket: 'ORG-ACF8F702CE82DE56D737-Name'
 *     }
 */
/**
* @api {Transport protocols} / Getting Started
* @apiName GettingStarted
* @apiGroup Overview
* @apiVersion 2.0.17-SNAPSHOT
* @apiDescription Configuration service enable you to integrate the API in order to create and manage the separate repositories purposed for entries collection and storage.
*
*
* <b>Getting Started</b>
*
*
* All API endpoints are documented below.
* <br> You can try out any query in realtime using our interactive API.
* Actually all methods require API key authorization since they provide a specific permission level for the each user.
* So there is a necessity to get the API key (token - assigned with relevant role Member/Admin/Owner) issued via Organization Service which is basically purposed for organization managers
* who provide such kind permission level to potential costumers.
* <br>Thus, we recommend you first to be granted with valid API key assigned with relevant role (permission level) to be able to use this API key across all service endpoints.
*
* Validation for the object entities is handled by scalecube services and do the next upon the request object:
* >~ ignores any excessive keys and values added besides the required parameters
* ><br>~ doesn't ignore the keys duplicates and takes the last values which applied for each of the relevant key duplicate
*/

/**
  * @api {Host addresses} / Interactive API Explorer
  * @apiName InteractiveAPIExplorer
  * @apiGroup Overview
  * @apiVersion 2.0.17-SNAPSHOT
  * @apiDescription For example <b>Websocket</b> connection is one of the accessible ways to use the API, so please follow the steps below to connect and perform requests via Sandbox:
  <ul>
                 <li> Navigate to the sandbox: <a href="http://scalecube.io/api-sandbox/app/index.html">Scalecube sandbox</a> </li>
                 <li> Click on the <b>Settings</b> button and choose the relevant <b>transort</b> and host <b>address</b>: wss://configuration-service-7070.genesis.om2.com </li>
                 <li> Click on <b>Import icon</b> and copy-paste the template.json file path <a href="https://raw.githubusercontent.com/scalecube/scalecube-configuration-service/develop/API-Calls-examples.json">Configuration service endpoints.json</a></li>
                 <li> Click on the <b>Connect</b> button (now you are connected to the environment) and push <b>Send</b> button to make your request</li>
  </ul>
*/
/**
 * @api {endpoint / fetch} /io.scalecube.configuration.api.ConfigurationService/fetch Fetch (get) specified entry
 * @apiName Fetch
 * @apiGroup Fetch
 * @apiVersion 2.0.18-SNAPSHOT
 * @apiDescription This operation enable you to get specific value (either string or object) by retrieving specified entry (key) from the relevant Repository and enabled for each accessible permission level (read&write) granted for owner either admin or member role
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name for relevant configuration setting in the repository
 *
 * @apiSuccess {String} key Specified key name for relevant configuration setting in the repository
 * @apiSuccess {Object} value Specified node name applied for relevant configuration settings in the repository
 *
 * @apiParamExample {json} Request-Example (WS):
 *     {
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/fetch",
 *         "sid": 1,
 *         "d":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (RS):
 *     {
 *       "metadata":{
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/fetch"
 *         },
 *         "data":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (HTTP):
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *     }
 *
 * @apiSuccessExample Success-Response (WS):
 *     {
 *         "q":"/io.scalecube.configuration.api.ConfigurationService/fetch",
 *         "sid":1,
 *         "d":{
 *         "value": {
 *                    "name": "Gold",
 *                    "instrument": "XAU",
 *                    "DecimalPrecision" : "2",
 *                    "Rounding": "down"
 *                  },
 *         "key": "specifiedKeyName"
 *         }
 *     }
 *
 *     {
 *          "sig":1,
 *          "sid":1
 *     }
 *
 * @apiSuccessExample Success-Response (RS):
 *    {
 *        "data":{
 *        "value": {
 *                   "name": "Gold",
 *                   "instrument": "XAU",
 *                   "DecimalPrecision" : "2",
 *                   "Rounding": "down"
 *                 },
 *        "key": "specifiedKeyName"
 *       },
 *       "metadata":{
 *          "q": "/io.scalecube.configuration.api.ConfigurationService/fetch"
 *       }
 *    }
 *
 * @apiSuccessExample Success-Response (HTTP):
 *    {
 *        "value": {
 *                   "name": "Gold",
 *                   "instrument": "XAU",
 *                   "DecimalPrecision" : "2",
 *                   "Rounding": "down"
 *                 },
 *        "key": "specifiedKeyName"
 *    }
 *
 * @apiError {String} field invalid or non-existent key name (entry in the repository)
 *
 * @apiErrorExample {json} Error-Response (WS):
 *    {
 *         "sig":2,
 *         "q":"/io.scalecube.services.error/500",
 *         "sid":1,
 *         "d":{
 *                 errorCode":500,
 *                 "errorMessage":"16562665EC17CDF08E97"
 *         }
 *    }
 *
 * @apiErrorExample {json} Error-Response (RS):
 *    {
 *         "data":{
 *         "errorCode":500,
 *         "errorMessage":"16562665EC17CDF08E97"
 *         },
 *         "metadata":{
 *             "q":"/io.scalecube.services.error/500"
 *         }
 *    }
 *
 * @apiErrorExample {json} Error-Response (HTTP):
 *    {
 *       errorCode":500,
 *       "errorMessage":"16562665EC17CDF08E97"
 *    }
 */
{
  "name": "Configuration service",
  "version": "2.0.18-SNAPSHOT",
  "description": "Configuration service API",
  "title": "API documentation for the Configuration Service",
  "url" : "Websocket: wss://configuration-service-7070.genesis.om2.com/\nRSocket: wss://configuration-service-9090.genesis.om2.com/\nHTTP: https://configuration-service-8080.genesis.om2.com",
  "template": {
    "forceLanguage" : "en"
  },
  "order": [
    "Overview",
    "GettingStarted",
    "TransportProtocols",
    "InteractiveAPIExplorer",
    "CreateRepository",
    "Save",
    "Fetch",
    "Entries",
    "Delete"
  ]
}
/**
 * @api {endpoint / createRepository} /io.scalecube.configuration.api.ConfigurationService/createRepository Create new Repo
 * @apiName CreateRepository
 * @apiGroup CreateRepository
 * @apiVersion 2.0.18-SNAPSHOT
 * @apiDescription This operation enable you to create the specified Repository for collecting and storing the relevant entries and requires a write permission level granted for owner role only
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 *
 * @apiSuccess Acknowledgment Empty object
 *
 * @apiParamExample {json} Request-Example (WS):
 *     {
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/createRepository",
 *         "sid": 1,
 *         "d":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (RS):
 *     {
 *       "metadata":{
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/createRepository"
 *         },
 *         "data":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (HTTP):
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *     }
 *
 *
 * @apiSuccessExample Success-Response (WS):
 *     {
 *         "q":"/io.scalecube.configuration.api.ConfigurationService/createRepository",
 *         "sid":1,
 *         "d":{}
 *     }
 *
 *     {
 *          "sig":1,
 *          "sid":1
 *     }
 *
 * @apiSuccessExample Success-Response (RS):
 *     {
 *         "data":{},
 *         "metadata":{
 *           "q": "/io.scalecube.configuration.api.ConfigurationService/createRepository"
 *         }
 *     }
 *
 * @apiSuccessExample Success-Response (HTTP):
 *     {
 *     }
 *
 * @apiError {String} field invalid permission level for specified API key
 *
 * @apiErrorExample {json} Error-Response (WS):
 *     {
 *          "sig":2,
 *          "q":"/io.scalecube.services.error/500",
 *          "sid":1,
 *          "d":{
 *                  errorCode":500,
 *                  "errorMessage":"Role 'Admin' has insufficient permissions for the requested operation: CreateRepoitory"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (RS):
 *     {
 *          "data":{
 *          "errorCode":500,
 *          "errorMessage":"Role 'Member' has insufficient permissions for the requested operation: CreateRepoitory"
 *          },
 *          "metadata":{
 *              "q":"/io.scalecube.services.error/500"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (HTTP):
 *     {
 *        errorCode":500,
 *        "errorMessage":"Role 'Admin' has insufficient permissions for the requested operation: CreateRepoitory"
 *     }
 */
/**
 * @api {endpoint / entries} /io.scalecube.configuration.api.ConfigurationService/entries Fetch (get) all entries
 * @apiName Entries
 * @apiGroup Entries
 * @apiVersion 2.0.18-SNAPSHOT
 * @apiDescription This operation enable you to get all values (array of objects) by retrieving all the entries (keys) from the relevant Repository and enabled for each accessible permission level (read&write) granted for owner either admin or member role
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 *
 * @apiSuccess {String} key Specified key name for relevant configuration setting in the repository
 * @apiSuccess {Object} value Specified node name applied for relevant configuration settings in the repository
 * @apiSuccess {Object[]} value.entries List of all entries from the relevant configuration setting in the repository (Array of Objects)
 *
 * @apiParamExample {json} Request-Example (WS):
 *     {
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/entries",
 *         "sid": 1,
 *         "d":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (RS):
 *     {
 *       "metadata":{
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/entries"
 *         },
 *         "data":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (HTTP):
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *     }
 *
 * @apiSuccessExample Success-Response (WS):
 *     {
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/entries",
 *         "sid": 1,
 *         "d":{
 *                "entries": [
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "Bitcoin",
 *                               "instrument": "BTC",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName"
 *                    }
 *                  },
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "Gold",
 *                               "instrument": "XAU",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName2"
 *                    }
 *                  },
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "USOIL",
 *                               "instrument": "OIL",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName3"
 *                    }
 *                  }
 *                  ]
 *              }
 *     }
 *     {
 *         "sig":1,
 *         "sid":1
 *     }
 *
 * @apiSuccessExample Success-Response (RS):
 *     {
 *       "data":{
 *                "entries": [
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "Bitcoin",
 *                               "instrument": "BTC",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName"
 *                    }
 *                  },
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "Gold",
 *                               "instrument": "XAU",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName2"
 *                    }
 *                  },
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "USOIL",
 *                               "instrument": "OIL",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName3"
 *                    }
 *                  }
 *                  ]
 *              },
 *       "metadata":{
 *          "q": "/io.scalecube.configuration.api.ConfigurationService/entries"
 *       }
 *     }
 *
 * @apiSuccessExample Success-Response (HTTP):
 *     {
 *       "entries": [
 *         {
 *           "value": {
 *                      "instrumentInstance": {
 *                      "name": "Bitcoin",
 *                      "instrument": "BTC",
 *                      "DecimalPrecision" : "2",
 *                      "Rounding": "down"
 *           },
 *           "key": "specifiedKeyName"
 *           }
 *         },
 *         {
 *           "value": {
 *                      "instrumentInstance": {
 *                      "name": "Gold",
 *                      "instrument": "XAU",
 *                      "DecimalPrecision" : "2",
 *                      "Rounding": "down"
 *           },
 *           "key": "specifiedKeyName2"
 *           }
 *         },
 *         {
 *           "value": {
 *                      "instrumentInstance": {
 *                      "name": "USOIL",
 *                      "instrument": "OIL",
 *                      "DecimalPrecision" : "2",
 *                      "Rounding": "down"
 *           },
 *           "key": "specifiedKeyName3"
 *           }
 *         }
 *         ]
 *    }
 *
 * @apiError {String} field invalid API key (token)
 * @apiErrorExample {json} Error-Response (WS):
 *    {
 *         "sig":2,
 *         "q":"/io.scalecube.services.error/500",
 *         "sid":1,
 *         "d":{
 *                 errorCode":500,
 *                 "errorMessage":"Token verification failed"
 *         }
 *    }
 *
 * @apiErrorExample {json} Error-Response (RS):
 *    {
 *         "data":{
 *         "errorCode":500,
 *         "errorMessage":"Token verification failed"
 *         },
 *         "metadata":{
 *             "q":"/io.scalecube.services.error/500"
 *         }
 *    }
 *
 * @apiErrorExample {json} Error-Response (HTTP):
 *    {
 *       errorCode":500,
 *       "errorMessage":"Token verification failed"
 *    }
 */
/**
 * @apiDefine BadRequestError
 * @apiVersion 2.0.18-SNAPSHOT
 *
 * @apiError BadRequestError The request didn't pass validation
 *
 * @apiErrorExample Error-Response:
 * {
 *   "errorCode": "400"
 *   "errorMessage": "Bad request."
 * }
 */

/**
 * @apiDefine InternalServerError
 *
 * @apiError (Error 500) InternalServerError Error happened during request processing
 *
 * @apiErrorExample Error-Response:
 * {
 *   "errorCode": "500"
 *   "errorMessage": "Error message"
 * }
 */

/**
 * @apiDefine ServiceUnavailableError
 *
 * @apiError (Error 503) ServiceUnavailableError Service in not available to accept requests
 *
 * @apiErrorExample Error-Response:
 * {
 *   "errorCode": "503"
 *   "errorMessage": "No reachable member with such service: %s"
 * }
 */
/**
 * @api {endpoint / delete} /io.scalecube.configuration.api.ConfigurationService/delete Delete specified entry
 * @apiName Delete
 * @apiGroup Delete
 * @apiVersion 2.0.18-SNAPSHOT
 * @apiDescription This operation enable you to delete a specified entry (key) from the relevant Repository and requires a write level permission granted for owner either admin role only
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name for relevant configuration setting in the repository
 *
 * @apiSuccess Acknowledgment Empty object
 *
 * @apiParamExample {json} Request-Example (WS):
 *     {
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/delete",
 *         "sid": 1,
 *         "d":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (RS):
 *     {
 *       "metadata":{
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/delete"
 *         },
 *         "data":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (HTTP):
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *     }
 *
 * @apiSuccessExample Success-Response (WS):
 *     {
 *         "q":"/io.scalecube.configuration.api.ConfigurationService/delete",
 *         "sid":1,
 *         "d":{}
 *     }
 *
 *     {
 *          "sig":1,
 *          "sid":1
 *     }
 *
 * @apiSuccessExample Success-Response (RS):
 *     {
 *         "data":{},
 *         "metadata":{
 *           "q": "/io.scalecube.configuration.api.ConfigurationService/delete"
 *         }
 *     }
 *
 * @apiSuccessExample Success-Response (HTTP):
 *     {
 *     }
 *
 * @apiError {String} field invalid permission level for specified API key
 *
 * @apiErrorExample {json} Error-Response (WS):
 *     {
 *          "sig":2,
 *          "q":"/io.scalecube.services.error/500",
 *          "sid":1,
 *          "d":{
 *                  errorCode":500,
 *                  "errorMessage":"ole 'Member' has insufficient permissions for the requested operation: Delete"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (RS):
 *     {
 *          "data":{
 *          "errorCode":500,
 *          "errorMessage":"ole 'Member' has insufficient permissions for the requested operation: Delete"
 *          },
 *          "metadata":{
 *              "q":"/io.scalecube.services.error/500"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (HTTP):
 *     {
 *        errorCode":500,
 *        "errorMessage":"ole 'Member' has insufficient permissions for the requested operation: Delete"
 *     }
 *
 *//**
 * @api {endpoint / save} /io.scalecube.configuration.api.ConfigurationService/save SaveUpdate the entry
 * @apiName Save
 * @apiGroup Save
 * @apiVersion 2.0.18-SNAPSHOT
 * @apiDescription This operation enable you to save either to update (edit) a specified entry in the relevant Repository and requires a write level permission granted for owner either admin role only.
 * Upon the specified entry is saved then it could be updated i.e. overwritten by the common method
 *
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name for relevant configuration setting in the repository
 * @apiParam {JsonNode} value Specified node name applied for relevant configuration settings in the repository
 *
 * @apiSuccess Acknowledgment Empty object
 *
 * @apiParamExample {json} Request-Example:
 *     {
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/save",
 *         "sid": 1,
 *         "d":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName",
 *         "value": {
 *                     "name": "Gold",
 *                     "instrument": "XAU",
 *                     "DecimalPrecision" : "2",
 *                     "Rounding": "down"
 *                  }
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (RS):
 *     {
 *       "metadata":{
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/save"
 *         },
 *         "data":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName",
 *         "value": {
 *                     "name": "Gold",
 *                     "instrument": "XAU",
 *                     "DecimalPrecision" : "2",
 *                     "Rounding": "down"
 *                  }
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (HTTP):
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName",
 *         "value": {
 *                     "name": "Gold",
 *                     "instrument": "XAU",
 *                     "DecimalPrecision" : "2",
 *                     "Rounding": "down"
 *                  }
 *     }
 *
 * @apiSuccessExample Success-Response (WS):
 *     {
 *         "q":"/io.scalecube.configuration.api.ConfigurationService/save",
 *         "sid":1,
 *         "d":{}
 *     }
 *
 *     {
 *          "sig":1,
 *          "sid":1
 *     }
 *
 * @apiSuccessExample Success-Response (RS):
 *     {
 *         "data":{},
 *         "metadata":{
 *           "q": "/io.scalecube.configuration.api.ConfigurationService/save"
 *         }
 *     }
 *
 * @apiSuccessExample Success-Response (HTTP):
 *     {
 *     }
 *
 * @apiError {String} field invalid or non-existent repository name
 *
 * @apiErrorExample {json} Error-Response (WS):
 *     {
 *          "sig":2,
 *          "q":"/io.scalecube.services.error/500",
 *          "sid":1,
 *          "d":{
 *                  errorCode":500,
 *                  "errorMessage":"Failed to open bucket: 'ORG-ACF8F702CE82DE56D737-Name"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (RS):
 *     {
 *          "data":{
 *          "errorCode":500,
 *          "errorMessage":"Failed to open bucket: 'ORG-ACF8F702CE82DE56D737-Name"
 *          },
 *          "metadata":{
 *              "q":"/io.scalecube.services.error/500"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (HTTP):
 *     {
 *        errorCode":500,
 *        "errorMessage":"Failed to open bucket: 'ORG-ACF8F702CE82DE56D737-Name"
 *     }
 */
/**
* @api {Host address} / Getting Started
* @apiName GettingStarted
* @apiGroup Overview
* @apiVersion 2.0.18-SNAPSHOT
* @apiDescription Configuration service enable you to integrate the API in order to create and manage the separate
* repositories purposed for entries collection and storage.
*
*
* <b>Getting Started</b>
*
*
* All API endpoints are documented below. You can try out any query in realtime using our interactive API.
* <br>Configuration service is a provider for <a href="http://scalecube.io/organization-service/index.html">Organization service</a> management.
* Actually all methods require <a href="http://scalecube.io/organization-service/index.html#api-ApiKey-AddOrganizationApiKey">API key</a> authorization since
* they provide a specific permission level (write or read ) for the each user. So there is a necessity to get the API key
* (token - assigned with relevant role Member/Admin/Owner) which is basically purposed for organization managers who provide such kind permission level to potential costumers.
* <br>Thus, we recommend you first to be granted with valid API key assigned with relevant role (permission level) to be able to make valid requests across all service <b>endpoints</b>.
*
* <b>Validation</b> for the object entities is handled by <b>Scalecube</b> services and do the next upon the request object:
* >~ ignores any excessive keys and values added besides the required parameters
* ><br>~ doesn't ignore the keys duplicates and takes the last values which applied for each of the relevant key duplicate
*
* >Contracts validation is implemented for specific parameters which value type is string and can only contain characters
* in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent. Appropriate validation will be added soon.
*/

/**
* @api {Host address} / Transport protocols API
* @apiName TransportProtocols
* @apiGroup Overview
* @apiVersion 2.0.18-SNAPSHOT
* @apiDescription Upon relevant <b>host address</b> was set the <b>request</b> should contain the following structure according to transport protocol usage:
                    <ul>
								   <b>Websocket (WS)</b><a href="https://github.com/scalecube/scalecube-services/wiki/Web-Socket-API"> API</a>
                                    <li> relevant host address </li>
                                    <li> "q": The query of the relevant service name and method used </li>
                                    <li> "sid": The stream identifier (couldn't be duplicated in the current stream connection)</li>
                                    <li> "d": The request data object (keys and values) </li>
								   <b>RSocket (RS)</b><a href="https://github.com/scalecube/scalecube-services/wiki/Rsocket-API"> API</a>
								     <li> relevant host address </li>
								     <li> "metadata": object which contains "q": The query of the relevant service name and method used </li>
                                    <li> "d": object: The request data (parameters and values) </li>
								   <b>HTTP</b>
								     <li>  host address with relevant service name and method used </li>
								     <li> "headers": Content-Type application/json </li>
                                    <li> "body" json: The request data object (parameters and values) </li>
                                    <li>  request "method": POST </li>
                    </ul>

* @apiParamExample {json} Request-Example (WS):
                    {
                        "q": "/io.scalecube.configuration.api.ConfigurationService/method_name",
                        "sid": 1,
                        "d": {
                                "relevant request parameters and values"
                             }
                    }

* @apiParamExample {json} Request-Example (RS):
                    {
                         "metadata": {
                             "q": "/io.scalecube.configuration.api.ConfigurationService/method_name"
                                     },
                             "data": {
                                        "relevant request parameters and values"
                                     }
                    }

* @apiParamExample {json} Request-Example (HTTP):
                    {
                            "relevant request parameters and values"
                    }

*/

/**
  * @api {Host address} / Interactive API Explorer
  * @apiName InteractiveAPIExplorer
  * @apiGroup Overview
  * @apiVersion 2.0.18-SNAPSHOT
  * @apiDescription For example <b>Websocket</b> connection is one of the accessible ways to use the API, so please follow the steps below to connect and run commands via Sandbox:
  <ul>
                 <li> Navigate to the sandbox: <a href="http://scalecube.io/api-sandbox/app/index.html">Scalecube sandbox</a> </li>
                 <li> Click on the <b>Settings</b> button and choose the relevant <b>transort</b> and host <b>address</b>: wss://configuration-service-7070.genesis.om2.com </li>
                 <li> Click on <b>Import icon</b> and copy-paste the exchange.json file path <a href="https://raw.githubusercontent.com/scalecube/scalecube-configuration-service/master/API-Calls-examples.json">Configuration service endpoints.json</a></li>
                 <li> Click on the <b>Connect</b> button (now you are connected to the environment) and push <b>Send</b> button to make your request</li>
  </ul>
*//**
 * @api {endpoint / fetch} /io.scalecube.configuration.api.ConfigurationService/fetch Fetch (get) specified entry
 * @apiName Fetch
 * @apiGroup Fetch
 * @apiVersion 2.0.19-SNAPSHOT
 * @apiDescription This operation enable you to get specific value (either string or object) by retrieving specified entry (key) from the relevant Repository and enabled for each accessible permission level (read&write) granted for owner either admin or member role
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name (entry) for relevant configuration setting in the repository
 *
 * @apiSuccess {Object} value Specified node name applied for relevant configuration settings in the repository
 * @apiSuccess {String} key Specified key name (entry) for relevant configuration setting in the repository
 *
 * @apiParamExample {json} Request-Example (WS):
 *     {
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/fetch",
 *         "sid": 1,
 *         "d":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (RS):
 *     {
 *       "metadata":{
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/fetch"
 *         },
 *         "data":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (HTTP):
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *     }
 *
 * @apiSuccessExample Success-Response (WS):
 *     {
 *         "q":"/io.scalecube.configuration.api.ConfigurationService/fetch",
 *         "sid":1,
 *         "d":{
 *         "value": {
 *                    "name": "Gold",
 *                    "instrument": "XAU",
 *                    "DecimalPrecision" : "2",
 *                    "Rounding": "down"
 *                  },
 *         "key": "specifiedKeyName"
 *         }
 *     }
 *
 *     {
 *          "sig":1,
 *          "sid":1
 *     }
 *
 * @apiSuccessExample Success-Response (RS):
 *    {
 *        "data":{
 *        "value": {
 *                   "name": "Gold",
 *                   "instrument": "XAU",
 *                   "DecimalPrecision" : "2",
 *                   "Rounding": "down"
 *                 },
 *        "key": "specifiedKeyName"
 *       },
 *       "metadata":{
 *          "q": "/io.scalecube.configuration.api.ConfigurationService/fetch"
 *       }
 *    }
 *
 * @apiSuccessExample Success-Response (HTTP):
 *    {
 *        "value": {
 *                   "name": "Gold",
 *                   "instrument": "XAU",
 *                   "DecimalPrecision" : "2",
 *                   "Rounding": "down"
 *                 },
 *        "key": "specifiedKeyName"
 *    }
 *
 * @apiError {String} field invalid or non-existent key name (entry in the repository)
 *
 * @apiErrorExample {json} Error-Response (WS):
 *    {
 *         "sig":2,
 *         "q":"/io.scalecube.services.error/500",
 *         "sid":1,
 *         "d":{
 *                 errorCode":500,
 *                 "errorMessage":"16562665EC17CDF08E97"
 *         }
 *    }
 *
 * @apiErrorExample {json} Error-Response (RS):
 *    {
 *         "data":{
 *         "errorCode":500,
 *         "errorMessage":"16562665EC17CDF08E97"
 *         },
 *         "metadata":{
 *             "q":"/io.scalecube.services.error/500"
 *         }
 *    }
 *
 * @apiErrorExample {json} Error-Response (HTTP):
 *    {
 *       errorCode":500,
 *       "errorMessage":"16562665EC17CDF08E97"
 *    }
 */
{
  "name": "Configuration service",
  "version": "2.0.19-SNAPSHOT",
  "description": "Configuration service API",
  "title": "API documentation for the Configuration Service",
  "url" : "Websocket: wss://configuration-service-7070.genesis.om2.com/\nRSocket: wss://configuration-service-9090.genesis.om2.com/\nHTTP: https://configuration-service-8080.genesis.om2.com",
  "template": {
    "forceLanguage" : "en"
  },
  "order": [
    "Overview",
    "GettingStarted",
    "TransportProtocols",
    "InteractiveAPIExplorer",
    "CreateRepository",
    "Save",
    "Fetch",
    "Entries",
    "Delete"
  ]
}
/**
 * @api {endpoint / createRepository} /io.scalecube.configuration.api.ConfigurationService/createRepository Create new Repo
 * @apiName CreateRepository
 * @apiGroup CreateRepository
 * @apiVersion 2.0.19-SNAPSHOT
 * @apiDescription This operation enable you to create the specified Repository for collecting and storing the relevant entries and requires a write permission level granted for owner role only
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 *
 * @apiSuccess Acknowledgment Empty object
 *
 * @apiParamExample {json} Request-Example (WS):
 *     {
 *         "q":"/io.scalecube.configuration.api.ConfigurationService/createRepository",
 *         "sid": 1,
 *         "d":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (RS):
 *     {
 *       "metadata":{
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/createRepository"
 *         },
 *         "data":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (HTTP):
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *     }
 *
 *
 * @apiSuccessExample Success-Response (WS):
 *     {
 *         "q":"/io.scalecube.configuration.api.ConfigurationService/createRepository",
 *         "sid":1,
 *         "d":{}
 *     }
 *
 *     {
 *          "sig":1,
 *          "sid":1
 *     }
 *
 * @apiSuccessExample Success-Response (RS):
 *     {
 *         "data":{},
 *         "metadata":{
 *           "q": "/io.scalecube.configuration.api.ConfigurationService/createRepository"
 *         }
 *     }
 *
 * @apiSuccessExample Success-Response (HTTP):
 *     {
 *     }
 *
 * @apiError {String} field invalid permission level for specified API key
 *
 * @apiErrorExample {json} Error-Response (WS):
 *     {
 *          "sig":2,
 *          "q":"/io.scalecube.services.error/500",
 *          "sid":1,
 *          "d":{
 *                  errorCode":500,
 *                  "errorMessage":"Role 'Admin' has insufficient permissions for the requested operation: CreateRepoitory"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (RS):
 *     {
 *          "data":{
 *          "errorCode":500,
 *          "errorMessage":"Role 'Member' has insufficient permissions for the requested operation: CreateRepoitory"
 *          },
 *          "metadata":{
 *              "q":"/io.scalecube.services.error/500"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (HTTP):
 *     {
 *        errorCode":500,
 *        "errorMessage":"Role 'Admin' has insufficient permissions for the requested operation: CreateRepoitory"
 *     }
 */
/**
 * @api {endpoint / entries} /io.scalecube.configuration.api.ConfigurationService/entries Fetch (get) all entries
 * @apiName Entries
 * @apiGroup Entries
 * @apiVersion 2.0.19-SNAPSHOT
 * @apiDescription This operation enable you to get all values (array of objects) by retrieving all the entries (keys) from the relevant Repository and enabled for each accessible permission level (read&write) granted for owner either admin or member role
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 *
 * @apiSuccess {Object[]} entries List of all entries from the relevant configuration setting in the repository (Array of Objects)
 * @apiSuccess {Object} value.entries Specified node name applied for relevant configuration settings in the repository
 * @apiSuccess {String} key.entries Specified key name (entry) for relevant configuration setting in the repository
 *
 * @apiParamExample {json} Request-Example (WS):
 *     {
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/entries",
 *         "sid": 1,
 *         "d":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (RS):
 *     {
 *       "metadata":{
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/entries"
 *         },
 *         "data":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (HTTP):
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *     }
 *
 * @apiSuccessExample Success-Response (WS):
 *     {
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/entries",
 *         "sid": 1,
 *         "d":{
 *                "entries": [
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "Bitcoin",
 *                               "instrument": "BTC",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName"
 *                    }
 *                  },
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "Gold",
 *                               "instrument": "XAU",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName2"
 *                    }
 *                  },
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "USOIL",
 *                               "instrument": "OIL",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName3"
 *                    }
 *                  }
 *                  ]
 *              }
 *     }
 *     {
 *         "sig":1,
 *         "sid":1
 *     }
 *
 * @apiSuccessExample Success-Response (RS):
 *     {
 *       "data":{
 *                "entries": [
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "Bitcoin",
 *                               "instrument": "BTC",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName"
 *                    }
 *                  },
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "Gold",
 *                               "instrument": "XAU",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName2"
 *                    }
 *                  },
 *                  {
 *                    "value": {
 *                               "instrumentInstance": {
 *                               "name": "USOIL",
 *                               "instrument": "OIL",
 *                               "DecimalPrecision" : "2",
 *                               "Rounding": "down"
 *                    },
 *                    "key": "specifiedKeyName3"
 *                    }
 *                  }
 *                  ]
 *              },
 *       "metadata":{
 *          "q": "/io.scalecube.configuration.api.ConfigurationService/entries"
 *       }
 *     }
 *
 * @apiSuccessExample Success-Response (HTTP):
 *     {
 *       "entries": [
 *         {
 *           "value": {
 *                      "instrumentInstance": {
 *                      "name": "Bitcoin",
 *                      "instrument": "BTC",
 *                      "DecimalPrecision" : "2",
 *                      "Rounding": "down"
 *           },
 *           "key": "specifiedKeyName"
 *           }
 *         },
 *         {
 *           "value": {
 *                      "instrumentInstance": {
 *                      "name": "Gold",
 *                      "instrument": "XAU",
 *                      "DecimalPrecision" : "2",
 *                      "Rounding": "down"
 *           },
 *           "key": "specifiedKeyName2"
 *           }
 *         },
 *         {
 *           "value": {
 *                      "instrumentInstance": {
 *                      "name": "USOIL",
 *                      "instrument": "OIL",
 *                      "DecimalPrecision" : "2",
 *                      "Rounding": "down"
 *           },
 *           "key": "specifiedKeyName3"
 *           }
 *         }
 *         ]
 *    }
 *
 * @apiError {String} field invalid API key (token)
 * @apiErrorExample {json} Error-Response (WS):
 *    {
 *         "sig":2,
 *         "q":"/io.scalecube.services.error/500",
 *         "sid":1,
 *         "d":{
 *                 errorCode":500,
 *                 "errorMessage":"Token verification failed"
 *         }
 *    }
 *
 * @apiErrorExample {json} Error-Response (RS):
 *    {
 *         "data":{
 *         "errorCode":500,
 *         "errorMessage":"Token verification failed"
 *         },
 *         "metadata":{
 *             "q":"/io.scalecube.services.error/500"
 *         }
 *    }
 *
 * @apiErrorExample {json} Error-Response (HTTP):
 *    {
 *       errorCode":500,
 *       "errorMessage":"Token verification failed"
 *    }
 */
/**
 * @apiDefine BadRequestError
 * @apiVersion 2.0.19-SNAPSHOT
 *
 * @apiError BadRequestError The request didn't pass validation
 *
 * @apiErrorExample Error-Response:
 * {
 *   "errorCode": "400"
 *   "errorMessage": "Bad request."
 * }
 */

/**
 * @apiDefine InternalServerError
 *
 * @apiError (Error 500) InternalServerError Error happened during request processing
 *
 * @apiErrorExample Error-Response:
 * {
 *   "errorCode": "500"
 *   "errorMessage": "Error message"
 * }
 */

/**
 * @apiDefine ServiceUnavailableError
 *
 * @apiError (Error 503) ServiceUnavailableError Service in not available to accept requests
 *
 * @apiErrorExample Error-Response:
 * {
 *   "errorCode": "503"
 *   "errorMessage": "No reachable member with such service: %s"
 * }
 */
/**
 * @api {endpoint / delete} /io.scalecube.configuration.api.ConfigurationService/delete Delete specified entry
 * @apiName Delete
 * @apiGroup Delete
 * @apiVersion 2.0.19-SNAPSHOT
 * @apiDescription This operation enable you to delete a specified entry (key) from the relevant Repository and requires a write level permission granted for owner either admin role only
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name (entry) for relevant configuration setting in the repository
 *
 * @apiSuccess Acknowledgment Empty object
 *
 * @apiParamExample {json} Request-Example (WS):
 *     {
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/delete",
 *         "sid": 1,
 *         "d":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (RS):
 *     {
 *       "metadata":{
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/delete"
 *         },
 *         "data":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (HTTP):
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName"
 *     }
 *
 * @apiSuccessExample Success-Response (WS):
 *     {
 *         "q":"/io.scalecube.configuration.api.ConfigurationService/delete",
 *         "sid":1,
 *         "d":{}
 *     }
 *
 *     {
 *          "sig":1,
 *          "sid":1
 *     }
 *
 * @apiSuccessExample Success-Response (RS):
 *     {
 *         "data":{},
 *         "metadata":{
 *           "q": "/io.scalecube.configuration.api.ConfigurationService/delete"
 *         }
 *     }
 *
 * @apiSuccessExample Success-Response (HTTP):
 *     {
 *     }
 *
 * @apiError {String} field invalid permission level for specified API key
 *
 * @apiErrorExample {json} Error-Response (WS):
 *     {
 *          "sig":2,
 *          "q":"/io.scalecube.services.error/500",
 *          "sid":1,
 *          "d":{
 *                  errorCode":500,
 *                  "errorMessage":"ole 'Member' has insufficient permissions for the requested operation: Delete"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (RS):
 *     {
 *          "data":{
 *          "errorCode":500,
 *          "errorMessage":"ole 'Member' has insufficient permissions for the requested operation: Delete"
 *          },
 *          "metadata":{
 *              "q":"/io.scalecube.services.error/500"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (HTTP):
 *     {
 *        errorCode":500,
 *        "errorMessage":"ole 'Member' has insufficient permissions for the requested operation: Delete"
 *     }
 *
 *//**
 * @api {endpoint / save} /io.scalecube.configuration.api.ConfigurationService/save Save and update the entry
 * @apiName Save
 * @apiGroup Save
 * @apiVersion 2.0.19-SNAPSHOT
 * @apiDescription This operation enable you to save either to update (edit) a specified entry in the relevant Repository and requires a write level permission granted for owner either admin role only.
 * Upon the specified entry is saved then it could be updated i.e. overwritten by the common method
 *
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name (entry) for relevant configuration setting in the repository
 * @apiParam {JsonNode} value Specified node (entry) name applied for relevant configuration settings in the repository
 *
 * @apiSuccess Acknowledgment Empty object
 *
 * @apiParamExample {json} Request-Example:
 *     {
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/save",
 *         "sid": 1,
 *         "d":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName",
 *         "value": {
 *                     "name": "Gold",
 *                     "instrument": "XAU",
 *                     "DecimalPrecision" : "2",
 *                     "Rounding": "down"
 *                  }
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (RS):
 *     {
 *       "metadata":{
 *         "q": "/io.scalecube.configuration.api.ConfigurationService/save"
 *         },
 *         "data":{
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName",
 *         "value": {
 *                     "name": "Gold",
 *                     "instrument": "XAU",
 *                     "DecimalPrecision" : "2",
 *                     "Rounding": "down"
 *                  }
 *         }
 *     }
 *
 * @apiParamExample {json} Request-Example (HTTP):
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName",
 *         "key": "specifiedKeyName",
 *         "value": {
 *                     "name": "Gold",
 *                     "instrument": "XAU",
 *                     "DecimalPrecision" : "2",
 *                     "Rounding": "down"
 *                  }
 *     }
 *
 * @apiSuccessExample Success-Response (WS):
 *     {
 *         "q":"/io.scalecube.configuration.api.ConfigurationService/save",
 *         "sid":1,
 *         "d":{}
 *     }
 *
 *     {
 *          "sig":1,
 *          "sid":1
 *     }
 *
 * @apiSuccessExample Success-Response (RS):
 *     {
 *         "data":{},
 *         "metadata":{
 *           "q": "/io.scalecube.configuration.api.ConfigurationService/save"
 *         }
 *     }
 *
 * @apiSuccessExample Success-Response (HTTP):
 *     {
 *     }
 *
 * @apiError {String} field invalid or non-existent repository name
 *
 * @apiErrorExample {json} Error-Response (WS):
 *     {
 *          "sig":2,
 *          "q":"/io.scalecube.services.error/500",
 *          "sid":1,
 *          "d":{
 *                  errorCode":500,
 *                  "errorMessage":"Failed to open bucket: 'ORG-ACF8F702CE82DE56D737-Name"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (RS):
 *     {
 *          "data":{
 *          "errorCode":500,
 *          "errorMessage":"Failed to open bucket: 'ORG-ACF8F702CE82DE56D737-Name"
 *          },
 *          "metadata":{
 *              "q":"/io.scalecube.services.error/500"
 *          }
 *     }
 *
 * @apiErrorExample {json} Error-Response (HTTP):
 *     {
 *        errorCode":500,
 *        "errorMessage":"Failed to open bucket: 'ORG-ACF8F702CE82DE56D737-Name"
 *     }
 */
/**
* @api {Host address} / Getting Started
* @apiName GettingStarted
* @apiGroup Overview
* @apiVersion 2.0.19-SNAPSHOT
* @apiDescription Configuration service enable you to integrate the API in order to create and manage the separate
* repositories purposed for entries collection and storage.
*
*
* <b>Getting Started</b>
*
*
* All API endpoints are documented below. You can try out any query in realtime using our interactive API.
* <br>Configuration service is a provider for <a href="http://scalecube.io/organization-service/index.html">Organization service</a> management.
* Actually all methods require <a href="http://scalecube.io/organization-service/index.html#api-ApiKey-AddOrganizationApiKey">API key</a> authorization since
* they provide a specific permission level (write or read ) for the each user. So there is a necessity to get the API key
* (token - assigned with relevant role Member/Admin/Owner) which is basically purposed for organization managers who provide such kind permission level to potential costumers.
* <br>Thus, we recommend you first to be granted with valid API key assigned with relevant role (permission level) to be able to make valid requests across all service <b>endpoints</b>.
*
* <b>Validation</b> for the object entities is handled by <b>Scalecube</b> services and do the next upon the request object:
* >~ ignores any excessive keys and values added besides the required parameters
* ><br>~ doesn't ignore the keys duplicates and takes the last values which applied for each of the relevant key duplicate
*
* >Contracts validation is implemented for specific parameters which value type is string and can only contain characters
* in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent. Appropriate validation will be added soon.
*/

/**
* @api {Host address} / Transport protocols API
* @apiName TransportProtocols
* @apiGroup Overview
* @apiVersion 2.0.19-SNAPSHOT
* @apiDescription Upon relevant <b>host address</b> was set the <b>request</b> should contain the following structure according to transport protocol usage:
                    <ul>
								   <b>Websocket (WS)</b><a href="https://github.com/scalecube/scalecube-services/wiki/Web-Socket-API"> API</a>
                                    <li> "q": The query of the relevant service name and method used </li>
                                    <li> "sid": The stream identifier (couldn't be duplicated in the current stream connection)</li>
                                    <li> "d": The request data object (keys and values) </li>
								   <b>RSocket (RS)</b><a href="https://github.com/scalecube/scalecube-services/wiki/Rsocket-API"> API</a>
								     <li> "metadata": object which contains "q": The query of the relevant service name and method used </li>
                                    <li> "d": object: The request data (parameters and values) </li>
								   <b>HTTP</b>
								     <li>  endpoint "url": host address with relevant service name and method used </li>
								     <li> "headers": Content-Type application/json </li>
                                    <li> "body" json: The request data object (parameters and values) </li>
                                    <li>  request "method": POST </li>
                    </ul>

* @apiParamExample {json} Request-Example (WS):
                    {
                        "q": "/io.scalecube.configuration.api.ConfigurationService/method_name",
                        "sid": 1,
                        "d": {
                                "relevant request parameters and values"
                             }
                    }

* @apiParamExample {json} Request-Example (RS):
                    {
                         "metadata": {
                             "q": "/io.scalecube.configuration.api.ConfigurationService/method_name"
                                     },
                             "data": {
                                        "relevant request parameters and values"
                                     }
                    }

* @apiParamExample {json} Request-Example (HTTP):
                    {
                            "relevant request parameters and values"
                    }

*/

/**
  * @api {Host address} / Interactive API Explorer
  * @apiName InteractiveAPIExplorer
  * @apiGroup Overview
  * @apiVersion 2.0.19-SNAPSHOT
  * @apiDescription For example <b>Websocket</b> connection is one of the accessible ways to use the API, so please follow the steps below to connect and run commands via Sandbox:
  <ul>
                 <li> Navigate to the sandbox: <a href="http://scalecube.io/api-sandbox/app/index.html">Scalecube sandbox</a> </li>
                 <li> Click on the <b>Settings</b> button and choose the relevant <b>transort</b> and host <b>address</b>: wss://configuration-service-7070.genesis.om2.com </li>
                 <li> Click on <b>Import icon</b> and copy-paste the template.json file path for <a href="https://raw.githubusercontent.com/scalecube/scalecube-configuration-service/master/API-Calls-examples.json">Configuration service endpoints.json</a></li>
                 <li> Click on the <b>Connect</b> button (now you are connected to the environment) and push <b>Send</b> button to make your request</li>
  </ul>
*/