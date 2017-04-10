import org.apache.camel.builder.RouteBuilder;

class MyRouteBuilder extends RouteBuilder {

    public void configure() {

        from(Constants.solver0URIin)
                .process(new MyLogProcessor())
                .choice()
                .when(body().contains("ERROR"))
                .to("log:default")
                .otherwise()
                .convertBodyTo(String.class)
                .to(Constants.broker0URI)
                .endChoice();
    }
    /*
    from(Constants.solver0URIin)
                .choice()
                .when(body().contains("ERROR"))
                .to("log:default")
                .when(header("From").isEqualTo("Broker0"))
                .convertBodyTo(String.class)
                .to(Constants.broker0URI)
                .otherwise()
                .convertBodyTo(String.class)
                .to(Constants.SMTPURL + "?to=" + header("From"))
                .endChoice();
     */
}
