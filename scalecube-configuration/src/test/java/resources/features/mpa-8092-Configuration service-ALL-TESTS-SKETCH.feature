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
















