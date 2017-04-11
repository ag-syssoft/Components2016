package gui0.tests;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.junit4.CamelTestSupport;

public class simulierterBrokerProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		ProducerTemplate template = exchange.getContext().createProducerTemplate();
		System.out.println("simulierender Broker leitet die Nachricht weiter");
		template.sendBody("netty4:tcp://localhost:8888?textline=true", exchange.getIn().getBody());

	}
}
