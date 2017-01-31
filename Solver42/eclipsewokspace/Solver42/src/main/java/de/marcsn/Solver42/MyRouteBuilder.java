package de.marcsn.Solver42;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure()
    {
    	Processor py_to_json = new PyToJsonProcessor();
 
    	from("zeromq:tcp://127.0.0.1:5555?socketType=PULL")		 	
    		.convertBodyTo(String.class) 							// Der Body an sicht ist zunächst vom Typ GenericFile, repräsentiert also noch den urspr File Endpoint.
	    	.process(py_to_json)									// Konvertieurng, siehe Klasse PyToJsonProcessor
	    	.log("${body}")											// Print auf die Konsole
	    	.to("rabbitmq://136.199.51.111/inExchange?username=kompo&password=kompo&skipQueueDeclare=true");
    }

}
