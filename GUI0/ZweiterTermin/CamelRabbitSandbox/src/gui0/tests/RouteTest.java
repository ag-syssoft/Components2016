package gui0.tests;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import gui0.MyMessage;
import gui0.parsingProcessor;

public class RouteTest extends CamelTestSupport{
	
	
	//1. Erstelle vereinfachte Route
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception	{
		return new RouteBuilder()	{
			public void configure() throws Exception	{
				from("direct:broker_in")
					.convertBodyTo(String.class).process(new parsingProcessor())
					.choice().when(header("valid-message").isEqualTo(true))
					.to("mock:windowsFormIn").otherwise().to("mock:dummy_out");
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
		
		
		//2.2 Initialisiere den Test durch senden einer In-Message
		template.sendBody("direct:broker_in", "Testnachricht -> dummyOut");
		template.sendBody("direct:broker_in", "Testnachricht -> dummyOut");
		
		//JsonTestObject
		MyMessage testMessage = new MyMessage();
		testMessage.setInstruction("register:gui");
		testMessage.setSender("netty4:test");

		template.sendBody("direct:broker_in", testMessage.toJSON());
		
		Thread.sleep(2000);	//lasse Camel Zeit um die Nachricht zu verarbeiten
		
		//2.3 Verifiziere die MockEndpoints
		assertMockEndpointsSatisfied();
	}
	
}
