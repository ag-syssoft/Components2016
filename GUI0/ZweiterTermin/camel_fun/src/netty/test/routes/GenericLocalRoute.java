package netty.test.routes;

import org.apache.camel.builder.RouteBuilder;

import netty.test.processor.EchoMessageProcessor;

public class GenericLocalRoute extends RouteBuilder {

	private String toRoute;
	private String msg;
	
	public GenericLocalRoute(String route, String msg) {
		super();
		this.toRoute = route;
		this.msg = msg;
	}
	
	@Override
	public void configure() throws Exception {
		// Netty Broker Versands route
		from("file://camel/echo/").process(new EchoMessageProcessor(msg))
				.to(toRoute);
	}
	
}
