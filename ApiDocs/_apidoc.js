/**
 * @apiDefine BadRequestError
 * @apiVersion 0.0.1-SNAPSHOT
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
 * @api {websocket} io.scalecube.configuration.api.ConfigurationService/createRepository Create new Repo
 * @apiVersion 0.0.1-SNAPSHOT
 * @apiName CreateRepository
 * @apiGroup CreateRepository
 *
 * @apiDescription This operation enable you to create the specified Repository for collecting and storing the relevant entities and requires a write level permission granted for owner role only
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
 *        "errorMessage":"role: "userId" not in role Owner"
 *     }
 */
/**
* @api {Transport protocols} / Summary
* @apiName Datatypes
* @apiVersion 0.0.1-SNAPSHOT
* @apiGroup Datatypes
* @apiDescription This chapter will detail datatypes that require specific format for or support a specific set of values.
<br> Currently implemented validation for specific parameters which value type is string and can only contain characters
in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent. Appropriate validation will be added soon.
*/
/**
 * @api {websocket} io.scalecube.configuration.api.ConfigurationService/delete Delete specified entry
 * @apiName Delete
 * @apiGroup Delete
 * @apiVersion 0.0.1-SNAPSHOT
 * @apiDescription This operation enable you to delete a specified entity from the relevant Repository and requires a write level permission granted for owner either admin role only
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name for relevant configuration setting in the repository
 *
 * @apiSuccess Acknowledgment Empty object
 *
 * @apiParamExample {json} Request-Example:
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
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
 *        "errorMessage":"userId" not in role Owner or Admin"
 *     }
 */
/**
 * @api {websocket} io.scalecube.configuration.api.ConfigurationService/entries Fetch (get) all entries
 * @apiName Entries
 * @apiGroup Entries
 * @apiVersion 0.0.1-SNAPSHOT
 * @apiDescription This operation enable you to get entry object for the all entities from the relevant Repository and enabled for each accessible permission level (read&write) granted for owner either admin or member role
 * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
 * @apiParam {String} repository Specified name of the repository
 * @apiParam {String} key Specified key name for relevant configuration setting in the repository
 *
 * @apiSuccess {Array} entries List of all entities from the relevant configuration setting in the repository
 * @apiSuccess {String} key Specified key name for relevant configuration setting in the repository
 * @apiSuccess {Object} value Specified node name applied for relevant configuration settings in the repository
 *
 * @apiParamExample {json} Request-Example:
 *     {
 *         "token": "eyJraWQiOiIyODQyYzFkYS0zMTFmLTRjNDcIUzI1NiJ9.JvbGUiOiJPd25lciIsImV4cCI6DAzOX0.RVhFs4WENT2_cR7Jy_1yB7YStO0d5V9Va43Q7lVqawR",
 *         "repository": "specifiedRepoName"
 *         "key": "specifiedKeyName"
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
 * @api {websocket} io.scalecube.configuration.api.ConfigurationService/fetch Fetch (get) specified entry
 * @apiName Fetch
 * @apiGroup Fetch
 * @apiVersion 0.0.1-SNAPSHOT
 * @apiDescription This operation enable you to get entry object for specified entity from the relevant Repository and enabled for each accessible permission level (read&write) granted for owner either admin or member role
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
 *         "repository": "specifiedRepoName"
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
 * @apiError {String} field invalid repository name
 * @apiErrorExample {json} Error-Response:
 *     {
 *        errorCode":500,
 *        "errorMessage":"repository: "specified" name doesn't exist"
 *     }
 */
/**
* @api {Transport protocols} / Getting Started
* @apiName GettingStarted
* @apiGroup Overview
* @apiVersion 0.0.1-SNAPSHOT
* @apiDescription Configuration service enable you to integrate the API in order to create and manage the separate repositories purposed for entity collection and storage.
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
* @api {API Gateway protocols} / General Definitions
* @apiName GeneralDefinitions
* @apiGroup Overview
* @apiVersion 0.0.1-SNAPSHOT
* @apiDescription The request should contain the following structure upon the transport protocols are used:
                    <ul>
								   <b>Websocket</b>
                                   <li> 1. "q": The query of the relevant service path </li>
                                   <li> 2. "sid": The identifier of the stream id. </li>
                                   <li> 3. "d": The request data (parameters and values). </li>
								   <b>RSocket</b>
								   <li> 1. "metadata" which contains object "q": The query of the relevant service path </li>
                                   <li> 2. "data" object: The request data (parameters and values). </li>
								   <b>HTTP</b>
								   <li> 1. "headers": Content-Type json </li>
                                   <li> 2. "body" json object: The request data (parameters and values). </li>
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
  * @apiVersion 0.0.1-SNAPSHOT
  * @apiDescription Please follow the steps below to connect and run commands via Sandbox:
  <ul>
                 <li> 1. Enter to the sandbox: <a href="http://scalecube.io/api-sandbox/app/index.html">Scalecube sandbox</a> </li>
                 <li> 2. Click on the Settings button and enter the environment: wss://configuration-service-7070.genesis.om2.com </li>
                 <li> 3. Import the exchange.json file path <a href="https://raw.githubusercontent.com/PavloPetrina/JsonData/master/OrganizationServiceALL.json">Organization service contracts.json</a></li>
                 <li> 4. Click on the Connect button (now you are connected to the environment) and push "Send"</li>
  </ul>
*/
/**
 * @api {websocket} io.scalecube.configuration.api.ConfigurationService/save SaveUpdate the entry
 * @apiName Save
 * @apiGroup Save
 * @apiVersion 0.0.1-SNAPSHOT
 * @apiDescription This operation enable you to save either update (edit) a specified entity in the relevant Repository and requires a write level permission granted for owner either admin role only.
 * Upon the specified entity is saved then it could be updated i.e. overwritten by the common method
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
 *         "repository": "specifiedRepoName"
 *         "key": "specifiedKeyName"
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
 * @apiError {String} field invalid key name
 * @apiErrorExample {json} Error-Response:
 *     {
 *        errorCode":500,
 *        "errorMessage":"key: "specified" name doesn't exist"
 *     }
 */
