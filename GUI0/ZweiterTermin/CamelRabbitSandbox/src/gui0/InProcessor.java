package gui0;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class InProcessor implements Processor{

	@Override
	public void process(Exchange arg0) throws Exception {
		// incoming message from broker, print it

		Message m = arg0.getIn();
		String in_string = (String) m.getBody();
		System.out.println("Incoming Message, i'll try to parse it");

		// check if message conforms to our json standard,
		// it could be IRC garbage

		Gson gson = new Gson();
		boolean msg_ok = true;
		try {
			MyMessage recv_msg_obj = gson.fromJson(in_string, MyMessage.class);
			System.out.println(in_string);
			System.out.println("the message was in proper format and could be converted into a mymessage object");
			m.setHeader("valid-message", true);

			// create the target data for jsgui
			
			switch(recv_msg_obj.getInstruction()) {
			
			default:
				
				System.out.println("regardless of the instruction, i forward the entire json to jsgui");
				m.setBody(in_string);
				break;
			}


		} catch (JsonSyntaxException ex) {
			System.err.println("the message was rubbish and i'll drop it");
		}
		
		//m.setBody(Snappy.compress((String) m.getBody()));
		arg0.setOut(m);
	}

}
