package rabbitmq.tutorial;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Send {

	private static final String QUEUE_NAME = "hallo";

	public Send() {
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			Connection con = factory.newConnection();
			Channel chan = con.createChannel();
			
			chan.exchangeDeclare("hallo", "fanout");
			
			String message = "Hello World!";
			
			chan.basicPublish("hallo", "", null, message.getBytes("UTF-8"));
			System.out.println(" [x] Sent '" + message + "'");
			
			chan.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
