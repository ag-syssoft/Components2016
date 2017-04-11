package gui0;

import java.nio.charset.Charset;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.xerial.snappy.Snappy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class parsingProcessor implements Processor {
	static String my_sender;

	@Override
	public void process(Exchange arg0) throws Exception {

		// incoming message from broker, print it

		Message m = arg0.getIn();
		String in_string = (String) m.getBody();
		System.out.println(in_string);
		System.out.println("Incoming Message, i'll try to parse it");

		// check if message conforms to our json standard,
		// it could be IRC garbage

		Gson gson = new Gson();
		boolean msg_ok = true;
		try {
			MyMessage recv_msg_obj = gson.fromJson(in_string, MyMessage.class);
			System.out.println(in_string);
			System.out.println("Message was JSON and parsed successfully");
			m.setHeader("valid-message", true);

			// create the target data for jsgui

			switch (recv_msg_obj.getInstruction()) {

			case "register:gui":

				m.setBody("register#gui");
				break;

			case "register:generator":
				m.setBody("register#generator");
				break;

			case "generate":

				m.setBody("generate#reply");
				break;

			case "register:solver":
				m.setBody("register#solver");
				break;

			case "solve":

				m.setBody("solve");
				break;

			case "display":
				m.setBody("display#" + gson.toJson(recv_msg_obj.getSudoku()));
				break;
			case "pong":
				m.setBody("pong");
				break;
			case "solved:impossible":
			case "solved:one":
			case "solved:many":
				m.setBody(recv_msg_obj.getInstruction() + "#" + gson.toJson(recv_msg_obj.getSudoku()));
				break;

			default:
				System.err.println("Unknown Message received: " + in_string);
				m.setBody(in_string);
				msg_ok = false;
				break;
			}

		} catch (JsonSyntaxException ex) {
			msg_ok = false;
			System.err.println("Message was RUBBISH and will be dropped");
		}

		arg0.setOut(m);
	}
}
