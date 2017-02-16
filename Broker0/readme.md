# Broker0
## What is it
CSharp: The basic logic of the broker, saves currently registered components and redirects messages
Java: The Apache Camel instance to send messages to other components
RabbitMQ: The messaging system used
## How to start
Change the password-hash in rabbitmq/rabbitmq_config/definitions.json
csharp, java and rabbitmq are prepared for beeing packed into docker containers by executing inside all 3 folder
``` 
     docker-compose build
     docker-compose up
``` 
