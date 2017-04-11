package camelprocessor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import MainApp.Starter;

public class NettyTestRespProcessor implements Processor {

	public void process(Exchange exchange) throws Exception {
		String msg = exchange.getIn().getBody(String.class);

		String resp = "Got response from Broker: " + msg;

		exchange.getIn().setBody(resp);
	}

}
