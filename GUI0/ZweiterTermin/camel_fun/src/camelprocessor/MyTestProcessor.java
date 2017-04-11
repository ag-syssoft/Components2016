package camelprocessor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import MainApp.Starter;

public class MyTestProcessor implements Processor {

	public void process(Exchange exchange) throws Exception {
		String msg = exchange.getIn().getBody(String.class);

		Starter.stopp = msg.equals("exit");

//		msg = msg.toUpperCase();

		exchange.getIn().setBody(msg);
	}

}
