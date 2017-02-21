# GUI0
## How to run
CamelRabbitSandbox: Gateway; connects jsgui to broker.
Just import the project Folder into Eclipse and run it. Don't forget the maven dependencies. Remember to alter the configuration for rabbitmq (connection to broker). You'll also need an IRC Server, just look into the first few lines of MyCamelServant.java.

JSClient: JavaScript GUI.
Run the following in folder JSClient and access localhost:8000 with any browser.
``` 
     python -m SimpleHTTPServer 8000
``` 

