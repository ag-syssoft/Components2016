package netty.test;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

import netty.test.routes.GenericLocalRoute;
import netty.test.routes.NettyEchoRoute;

public class NettyStarter {

	public static boolean stopp = false;
	private static CamelContext ctx = new DefaultCamelContext();
	
	public static void main(String[] args) {
		startCamelTest();
	}

	private static void startCamelTest() {

		try {
			ctx.addRoutes(new NettyEchoRoute());
			ctx.start();

			System.out.println("Context started");

			while (!stopp) {
				Thread.sleep(2000);
			}

			ctx.stop();

			System.out.println("Context stopped");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addRoute(GenericLocalRoute r) {
		System.out.println("Trying to add new Route");
		try {
			ctx.addRoutes(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
