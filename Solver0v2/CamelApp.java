import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;
import org.apache.camel.component.vertx.VertxComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Beeinhaltet und managed den CamelContext.
 */
class CamelApp {
    private static final String name = "CamelApp: ";
    private static String registerUID;
    private static CamelApp singleton;
    private static boolean pingReturned = true;
    private CamelContext camelContext;
    private EventBus eventBus;
    private Vertx vertx;
    private String lastID = null;
    private Timer timer;
    private MessageConsumer internalConsumer;

    private CamelApp(Vertx vertx) throws Exception {
        this.vertx = vertx;
        eventBus = vertx.eventBus();
        internalConsumer = eventBus.consumer(Constants.BUS_ADDRESS_INTERNAL, message -> {
            String body = message.body().toString();
            Core.logger.fine("CamelApp received a message: " + body);
            switch (body) {
                case Constants.BUS_ORDER_PING:
                    pingFunction();
                    break;
                case Constants.BUS_ORDER_TEST:
                    testFunction();
                    break;
                case Constants.BUS_ORDER_REGISTER:
                    try {
                        register();
                    } catch (Exception e) {
                        Core.logger.warning("Register Failed: " + e.toString());
                    }
                    break;
                case Constants.BUS_ORDER_UNREGISTER:
                    unregister();
                    break;
                case Constants.BUS_ORDER_EXIT:
                    unregister();
                    eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, Constants.BUS_ORDER_EXITED);
                    break;
                default:
                    if (body.startsWith(Constants.PONG)) {
                        String temp = body.replace(Constants.PONG, "");
                        if (temp.trim().equals(lastID)) {
                            pingReturned = true;
                            Core.logger.info("Pong match: " + temp + " expected: " + lastID + " Ping returned: " + pingReturned);
                        } else {
                            Core.logger.info("Pong mismatch: " + temp + " expected: " + lastID);
                        }
                    }
            }
        });

    }

    static CamelApp getInstance(Vertx vertx) {
        if (singleton == null) {
            try {
                singleton = new CamelApp(vertx);
            } catch (Exception e) {
                Core.logger.warning("Error in CamelAppCreation: " + e.toString());
            }
        }
        return singleton;
    }

    /**
     * @param unregister Gibt an, ob die JSON für ein Register oder Unregister erzeugt werden soll
     * @return Gibt eine fertige JSON zum Registrieren oder Deregistrieren zurück
     * @see SolverApp#answerJSON(String, String, int[]) analog
     */
    @SuppressWarnings("unchecked")
    private static String registerBody(boolean unregister) {
        UUID id = UUID.randomUUID();
        JSONObject object = new JSONObject();
        registerUID = id.toString();
        object.put(Constants.REQUEST_ID, id.toString());
        object.put(Constants.SENDER, Constants.solver0URIout);
        object.put(Constants.INSTRUCTION, unregister ? Constants.UNREGISTER : Constants.REGISTER_SOLVER);
        JSONArray sudokuArray = new JSONArray();
        sudokuArray.add(0);
        object.put(Constants.SUDOKU, sudokuArray);
        String answer = object.toJSONString();
        String answer2 = answer.replace("\\", "");
        Core.logger.info("Register: " + answer2);
        return answer2;
    }

    /**
     * Erzeugt eine JSON zum Selbsttest der Funktionalität der Applikation
     * @param one Gibt an, welches der beiden Test Sudokus in der JSON verwendet werden soll
     * @return Gibt eine fertig erzeugte JSON zurück
     */
    @SuppressWarnings("unchecked")
    private static String testBody(boolean one) {
        UUID id = UUID.randomUUID();
        JSONObject object = new JSONObject();
        object.put(Constants.REQUEST_ID, id.toString());
        object.put(Constants.SENDER, Constants.solver0URIout);
        object.put(Constants.INSTRUCTION, Constants.SOLVE);
        JSONArray sudokuArray = new JSONArray();
        String sudoku = one ? "0,0,0,25,1,2,0,5,0,4,25,7,0,9,8,7,6,17,19,24,21,16,11,0,0" : "0,0,0,0,1,0,7,5,0,7,4,8,6,0,0,3,0,1,3,0,0,0,9,4,0,0,0,1,0,4,0,0,6,5,0,7,5,9,0,0,7,0,0,3,0,0,0,0,0,4,8,0,0,0,4,3,0,1,0,0,0,0,2,9,0,2,0,0,0,6,4,0,8,0,7,0,5,0,9,1,0";
        String[] tokens = sudoku.split(",");
        for (int j = 0; j < (one ? 25 : 1); j++) {
            for (String token : tokens) {
                try {
                    sudokuArray.add(Integer.parseInt(token));
                } catch (Exception e) {
                    sudokuArray.add(0);
                }
            }
        }
        object.put(Constants.SUDOKU, sudokuArray);
        String answer = object.toJSONString();
        String answer2 = answer.replace("\\", "");
        Core.logger.fine("Test: " + answer2);
        return answer2;
    }

    /**
     * Diese Methode erzeugt die JSON, um einen Ping an den Broker zu senden
     * @return Gibt eine fertig erzeugte JSON zurück
     */
    @SuppressWarnings("unchecked")
    private static String pingBody() {
        UUID id = UUID.randomUUID();
        singleton.lastID = id.toString();
        Core.logger.info("Ping Out and set Last-ID: " + singleton.lastID);
        JSONObject object = new JSONObject();
        object.put(Constants.REQUEST_ID, id.toString());
        object.put(Constants.SENDER, Constants.solver0URIout);
        object.put(Constants.INSTRUCTION, Constants.PING);
        JSONArray sudokuArray = new JSONArray();
        sudokuArray.add(0);
        object.put(Constants.SUDOKU, sudokuArray);
        String answer = object.toJSONString();
        String answer2 = answer.replace("\\", "");
        Core.logger.fine("Ping: " + answer2);
        return answer2;
    }

    /**
     * Erstellt zuerst den CamelContext, wartet dann ab bis dieser die Verbindung aufgebaut hat und schickt anschließend die Registrierungsnachricht an den Broker
     */
    void register() throws Exception {
        camelContext = new DefaultCamelContext();
        VertxComponent vertxComponent = new VertxComponent();
        vertxComponent.setVertx(vertx);
        camelContext.addComponent("vertx", vertxComponent);
        camelContext.addRoutes(new MyRouteBuilder());
        eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "Started, starting Camel...");
        camelContext.start();
        while (camelContext.isStartingRoutes()) {
            Thread.sleep(2000);
        }
        eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "Camel started, registering with Broker...");
        newMessage(registerBody(false));
        eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, Constants.BUS_ORDER_REGISTERED);
        timer = new Timer();
        timer.scheduleAtFixedRate(new Pinger(), 4 * 60000, 4 * 60000);
    }

    /**
     * Nachrichten zur übermittlung an den Brokerß
     *
     * @param message
     */
    private void newMessage(String message) {
        DeliveryOptions options = new DeliveryOptions();
        options.addHeader("From", "Broker0");
        eventBus.publish(Constants.BUS_ADDRESS_EXTERNAL, message, options);
    }

    /**
     * Meldet sich beim Broker0 ab und schließt anschließend die Verbindung zum Vert.x Evenbus
     */
    boolean unregister() {
        if (singleton != null) {
            try {
                eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, Constants.BUS_ORDER_UNREGISTERED);
                this.timer.cancel();
                newMessage(registerBody(true));
                camelContext.stop();
                internalConsumer.unregister();
                singleton = null;
            } catch (Exception e) {
                Core.logger.warning("Stopping CamelContext failed: " + e.toString());
                return false;
            }
        }
        return true;
    }

    /**
     * Ruft die Methode testBody mit einem zufällig generierten Wahrheitswert (true oder false) auf und sendet anschließend die Nachricht
     */
    private void testFunction() {
        try {
            newMessage(testBody(new Random().nextBoolean()));
        } catch (Exception exce) {
            Core.logger.warning("Error sending testMessage: " + exce.toString());
        }
    }

    /**
     * Ruft die Methode pingBody auf und sendet anschließend die Nachricht
     */
    private void pingFunction() {
        try {
            newMessage(pingBody());
        } catch (Exception exce) {
            Core.logger.warning("Error sending pingMessage: " + exce.toString());
        }
    }

    /**
     * Organisiert einen 4 minütigen Ping an den Broker0. Wird der korrekte Pong zwischen 2 Ausführungen nicht empfangen, wird situationsabhänhig versucht die Registrierung zum Broker0 bzw die Camel Route neu aufzubauen.
     */
    private class Pinger extends TimerTask {

        final Logger logger = Core.logger;

        Pinger() {
        }

        @Override
        public void run() {
            logger.info("Pinger on run" + " Var. pingReturned " + pingReturned);
            eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "Automatic Pinger on run");
            boolean restart = false;
            try {
                if (pingReturned) {
                    pingReturned = false;
                    newMessage(pingBody());
                    eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "Ping sent");
                } else {
                    restart = true;
                    if (camelContext.getStatus().equals(ServiceStatus.Started)) {
                        logger.warning("No Pong Returned!");
                        eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "No Pong Returned");
                        newMessage(registerBody(false));
                    } else {
                        logger.warning("No Pong, init not started!");
                        eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "No Pong Returned, init not started");
                        camelContext.stop();
                        eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "Camel stopped, restating CamelApp");
                        logger.info("Camel stopped, restarting CamelApp");
                        singleton = new CamelApp(vertx);
                    }
                }
            } catch (Exception e) {
                logger.warning("Pinger run " + e.toString());
                if (restart) {
                    logger.info("Pinger restart");
                    try {
                        camelContext.stop();
                        logger.info("Camel stopped, restarting CamelApp");
                        singleton = new CamelApp(vertx);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}