package netty.test.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import MainApp.Starter;

public class EchoMessageProcessor implements Processor {

	private String msg;
	
	public EchoMessageProcessor(String msg) {
		super();
		this.msg = msg;
	}
	
	public void process(Exchange exchange) throws Exception {
		Starter.stopp = msg.equals("exit");

		exchange.getIn().setBody(msg);
	}

}
