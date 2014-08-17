SNS Event Stream
================

Application to wrap an SNS topic, providing an endpoint for JavaScript to poll
events using [server-sent events](http://dev.w3.org/html5/eventsource/). 

Allows horizontal scaling, which is where most of the complexity comes in.

## Lifecycle of a box

* Looks up its own public hostname using EC2 metadata
* Registers a subscription with the SNS topic for /broadcast on that hostname
* Looks up its instance ID using EC2 metadata
* Records its subscription ARN against its instance ID in DynamoDB
* Polls an autoscaling notifications queue, automatically unsubscribing
  instances that are killed due to autoscaling, then removing their data from
  DynamoDB

Clients connect to /events. The body of the SNS notification will be sent as
the body of the server-sent event.

