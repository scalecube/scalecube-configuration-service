/**
  * @api {ServiceMethod: fetch} /configuration/fetch fetch
  * @apiName Fetch
  * @apiGroup Methods
  * @apiVersion 2.0.20-SNAPSHOT
  * @apiPermission Request / Response / Error-response
  *
  * @apiDescription This operation enable you to get specific value (either string or object) by retrieving specified entry (key)
  * from the relevant Repository and enabled for each accessible permission level (read&write) granted for owner either admin or member role
  *
  * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
  * @apiParam {String} repository Specified name of the repository
  * @apiParam {String} key Specified key name (entry) for relevant configuration setting in the repository
  *
  * @apiSuccess {Object} value Specified node name applied for relevant configuration settings in the repository
  * @apiSuccess {String} key Specified key name (entry) for relevant configuration setting in the repository
  *
  * @apiError {String} field invalid or non-existent plain key name (entry in the repository)
  *
  * @apiErrorExample {json} WebSocket:
  * Request:
  *     {
  *         "q":"/configuration/fetch",
  *         "sid": 1,
  *         "d":{
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName",
  *         "key": "specifiedKeyName"
  *         }
  *     }
  *
  * Response:
  *
  *     {
  *         "q":"/configuration/fetch",
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
  * Error Response:
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
  * @apiErrorExample {json} RSocket:
  * Request:
  *     {
  *       "metadata":{
  *         "q": "/configuration/fetch"
  *         },
  *         "data":{
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName",
  *         "key": "specifiedKeyName"
  *         }
  *     }
  *
  * Response:
  *
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
  *          "q": "/configuration/fetch"
  *       }
  *    }
  *
  * Error Response:
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
  * @apiErrorExample {json} HTTP:
  * Request:
  * https://localhost:port/configuration/fetch (endpoint url)
  *
  * Body:
  *     {
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName",
  *         "key": "specifiedKeyName"
  *     }
  *
  * Response:
  *
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
  * Error Response (HTTP):
  *    {
  *       errorCode":500,
  *       "errorMessage":"16562665EC17CDF08E97"
  *    }
  */{
  "name": "Configuration service",
  "version": "2.0.20-SNAPSHOT",
  "description": "Configuration service API",
  "title": "API documentation for the Configuration Service",
  "template": {
    "forceLanguage" : "en"
  },
  "order": [
    "Overview",
    "GettingStarted",
    "TransportProtocols",
    "InteractiveAPIExplorer",
    "Methods"
  ]
}
/**
  * @api {ServiceMethod: createRepository} /configuration/createRepository  createRepository
  * @apiName CreateRepository
  * @apiGroup Methods
  * @apiVersion 2.0.20-SNAPSHOT
  * @apiPermission Request / Response / Error-response
  *
  * @apiDescription This operation enable you to create the specified Repository for collecting and storing the relevant entries and requires a write permission level granted for owner role only
  *
  * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
  * @apiParam {String} repository Specified name of the repository
  *
  * @apiSuccess Acknowledgment Empty object
  *
  * @apiError {String} field invalid permission level for specified API key
  *
  * @apiErrorExample {json} WebSocket
  * Request:
  *     {
  *         "q":"/configuration/createRepository",
  *         "sid": 1,
  *         "d":{
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName"
  *         }
  *     }
  *
  * Response:
  *
  *     {
  *         "q":"/configuration/createRepository",
  *         "sid":1,
  *         "d":{}
  *     }
  *
  *     {
  *          "sig":1,
  *          "sid":1
  *     }
  *
  * Error Response:
  *
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
  *
  * @apiErrorExample {json} RSocket
  * Request:
  *     {
  *       "metadata":{
  *         "q": "/configuration/createRepository"
  *         },
  *         "data":{
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName"
  *         }
  *     }
  *
  * Response:
  *
  *     {
  *         "data":{},
  *         "metadata":{
  *           "q": "/configuration/createRepository"
  *         }
  *     }
  *
  * Error Response:
  *
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
  * @apiErrorExample{json} HTTP
  * Request:
  * https://localhost:port/configuration/createRepository (endpoint url)
  *
  * Body:
  *     {
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName"
  *     }
  *
  * Response:
  *     {
  *
  *     }
  *
  * Error Response:
  *
  *     {
  *        errorCode":500,
  *        "errorMessage":"Role 'Admin' has insufficient permissions for the requested operation: CreateRepoitory"
  *     }
  *//**
  * @api {ServiceMethod: entries} /configuration/entries entries
  * @apiName Entries
  * @apiGroup Methods
  * @apiVersion 2.0.20-SNAPSHOT
  * @apiPermission Request / Response / Error-response
  *
  * @apiDescription This operation enable you to get all values (array of objects) by retrieving all the entries (keys) from
  * the relevant Repository and enabled for each accessible permission level (read&write) granted for owner either admin or member role
  *
  * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
  * @apiParam {String} repository Specified name of the repository
  *
  * @apiSuccess {Object[]} entries List of all entries from the relevant configuration setting in the repository (Array of Objects)
  * @apiSuccess {Object} entries.value Specified node name applied for relevant configuration settings in the repository
  * @apiSuccess {String} entries.key Specified key name (entry) for relevant configuration setting in the repository
  *
  * @apiError {String} field invalid API key (token)
  *
  * @apiErrorExample {json} WebSocket
  * Request:
  *     {
  *         "q":"/configuration/entries",
  *         "sid": 1,
  *         "d":{
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName"
  *         }
  *     }
  *
  * Response:
  *
  *     {
  *         "q": "/configuration/entries",
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
  * Error Response:
  *     {
  *          "sig":2,
  *          "q":"/io.scalecube.services.error/500",
  *          "sid":1,
  *          "d":{
  *                  errorCode":500,
  *                  "errorMessage":"Token verification failed"
  *          }
  *     }
  *
  * @apiErrorExample {json} RSocket:
  * Request:
  *     {
  *       "metadata":{
  *         "q": "/configuration/entries"
  *         },
  *         "data":{
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName"
  *         }
  *     }
  *
  * Response:
  *
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
  *          "q": "/configuration/entries"
  *       }
  *     }
  *
  * Error Response:
  *     {
  *         "data":{
  *         "errorCode":500,
  *         "errorMessage":"Token verification failed"
  *         },
  *         "metadata":{
  *             "q":"/io.scalecube.services.error/500"
  *         }
  *     }
  *
  * @apiErrorExample {json} HTTP:
  * Request:
  * https://localhost:port/configuration/entries (endpoint url)
  *
  * Body:
  *     {
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName"
  *     }
  *
  * Response:
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
  * Error Response:
  *    {
  *       errorCode":500,
  *       "errorMessage":"Token verification failed"
  *    }
  *//**
 * @apiDefine BadRequestError
 * @apiVersion 2.0.20-SNAPSHOT
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
  * @api {ServiceMethod: delete} /configuration/delete delete
  * @apiName Delete
  * @apiGroup Methods
  * @apiVersion 2.0.20-SNAPSHOT
  * @apiPermission Request / Response / Error-response
  *
  * @apiDescription This operation enable you to delete a specified entry (key) from the relevant Repository and requires a write level permission granted for owner either admin role only
  *
  * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
  * @apiParam {String} repository Specified name of the repository
  * @apiParam {String} key Specified key name (entry) for relevant configuration setting in the repository
  *
  * @apiSuccess Acknowledgment Empty object
  *
  * @apiError {String} field invalid permission level for specified API key
  *
  * @apiErrorExample {json} WebSocket
  * Request:
  *     {
  *         "q":"/configuration/delete",
  *         "sid": 1,
  *         "d":{
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName",
  *         "key": "specifiedKeyName"
  *         }
  *     }
  *
  * Response:
  *
  *     {
  *         "q":"/configuration/delete",
  *         "sid":1,
  *         "d":{}
  *     }
  *
  *     {
  *          "sig":1,
  *          "sid":1
  *     }
  *
  * Error Response:
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
  * @apiErrorExample {json} RSocket
  * Request:
  *     {
  *       "metadata":{
  *         "q": "/configuration/delete"
  *         },
  *         "data":{
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName",
  *         "key": "specifiedKeyName"
  *         }
  *     }
  *
  * Response:
  *
  *     {
  *         "data":{},
  *         "metadata":{
  *           "q": "/configuration/delete"
  *         }
  *     }
  *
  * Error Response:
  *     {
  *          "data":{
  *          "errorCode":500,
  *          "errorMessage":"ole 'Member' has insufficient permissions for the requested operation: Delete"
  *          },
  *          "metadata":{
  *              "q":"/io.scalecube.services.error/500"
  *          }
  *     }
  * @apiErrorExample {json} HTTP
  * Request:
  * https://localhost:port/configuration/delete (endpoint url)
  *
  * Body:
  *     {
  *         "token": "API-TOKEN",
  *         "repository": "specifiedRepoName",
  *         "key": "specifiedKeyName"
  *     }
  *
  * Response:
  *     {
  *
  *     }
  *
  * Error Response:
  *     {
  *        errorCode":500,
  *        "errorMessage":"ole 'Member' has insufficient permissions for the requested operation: Delete"
  *     }
  *//**
  * @api {ServiceMethod: save} /configuration/save save
  * @apiName Save
  * @apiGroup Methods
  * @apiVersion 2.0.20-SNAPSHOT
  * @apiPermission Request / Response / Error-response
  *
  * @apiDescription This operation enable you to save either to update (edit) a specified entry in the relevant Repository
  * and requires a write level permission granted for owner either admin role only. Upon the specified entry is saved then it could be updated i.e. overwritten by the common method
  *
  * @apiParam {Object} token The requested API key (token) which assigned with relevant role (permission level)
  * @apiParam {String} repository Specified name of the repository
  * @apiParam {String} key Specified key name (entry) for relevant configuration setting in the repository
  * @apiParam {JsonNode} value Specified node (entry) name applied for relevant configuration settings in the repository
  *
  * @apiSuccess Acknowledgment Empty object
  *
  * @apiError {String} field invalid or non-existent repository name
  *
  *
  * @apiErrorExample {json} WebSocket:
  * Request:
  *
  *     {
  *         "q": "/configuration/save",
  *         "sid": 1,
  *         "d":{
  *         "token": "API-TOKEN",
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
  * Response:
  *     {
  *         "q":"/configuration/save",
  *         "sid":1,
  *         "d":{}
  *     }
  *
  *     {
  *          "sig":1,
  *          "sid":1
  *     }
  *
  * Error Response:
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
  * @apiErrorExample {json} RSocket:
  * Request:
  *     {
  *       "metadata":{
  *         "q": "/configuration/save"
  *         },
  *         "data":{
  *         "token": "API-TOKEN",
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
  * Response:
  *     {
  *         "data":{},
  *         "metadata":{
  *           "q": "/configuration/save"
  *         }
  *     }
  *
  * Error Response:
  *     {
  *         "data":{
  *         "errorCode":500,
  *         "errorMessage":"Failed to open bucket: 'ORG-ACF8F702CE82DE56D737-Name"
  *         },
  *         "metadata":{
  *             "q":"/io.scalecube.services.error/500"
  *         }
  *     }
  * @apiErrorExample {json} HTTP:
  * Request:
  *     {
  *         "token": "API-TOKEN",
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
  * Response:
  *     {
  *
  *     }
  *
  * Error-Response (HTTP):
  *     {
  *        errorCode":500,
  *        "errorMessage":"Failed to open bucket: 'ORG-ACF8F702CE82DE56D737-Name"
  *     }
  */
/**
  * @api  . Getting Started
  * @apiName GettingStarted
  * @apiGroup Overview
  * @apiVersion 2.0.20-SNAPSHOT
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
  * <br>Thus, we recommend you first to be granted with valid <b>API key</b> assigned with relevant role (permission level) to be able to make valid requests across all service <b>endpoints</b>.
  *
  * <b>Validation</b> for the object entities is handled by <b>Scalecube</b> services and do the next upon the request object:
  * >~ ignores any excessive keys and values added besides the required parameters
  * ><br>~ doesn't ignore the keys duplicates and takes the last values which applied for each of the relevant key duplicate
  *
  * >Contracts validation is implemented for specific parameters which value type is string and can only contain characters
  * in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent. Appropriate validation will be added soon.
  */

  /**
  * @api  . Transport protocols API
  * @apiName TransportProtocols
  * @apiGroup Overview
  * @apiVersion 2.0.20-SNAPSHOT
  * @apiPermission Successful requests and responses
  * @apiDescription You are able to manage the service API through the three types of transport protocols which are supported.
  * <br>Upon relevant <a href="https://github.com/jivygroup/exchange/wiki/Configuration-&-Organization-services-host-addresses"><b>Host address</b></a> was set the <b>request</b> should contain the following structure according to transport protocol usage:
                      <ul>
  						  <b>Websocket (WS)</b>
                             <li> "q": The query of the relevant service name and method used </li>
                             <li> "sid": The stream identifier (couldn't be reused upon current stream connection is opened)</li>
                             <li> "d": The request data object (keys and values) </li>
  						  <br><b>RSocket (RS)</b>
  						   <li> "metadata": object which contains "q": The query of the relevant service name and method used </li>
                             <li> "d": object: The request data (parameters and values) </li>
  						  <br><b>HTTP</b> (service name and method used should be added to the relevant host address)
  						   <li> "headers": Content-Type application/json </li>
                             <li> "body" json: The request data object (parameters and values) </li>
                             <li>  request "method": POST </li>
                      </ul>

  * @apiParamExample {json} WebSocket:
                      Request:
                      {
                          "q": "/serviceName/method_name",
                          "sid":int,
                          "d": {
                                  "relevant request parameters and values"
                               }
                      }

                      Response:

                      {
                          "q":"/serviceName/method_name",
                          "sid":int,
                          "d":{
                                 "relevant response parameters and values"
                              }
                      }
                      {
                          "sig":1,
                          "sid":int
                      }

  * @apiParamExample {json} RSocket:
                      Request:
                      {
                         "metadata": {
                             "q": "/serviceName/method_name"
                                     },
                             "data": {
                                        "relevant request parameters and values"
                                     }
                      }

                      Response:
                      {
                          "data":{
                                    "relevant response parameters and values"
                          },
                          "metadata":{
                            "q": "/serviceName/method_name"
                          }
                      }
  * @apiParamExample {json} HTTP:
                      Request:
                      https://localhost:port/serviceName/method_name (endpoint url)

                      Body:
                      {
                         "relevant request parameters and values"
                      }

                      Response:
                      {
                         "relevant response parameters and values"
                      }
  */

  /**
    * @api  . Interactive API Explorer
    * @apiName InteractiveAPIExplorer
    * @apiGroup Overview
    * @apiVersion 2.0.20-SNAPSHOT
    * @apiDescription <b>WebSocket and RSocket</b> transport are accessible to use the API via in-house developed API Explorer called <b>Sandbox</b> thus to connect and run follow up the next steps:
    <ul>
                   <li> Navigate to the sandbox: <a href="http://scalecube.io/api-sandbox/app/index.html">Scalecube sandbox</a> </li>
                   <li> Click on the <b>Settings</b> button then set the relevant <a href="https://github.com/jivygroup/exchange/wiki/Configuration-&-Organization-services-host-addresses"><b>Host address</b></a> for the chosen <b>transport</b> </li>
                   <li> Click on <b>Import icon</b> and copy-paste the template.json file path for <a href="https://raw.githubusercontent.com/scalecube/scalecube-configuration-service/master/API-Calls-examples.json">Configuration service endpoints.json</a></li>
                   <li> Click on the <b>Connect</b> button (now you are connected to the environment) and push <b>Send</b> button to make your request</li>
    </ul>
  */