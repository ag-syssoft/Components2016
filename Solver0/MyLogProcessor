package Solver;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;



public class MyLogProcessor implements Processor {
    public void process(Exchange exchange) throws Exception {
        exchange.getOut().setBody(Parser.parse(exchange.getIn().getBody(String.class)));
    }
}
