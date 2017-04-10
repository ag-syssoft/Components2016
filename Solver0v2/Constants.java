/**
 * Created by Thommes on 21.03.2017.
 */
class Constants {
    public static final String BUS_ORDER_EXITED = "Exited!";
    static final String broker0URI = "rabbitmq://136.199.51.111/inExchange?username=kompo&password=kompo&skipQueueDeclare=true";
    static final String generator0URI = "restlet:http://136.199.26.133:80/api/message?restletMethod=post";
    static final String REQUEST_ID = "request-id";
    static final String SENDER = "sender";
    static final String INSTRUCTION = "instruction";
    static final String SUDOKU = "sudoku";
    static final String SOLVE = "solve";
    static final String PING = "ping";
    static final String UNREGISTER = "unregister";
    static final String REGISTER_SOLVER = "register:solver";
    static final String BUS_ORDER_UNREGISTERED = "Unregistered!";
    static final String BUS_ORDER_REGISTERED = "Registered!";
    static final String BUS_ADDRESS_EXTERNAL = "solver.external";
    static final String BUS_ORDER_REGISTER = "Register!";
    static final String solver0URIin = "vertx:" + BUS_ADDRESS_EXTERNAL;
    static final String BUS_ADDRESS_INTERNAL = "solver.internal";
    static final String BUS_ORDER_UNREGISTER = "Unregister!";
    static final String BUS_ORDER_PING = "Ping!";
    static final String BUS_ORDER_TEST = "Test!";
    static final String HEADER_LINE = "HEADER";
    static final String INPUT = "INPUT";
    static final String PONG = "Pong!";
    static final String BUS_ORDER_EXIT = "Exit!";
    private static final String POSTFIXURL = "v22017021402245169.hotsrv.de";
    private static final String SMTPURL = "smtp://" + POSTFIXURL;
    //  static final String solver0URIout = SMTPURL + "?subject=broker" + "&to=kombo@" + POSTFIXURL + "&from=Broker0";
    static String solver0URIout = "smtp://we4c5.netcup.net?username=web731p5&password=komboenten&subject=Solver&to=solver0@thommes.net&from=Broker0";

}
