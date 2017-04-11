package camelroutes;

import org.apache.camel.builder.RouteBuilder;

import camelprocessor.MyTestProcessor;

public class NettyTestBrokerRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		// from("stream:in?promptMessage=Enter something:").process(new
		// MyTestProcessor())
		// .to("netty4:tcp://localhost:5555?keepAlive=true");
		// Netty Client Versands route
		from("file://camel/input").process(new MyTestProcessor()).to("netty4:tcp://192.168.56.101:5555");
	}

}
