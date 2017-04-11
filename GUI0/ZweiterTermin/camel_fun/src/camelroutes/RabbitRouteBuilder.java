package camelroutes;

import org.apache.camel.builder.RouteBuilder;

import camelprocessor.MyTestProcessor;

public class RabbitRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("stream:in?promptMessage=Enter something:").process(new MyTestProcessor())
				.to("rabbitmq://localhost/hallo?exchangeType=fanout&autoAck=true&autoDelete=true&durable=false");
	}

}
