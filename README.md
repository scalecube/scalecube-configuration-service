# scalecube-configuration-service

scalecube configuration service enable clients to consume configuration data this can come in handy when the consumer needs to know how to connect to an 
external/3rd party services. For example, the database network location and credentials.

Another examle: A service must run in multiple environments - dev, test, qa, staging, production - without modification and/or recompilation
Different environments have different instances of the external/3rd party services, e.g. QA database vs. production database, test credit card 
processing account vs. production credit card processing account.

scalecube configuration service provides a solution by externalizing all application configuration (e.g. database credentials and network location). 
