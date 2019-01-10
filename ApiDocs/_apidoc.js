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
  "url" : "Websocket: wss://configuration-service-7070.genesis.om2.com/\nRSocket: wss://configuration-service-9090.genesis.om2.com/\nHTTP: https://configuration-service-8080.genesis.om2.com",
  "template": {
    "forceLanguage" : "en"
  },
  "order": [
    "Overview",
    "GettingStarted",
    "Datatypes",
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
    "General Definitions",
    "InteractiveAPIExplorer",
    "Datatypes",
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
/**
* @api {Transport protocols} / Summary
* @apiName Datatypes
* @apiVersion 2.0.17-SNAPSHOT
* @apiGroup Datatypes
* @apiDescription This chapter will detail datatypes that require specific format for or support a specific set of values.
<br> Currently the validation is implemented for specific parameters which value type is string and can only contain characters
in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent. Appropriate validation will be added soon.
*/
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
* @api {Host addresses} / General Definitions
* @apiName GeneralDefinitions
* @apiGroup Overview
* @apiVersion 2.0.17-SNAPSHOT
* @apiDescription The request should contain the following structure upon the transport protocols are used:
                    <ul>
								   <b>Websocket</b>
                                   <li> "q": The query of the relevant service path </li>
                                   <li> "sid": The identifier of the stream id. </li>
                                   <li> "d": The request data (parameters and values). </li>
								   <b>RSocket</b>
								   <li> "metadata" which contains object "q": The query of the relevant service path </li>
                                   <li> "data" object: The request data (parameters and values). </li>
								   <b>HTTP</b>
								   <li> add the "/io.scalecube.configuration.api.ServiceName/method_name" to the host </li>
								   <li> "headers": Content-Type json </li>
                                   <li> "body" json object: The request data (parameters and values). </li>
                    </ul>

* @apiParamExample {json} Request-Example Websocket:
                    {
                        "q": "/io.scalecube.configuration.api.ServiceName/method_name",
                        "sid": 1,
                        "d": {
                                "relevant request parameters and values"
                             }
                    }

* @apiParamExample {json} Request-Example RSocket:
                    {
                        "metadata": {
                            "q": "/io.scalecube.configuration.api.ServiceName/method_name",
                            }
                            "d": {
                                    "relevant request parameters and values"
                            }
                    }

* @apiParamExample {json} Request-Example HTTP:
                    {
                                    "relevant request parameters and values"
                    }

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
