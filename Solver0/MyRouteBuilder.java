package comp.solver;

import org.apache.camel.builder.RouteBuilder;

public class MyRouteBuilder extends RouteBuilder {

    public void configure() {

        from("imaps://imap.gmail.com?username=&password="
                + "&delete=true&searchTerm.unseen=true&sortTerm=arrival&searchTerm.subject=Solver&consumer.delay=20000")
                .process(new MyLogProcessor())
                .choice()
                .when(body().contains("ERROR"))
                .to("log:default")
                .otherwise()
                .convertBodyTo(String.class)
                .to(MainApp.broker0)
                .endChoice()
        ;
    }
}