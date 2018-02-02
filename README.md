# cumulus-message-adapter-java

[![CircleCI](https://circleci.com/gh/cumulus-nasa/cumulus-message-adapter-java.svg?style=svg)](https://circleci.com/gh/cumulus-nasa/cumulus-message-adapter-java)

## What is Cumulus?

Cumulus is a cloud-based data ingest, archive, distribution and management
prototype for NASA's future Earth science data streams.

Read the [Cumulus Documentation](https://cumulus-nasa.github.io/)

## What is the Cumulus Message Adapter?

The Cumulus Message Adapter is a library that adapts incoming messages in the
Cumulus protocol to a format more easily consumable by Cumulus tasks, invokes
the tasks, and then adapts their response back to the Cumulus message protocol
to be sent to the next task.

## Installation

With Maven installed, to build the uber-jar, run

```mvn -B package``` 

The resulting message parser uber-jar should be added as a dependency to the lambda task in the lib folder. To deploy the message parser to the task, build and package the message parser and run the following from the message_parser directory: 

```mvn deploy:deploy-file -Durl=file:<path to task lib folder> -Dfile=target/message_parser-1.8.jar -DgroupId=cumulus_message_adapter.message_parser -DartifactId=message_parser -Dpackaging=jar -Dversion=1.8```

## Task definition

In order to use the Cumulus Message Adapter, you will need to create two
methods in your task module: a handler function and a business logic function.

The handler function is a standard Lambda handler function.

The business logic function is where the actual work of your task occurs. The class containing this work should implement the `ITask` interface and the ```String PerformFunction(String input, Context context);``` function. `input` is the simplified JSON from the message adapter and `context` is the AWS Lambda Context.

## Cumulus Message Adapter interface

Create an instance of ```MessageParser``` and call ```HandleMessage(String input, Context context, ITask task)``` with the following parameters:
  
  * `input` - the input to the Lamda function
  * `context` - the Lambda context
  * `task` - an instance of the class that implements `ITask`
  
 ```HandleMessage``` throws a ```MessageAdapterException``` when there is an error.
  
## Example Cumulus task

```java
public class TaskLogic implements ITask
{
    public String PerformFunction(String input, Context context)
    {
        return "{\"status\":\"complete\"}";
    }
}
```

```java
public class Task implements RequestHandler<String, String> 
{
    public String handleRequest(String input, Context context) {
        MessageParser parser = new MessageParser();

        try
        {
            return parser.HandleMessage(input, context, new TaskLogic());
        }
        catch(MessageAdapterException e)
        {
            return e.getMessage();
        }
    }
}
```

For a full example see the [task folder](./task).

## Creating a deployment package

The compiled task code, the message parser uber-jar, the cumulus message adapter zip, and any other dependencies should all be included in a zip file and uploaded to lambda. Information on the zip file folder structure is located [here](https://docs.aws.amazon.com/lambda/latest/dg/create-deployment-pkg-zip-java.html).

## Usage in Cumulus Deployment

During deployment, Cumulus will automatically obtain and inject the Cumulus Message Adapter zip into the compiled code and create a zip file to be deployed to Lambda.

The test task in the 'task' folder of this repository would be configured in lambdas.yml as follows:

```yaml
JavaTest:
  handler: test_task.task.Task::handleRequest
  timeout: 300
  source: '../cumulus-message-adapter-java/deploy/'
  useMessageAdapter: true
  runtime: java8
  memory: 256
```

The source points to a folder with the compiled .class files and dependency libraries in the Lambda Java zip folder structure (details [here](https://docs.aws.amazon.com/lambda/latest/dg/create-deployment-pkg-zip-java.html)), not an uber-jar.

The deploy folder referenced here would contain a folder 'test_task/task/' which contains Task.class and TaskLogic.class as well as a lib folder containing dependency jars. The Cumulus Message Adapter zip would be added at the top level by the deployment step and that folder zipped and deployed to Lambda. 

## Development

### Prerequisites

  * Apache Maven
  
### Building

Build with ```mvn -B package```

### Integration Tests

Integration tests are located in the test folder in ```MessageParserTest.java```. To build and run the tests, run 

```mvn -B test```

### Running the example task

Follow the installation instructions above for the example task.

If updating the version of the message parser, make sure to update the pom.xml in the task code. To build the task with this dependency, run:

```mvn clean install -U```

then ```mvn -B package```

## Why?

This approach has a few major advantages:

1. It explicitly prevents tasks from making assumptions about data structures
   like `meta` and `cumulus_meta` that are owned internally and may therefore
   be broken in future updates. To gain access to fields in these structures,
   tasks must be passed the data explicitly in the workflow configuration.
1. It provides clearer ownership of the various data structures. Operators own
   `meta`. Cumulus owns `cumulus_meta`. Tasks define their own `config`,
   `input`, and `output` formats.
1. The Cumulus Message Adapter greatly simplifies running Lambda functions not
   explicitly created for Cumulus.
1. The approach greatly simplifies testing for tasks, as tasks don't need to
   set up cumbersome structures to emulate the message protocol and can just
   test their business function.
