package SolverGID.SolverAID;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;



public class MyLogProcessor implements Processor {
    public void process(Exchange exchange) throws Exception {
    	String body = exchange.getIn().getBody(String.class);
        exchange.getOut().setBody(Parser.parse(body));
    }
}