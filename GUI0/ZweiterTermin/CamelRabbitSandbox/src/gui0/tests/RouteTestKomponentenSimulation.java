package gui0.tests;

import java.nio.charset.Charset;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.junit.Test;

import com.google.gson.Gson;

import gui0.MyMessage;
import gui0.parsingProcessor;

public class RouteTestKomponentenSimulation extends CamelTestSupport{
	
	
	//1. Erstelle vereinfachte Route
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception	{
		return new RouteBuilder()	{
			public void configure() throws Exception	{
				String broker_in= "tcp://localhost:8888?textline=true";
				
				from("netty4:" +broker_in)
					.convertBodyTo(String.class).process(new parsingProcessor())
					.choice().when(header("valid-message").isEqualTo(true))
					.to("mock:windowsFormIn").otherwise().to("mock:dummy_out");
				
				
				from("direct:windowsFormOut").convertBodyTo(byte[].class).process(new Processor() {

					@Override
					public void process(Exchange arg0) throws Exception {

						// incoming message compressed with google snappy

						Message m = arg0.getIn();
						byte[] s = (byte[]) m.getBody();
						
						String str_uncompressed = new String(s, Charset.forName("UTF-8"));
						System.out.println("Uncompressed String:>" + str_uncompressed.trim() + "<");

						// processing
													
						// generate message object out of the str_uncompressed string from js
						MyMessage msg = new MyMessage();
						msg.generateDummy(1);
						try{
							JSONObject jObj = XML.toJSONObject(str_uncompressed).getJSONObject("message");
							String json = XML.toJSONObject(str_uncompressed).get("message").toString();
							
							msg.setSender(jObj.getString("sender"));
							msg.setInstruction(jObj.getString("instruction"));
							String sudoku = jObj.getString("sudoku");
							sudoku.replace("[","");
							String[] digits = sudoku.split(",");
							int[] sudokuVals = new int[digits.length];
							for(int i = 0; i < sudokuVals.length; i++){
								sudokuVals[i] = Integer.parseInt(digits[i]);
							}
							
							msg.setSudoku(sudokuVals);
							m.setBody(msg.toJSON());
							m.setHeader("valid-message", true);
						} catch(JSONException e){
							try{
							msg = new Gson().fromJson(str_uncompressed, MyMessage.class);
							m.setHeader("valid-message", true);
							m.setBody(msg.toJSON());
							} catch(Exception ex){
								msg = null;
								m.setHeader("valid-message", false);
							}
						}
					}
				}).choice().when(header("valid-message").isEqualTo(true)).to("mock:broker_out").otherwise().to("mock:dummy_out");

			}
				
		};
	}
	
	
	//2. Schreibe Test
	@Test
	public void testRoute() throws Exception	{
		//nutze Methode der Basistestklasse, um auf die richtigen MockEndpoints zuzugreifen
		MockEndpoint mockWindowsFormsIn = getMockEndpoint("mock:windowsFormIn");
		MockEndpoint mockDummyOut = getMockEndpoint("mock:dummy_out");
		
		//2.1 Formuliere Expectations der Mocks
		//Sende im folgenden eine syntaktisch korrekte und zwei nicht korrekte Nachrichten 
		mockWindowsFormsIn.expectedMessageCount(1);
		mockDummyOut.expectedMessageCount(2);
		
		//Mock Komponente "broker_out" simuliert die Komponente Broker
		MockEndpoint mockBroker_out = getMockEndpoint("mock:broker_out");
		mockBroker_out.whenAnyExchangeReceived(new simulierterBrokerProcessor());
		
		
		//2.2 Initialisiere den Test durch senden einer In-Message
		template.sendBody("direct:windowsFormOut", "Testnachricht -> dummyOut");
		template.sendBody("direct:windowsFormOut", "Testnachricht -> dummyOut");
		
		//JsonTestObject
		MyMessage testMessage = new MyMessage();
		testMessage.setInstruction("register:gui");
		testMessage.setSender("netty4:test");

		template.sendBody("direct:windowsFormOut", testMessage.toJSON());
		
		Thread.sleep(2000);	//lasse Camel Zeit um die Nachricht zu verarbeiten
		
		//2.3 Verifiziere die MockEndpoints
		assertMockEndpointsSatisfied();
	}
	
}
