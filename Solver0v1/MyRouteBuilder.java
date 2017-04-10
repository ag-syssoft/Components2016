import javax.mail.Header;

import org.apache.camel.builder.RouteBuilder;

public class MyRouteBuilder extends RouteBuilder {

	public void configure() {

		from(MainApp.solver0URIin)
						.process(new MyLogProcessor())
						.choice()
						.when(body().contains("ERROR"))
						.to("log:default")
						.otherwise()
						.convertBodyTo(String.class)
						.to(MainApp.broker0URI)
						.endChoice();
	}
/**
 * 	from(MainApp.solver0URIin)
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
 */
}