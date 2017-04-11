package netty.test.routes;

import org.apache.camel.builder.RouteBuilder;

import camelprocessor.MyTestProcessor;
import netty.test.processor.RouteExtractProcessor;

public class NettyEchoRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		// Netty Empfangs route
		from("netty4:tcp://0.0.0.0:5555").process(new RouteExtractProcessor())
				.to("file://camel/echo/");
	}

}
