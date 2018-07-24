# Permissions Service

The permissions service is responsible for handling a submissions for OGEL registrations to SPIRE. 
It also provides API's to view a users' OGEL registrations and information on ongoing submissions.

As part of the submission process the service will create a customer, create a site, update a user role where necessary 
and respond with a callback request which will detail the result of the submission processing.

# Running locally

* `git clone git@github.com:uktrade/lite-permissions-service.git`
* `cd lite-permissions-service` 
* `cp src/main/resources/sample-config.yaml src/main/resources/config.yaml`
* `./gradlew run`

# Endpoint summary

* `/ogel-registrations` (`OgelRegistrationResource`)

Gets all existing OGEL registration details for a given user. Requires JWT with `userId` matching the `subject`.
 
* `/ogel-submissions` (`OgelSubmissionResource`)
 
Administrative endpoints for viewing/cancelling OGEL submissions which are completed or in progress.

* `/register-ogel` (`RegisterOgelResource`)

Allows a user to register an OGEL and triggers the processing described below.

* `/licences` (`LicenceResource`)

Gets all licences for the supplied `userId`. Requires JWT with `userId` matching the `subject`. 

# JWT Authentication

Some of this services endpoints require authentication with a JWT token, information on the token can be found here [lite-dropwizard-common/jwt](https://github.com/uktrade/lite-dropwizard-common/tree/master/jwt). 
It's recommended to use `SpireLicenceUtil#tokenHelperTest()`, this generates a token which is valid for this service. 

# OGEL submission processing

The OgelSubmission is the entity which captures the submission processing state of any particular submission.

## OgelSubmission MODE
An OgelSubmission has 2 processing modes: `IMMEDIATE` and `SCHEDULED`.

`IMMEDIATE` mode correlates to a process tied to the initial request. An OgelSubmission's mode is updated to `SCHEDULED` 
for any subsequent processing if it cannot complete the processing in `IMMEDIATE` mode.

OgelSubmissions in `SCHEDULED` mode are processed via a repeating scheduled job (`ProcessScheduledJob`).

## OgelSubmission STAGE
The OgelSubmission is required to be processed through a number of stages - see `OgelSubmission.Stage`.

Each stage corresponds to an action that needs to be completed:

* `CUSTOMER`   - we need to create a Customer and populate customerId with resulting sarRef
* `SITE`       - we need to create a Site and populate siteId with resulting siteRef
* `USER_ROLE`  - we need to update user role permissions
* `OGEL`       - we need to create Ogel via Spire

## OgelSubmission STATUS
An OgelSubmission can have the 1 of the following statuses: `ACTIVE`, `COMPLETE`, `TERMINATED`

* An `ACTIVE` submission indicates the submission is currently being processed through the stages.
* A `COMPLETE` submission indicates the submission has finished being processed through the stages and the callback can now be attempted
* A `TERMINATED` submission indicates the submission has finished being processed and no callback should be attempted. A `TERMINATED` 
submission may have completed some stage processing before being terminated.


## OgelSubmission Callback
Every submission includes a callback URL which the PermissionsService uses to detail the result of the processing back to the client.
The PermissionService attempts the callback for any submission that is `COMPLETE` and has not been called back.
`TERMINATED` submissions are ignored for callbacks.

All the main processing logic can be found in the `ProcessSubmissionServiceImpl` service class.

## Submission Error Handling
Both the STAGE processing and the callbacks check for repeating errors during any part of the processing.

Configuration options are available to determine when to stop processing attempts:

* `maxMinutesRetryAfterFail` - how many minutes the submission should be left in the queue and retried after its initial failure
 (the total amount of retries will be determined by the retry job's frequency)
* `maxCallbackFailCount` - how many times to attempt a failing callback

## Termination

Any submission can be terminated at any stage through a dedicated API endpoint: `DELETE /ogel-submissions/{id}`

## Notes on the view API
When attempting to serialize or deserialize `LicenceView` with jackson-databind, you will need to enable the serialization 
format for `LocalDate`.
```java
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JavaTimeModule());
mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

String json = mapper.writeValueAsString(LocalDate.of(2000,12,31)); // "2000-12-31"
LocalDate localDate = mapper.readValue(json, LocalDate.class);
assertThat(localDate).isEqualTo("2000-12-31"); // true
```

