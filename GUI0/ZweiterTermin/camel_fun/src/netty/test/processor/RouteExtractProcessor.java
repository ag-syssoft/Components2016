package netty.test.processor;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import netty.test.NettyStarter;
import netty.test.routes.GenericLocalRoute;

public class RouteExtractProcessor implements Processor {

	public void process(Exchange exchange) throws Exception {
		String msg = exchange.getIn().getBody(String.class);

//		msg = msg.replaceFirst("\"", "");
//		msg = msg.substring(0, msg.length() - 1);
//
//		System.out.println("msg -> " + msg);
//		System.out.println();
//
//		String utfTest = new String(msg.getBytes(), Charset.forName("UTF-8"));
//		
//		PrintWriter out = new PrintWriter("test.txt");
//		
//		out.println(utfTest);
//		out.close();
		
		Pattern p = Pattern.compile("sender=\"([^\"]|\\\")*?\"");
		Matcher m = p.matcher(msg);

		String routeUri = null;

		System.out.println("Got new message. Trying to extract route.");
		if (m.find()) {
			routeUri = m.group().replaceAll("sender=\"", "");
			routeUri = routeUri.substring(0, routeUri.length() - 1);
		} else {
			System.out.println("No route found");
		}

		if (routeUri != null) {
			System.out.println("Found a route and creating it");
			GenericLocalRoute r = new GenericLocalRoute(routeUri, "Hello there");
			NettyStarter.addRoute(r);
		}

	}

}
