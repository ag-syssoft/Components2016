import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Die Main-Klasse der Applikation
 * Initialisiert die Sub-Komponenten.
 * Überprüft, ob CamelApp gestartet ist oder nicht. Falls ein Shutdown gerufen wird, und CamelApp bereits beendet wird, wird direkt das Programm beendet.
 * Ansonsten wird auf die deregistrierung der CamelApp gewartet.
 */
class Core {
    private static final String name = "Core: ";
    static Logger logger;
    private static Vertx vertx;
    private static CamelApp camelApp;
    private static ServerApp serverApp;
    private static EventBus eventBus;
    private static boolean camelRegistered = true; // wird erst nach der ersten Unregistrierung benötigt.

    public static void main(String[] args) {
        initLogger();
        vertx = Vertx.vertx();
        serverApp = new ServerApp(vertx);
        camelApp = CamelApp.getInstance(vertx);
        SolverApp solverApp = new SolverApp(vertx);
        solverApp.start();
        eventBus = vertx.eventBus();
        eventBus.consumer(Constants.BUS_ADDRESS_INTERNAL, message -> {
            String body = message.body().toString();
            logger.fine("Core received a message: " + body);
            switch (body) {
                case Constants.BUS_ORDER_REGISTERED:
                    camelRegistered = true;
                    break;
                case Constants.BUS_ORDER_UNREGISTERED:
                    camelRegistered = false;
                    break;
                case Constants.BUS_ORDER_EXIT:
                    if (!camelRegistered) {
                        close();
                    }
                    break;
                case Constants.BUS_ORDER_REGISTER:
                    try {
                        if (!camelRegistered) {
                            camelApp = CamelApp.getInstance(vertx);
                            camelApp.register();
                        }
                    } catch (Exception e) {
                        Core.logger.warning("Register Failed: " + e.toString());
                    }
                    break;
                case Constants.BUS_ORDER_EXITED:
                    close();
                    break;
            }
        });
    }

    /**
     * Diese Methode schließt die Applikation.
     * @see ServerApp
     */
    public static void close() {
        eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "Application terminated");
        System.exit(0);
    }

    /**
     * Zentrale initialisierung des Loggers.
     */
    private static void initLogger() {
        logger = Logger.getLogger("MyLog");
        FileHandler fh;
        try {
            fh = new FileHandler("LogFile.txt");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.setLevel(Level.FINE);
            logger.fine("---- Log Init ----");
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }
}
