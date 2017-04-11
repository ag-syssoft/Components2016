package camelroutes;

import org.apache.camel.builder.RouteBuilder;

import camelprocessor.MyTestProcessor;
import camelprocessor.NettyTestRespProcessor;

public class NettyTestEchoRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		// from("stream:in?promptMessage=Enter something:").process(new
		// MyTestProcessor())
		// .to("netty4:tcp://localhost:5555?keepAlive=true");
		// Netty Client Empfangs route
		from("netty4:tcp://0.0.0.0:8888/").process(new NettyTestRespProcessor()).to("stream:out");
	}

}
