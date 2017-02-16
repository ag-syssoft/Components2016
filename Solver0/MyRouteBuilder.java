package SolverGID.SolverAID;

import javax.mail.Header;

import org.apache.camel.builder.RouteBuilder;

public class MyRouteBuilder extends RouteBuilder {

	public void configure() {

		from("imaps://imap.gmail.com?username=komposolver@gmail.com&password=KomponentenWiSe!2016"
				+ "&delete=true&searchTerm.unseen=true&sortTerm=arrival&searchTerm.subject=Solver&consumer.delay=10000")
						.process(new MyLogProcessor())
						.choice()
						.when(body().contains("ERROR"))
						.to("log:default")
						.when(header("From").isEqualTo("Generator0"))
						.convertBodyTo(String.class)
						.to(MainApp.generator0URI)
						.otherwise()
						.convertBodyTo(String.class)
						.to(MainApp.broker0URI)
						.endChoice();

		/*
		 * 
		 * 
		 * 
		 * from(
		 * "imaps://imap.gmail.com?username=komposolver@gmail.com&password=KomponentenWiSe!2016"
		 * +
		 * "&delete=false&searchTerm.unseen=true&sortTerm=arrival&searchTerm.subject=Solver&consumer.delay=20000")
		 * .process(new MyLogProcessor()) .choice()
		 * .when(body().contains("ERROR")) .to("log:default") .otherwise()
		 * .convertBodyTo(String.class) .to(
		 * "smtps://smtp.gmail.com?username=komposolver@gmail.com&password=KomponentenWiSe!2016&subject="
		 * + "broker" + "&to=" + "komposolver@gmail.com" + "&from=Broker")
		 * .endChoice() ;
		 */

	}

}