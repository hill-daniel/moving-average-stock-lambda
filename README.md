# moving-average-stock-lambda
Moving average calculation of stocks

Written in 2017.

This code should be deployed as a lambda function in AWS.
* Pulls stock data from an API
* Calculates 200 day moving average
* Recommends action (buy, sell, hold) via SNS i.e. sends an email
* Stores new value in DynamoDB

## Technology used in this one:
* [Java 8](https://java.com/de/) Programming language
* [Immutables](https://immutables.github.io/) Immutable data types for Java
* [AWS Lambda](https://aws.amazon.com/lambda/) Serverless code execution
* [AWS SNS](https://aws.amazon.com/sns/) Notifications (E-Mail, Push, SMS...)
* [AWS CloudWatch](https://aws.amazon.com/cloudwatch/) Events, Logs, Metrics
* [AWS DynamoDB](https://aws.amazon.com/de/dynamodb/)
* [UniRest](http://unirest.io/) REST client
