package org.komponententechnologien.broker;

import java.io.IOException;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import com.rabbitmq.client.*;

public class CamelInstance {
	static CamelContext context;
	static ConnectionFactory factory;
	//this is my queue to consume outgoing messages from
	static final String OUTQUEUE = "rabbitmq://136.199.51.111/out?username=kompo&password=kompo&queue=out&skipQueueDeclare=true";
	public static void main(String[] args) throws Exception {
		context = new DefaultCamelContext();
		factory = new ConnectionFactory();
		factory.setUri("amqp://kompo:kompo@136.199.51.111:5672");
		Connection conn = factory.newConnection();
		Channel channel = conn.createChannel();
		//setup config-consumer that consumes and processes config-messages from the broker
		DefaultConsumer cons = new DefaultConsumer(channel){
			@Override
	         public void handleDelivery(String consumerTag,
	                                    Envelope envelope,
	                                    AMQP.BasicProperties properties,
	                                    byte[] body)
	             throws IOException
	         {
				for(Route r:context.getRoutes()){
					System.out.println(r.getId()+"  "+r.getEndpoint().getEndpointUri());
				}
	             String routingKey = envelope.getRoutingKey();
	             long deliveryTag = envelope.getDeliveryTag();
	             String uri = new String(body);
	             configureRoute(uri, routingKey);
	             
	             channel.basicAck(deliveryTag, false);
	         }
		};
		channel.basicConsume("config", false,"camelInstance",cons);
		context.start();
	}
	
	public static void configureRoute(String uri, String id){
		//If dont have a route with that routingKey (componentID) create it, otherwise delete it
		if(context.getRoute("route"+id)==null){
			
		try {
			context.addRoutes(new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {
					from(OUTQUEUE+"&routingKey="+id).to(uri).id(id);
					System.out.println("Added route from RoutingKey "+id+" to "+uri);
				}
			});
		} catch (Exception e) {
			System.out.println("Could not create new route to "+uri);
		}
		}
		else{
			try{
				context.stopRoute("route"+id);
				context.removeRoute("route"+id);
				System.out.println("Removed route with id "+id);
				
			}catch (Exception e) {
				System.out.println("Could not delete route to "+uri);
			}
		}
	}


}
