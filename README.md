The Permissions Service
=======================

The PermissionsService is responsible for handling a submission (from the Ogel Registration Application) to register an Ogel.

The service will create a customer, create a site, update a user role where necessary 
and respond with a callback request detailing the result of it's processing.

An OgelSubmission
=================

The OgelSubmission is the entity which captures the submission processing state of any particular submission.

# OgelSubmission MODE
An OgelSubmission has 2 processing modes: IMMEDIATE, SCHEDULED

'IMMEDIATE' mode correlates to a process tied to the initial request. An OgelSubmission's mode is updated to 'SCHEDULED' 
for any subsequent processing if it cannot complete the processing in IMMEDIATE mode.

OgelSubmissions in SCHEDULED mode are processed via a repeating scheduled job.

# OgelSubmission STAGE
The OgelSubmission is required to be processed through a number of stages: CUSTOMER, SITE, USER_ROLE, OGEL

Each stage corresponds to an action that needs to be completed:

CUSTOMER   - we need to create a Customer and populate customerId with resulting sarRef

SITE       - we need to create a Site and populate siteId with resulting siteRef

USER_ROLE  - we need to update user role permissions

OGEL       - we need to create Ogel via Spire

# OgelSubmission STATUS
An OgelSubmission can have the 1 of the following status's: ACTIVE, COMPLETE, TERMINATED

An ACTIVE submission indicates the submission is currently being processed through the stages.

A COMPLETE submission indicates the submission has finished being processed through the stages and the callback can now be attempted

A TERMINATED submission indicates the submission has finished being processed and no callback should be attempted. A TERMINATED submission may have completed some stage processing before being terminated.


# OgelSubmission Callback
Every submission includes a callback URL which the PermissionsService uses to detail the result of the processing back to the client.
The PermissionService attempts the callback for any submission that is COMPLETE and has not been called back.
TERMINATED submissions are ignored for callbacks.

All the main processing logic can be found in the ProcessSubmissionServiceImpl service class.

# Submission Error Handling
Both the STAGE processing and the callbacks check for repeating errors during any part of the processing.

Configuration options are available to determine when to stop processing attempts:

maxMinutesRetryAfterFail: 1
maxCallbackFailCount: 5

# Termination

Any submission can be terminated at any stage through a dedicated API endpoint: /ogel-submissions/{id}

