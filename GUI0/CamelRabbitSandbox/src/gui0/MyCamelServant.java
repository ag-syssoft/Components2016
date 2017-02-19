package gui0;
import java.nio.charset.Charset;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.xerial.snappy.Snappy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class MyCamelServant {

	// Target -> Broker
	final static String broker_out = "rabbitmq://136.199.51.111/inExchange?username=kompo&password=kompo&skipQueueDeclare=true&exchangeType=fanout&autoDelete=false";

	// Connection to jsgui
	final static String jsgui = "websocket://myUri?sendToAll=true";

	//final static String broker_in = "irc:MyConsumer@irc.freenode.net/#MyCamelTest&nickname=MyConsumer";
	final static String broker_in = "irc:MyConsumer@136.199.53.49:6667/#MyCamelTest&nickname=MyConsumer";
	final static String dummy_out = "stream:out";

	static String my_sender;

	public static void main(String[] args) {

		CamelContext context = new DefaultCamelContext();
		System.out.println("context created");

		try {
			context.addRoutes(new RouteBuilder() {
				public void configure() {

					from(jsgui).convertBodyTo(byte[].class).process(new Processor() {

						@Override
						public void process(Exchange arg0) throws Exception {

							// incoming message compressed with google snappy

							Message m = arg0.getIn();
							byte[] s = (byte[]) m.getBody();
							byte[] unc = Snappy.uncompress(s);

							String str_uncompressed = new String(unc, Charset.forName("UTF-8"));
							System.out.println("uncompressed string received: >" + str_uncompressed.trim() + "<");

							// processing

							// generate message object out of the str_uncompressed string from js

							MyMessage msg = new MyMessage();
							msg.generateDummy(1);
							//msg.setSender("irc:MyJames@irc.freenode.net/#MyCamelTest&nickname=MyJames");
							msg.setSender("irc:MyJames@136.199.53.49:6667/#MyCamelTest&nickname=MyJames");

							switch(str_uncompressed.trim().split(":")[0]) {

							case "register":

								System.out.println("jsgui sent us a register query");
								msg.setInstruction("register:gui");
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								my_sender = msg.getSender();
								break;

							case "unregister":
								msg.setInstruction("jsgui sent us an unregister query");
								msg.setSender(my_sender);
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								break;

							case "ping":

								msg.setInstruction("jsgui sent us a ping request ping");
								msg.setSender(my_sender);
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								break;

							case "pong":

								msg.setInstruction("jsgui sent us a pong answer");
								msg.setSender(my_sender);
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								break;

							case "generate":
								System.out.println("jsgui sent us a generate");
								msg.generateDummy(Integer.valueOf(str_uncompressed.trim().split(":")[1]));
								msg.setInstruction("generate:"+str_uncompressed.trim().split(":")[2]);
								msg.setSender(my_sender);
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								break;

							case "solve":
								System.out.println("jsgui sent us a solve");

								// Generate 1D sudoku
								String input = str_uncompressed.trim().split(":")[1];
								String[] sudoku = input.split(",");
								sudoku[0].replace("[", "");
								sudoku[sudoku.length-1].replace("]","");

								int[] sudoku1d = new int[sudoku.length];

								for(int i=0; i<sudoku.length; i++) {
									sudoku1d[i] = Integer.valueOf(sudoku[i]);
								}

								msg.setSudoku1D(sudoku1d);
								msg.setInstruction("solve");
								msg.setSender(my_sender);
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								break;

								default:
									System.err.println("jsgui sent us the following unsupported message: " + str_uncompressed);
									break;

							}

						}
					}).choice().when(header("valid-message").isEqualTo(true)).to(broker_out).otherwise().to(dummy_out);

				}
			});

			context.addRoutes(new RouteBuilder() {
				public void configure() {

					from(broker_in).convertBodyTo(String.class).process(new Processor() {

						@Override
						public void process(Exchange arg0) throws Exception {

							// incoming message from broker, print it

							Message m = arg0.getIn();
							String in_string = (String) m.getBody();

							System.out.println("incoming message from broker, i'll try to parse it");

							// check if message conforms to our json standard,
							// it could be IRC garbage

							Gson gson = new Gson();

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

							m.setBody(Snappy.compress((String) m.getBody()));

						}
					}).choice().when(header("valid-message").isEqualTo(true)).to(jsgui).otherwise().to(dummy_out);

				}
			});
			System.out.println("routes added");
			context.start();
			Thread.currentThread().join();
			System.out.println("context started");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
