SNS Event Stream
================

Hooks up an SNS topic to server-sent events in JavaScript, allowing you to
push messages to browsers by just publishing to an SNS topic.

This will autoscale:

* On start, register with the SNS topic, saying "this is my IP address, post
  messages to me!"
* Expose an endpoint to the loadbalancer, to which clients can connect and
  wait for events to be sent
* Follow a queue set up for autoscaling events, and on an EC2 box being taken
  out of the autoscaling group, automatically unsubscribe that box from the
  SNS topic

TODO:

* What should healthcheck be based on? The number of connected clients? Might
  need to do some experimentation to see what an ideal number is.

