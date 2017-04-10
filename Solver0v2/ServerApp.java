import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Erzeugt eine HTML-Seite als Benutzeroberfläche.
 */
class ServerApp {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private static Calendar cal = Calendar.getInstance();
    private static String textboxContent = sdf.format(cal.getTime()) + ": " + "Server started\n";
    private static String JSONInboxContent = "Kein Input";
    private static String JSONOutboxContent = "Kein Output";
    private final EventBus eventBus;
    private final Vertx vertx;
    private boolean camelStarted;
    private String lastSudokuResult = "no Sudoku called yet";
    private String lastSudokuTime = "no Sudoku called yet";

    /**
     * Dynamische erstellung einer HTML Seite zum anzeigen von Nachrichten und senden von Steuerbefehlen mittels HTML Formular/HTTP-GET.
     * @param vertx Vertx-Instanz.
     */
    ServerApp(Vertx vertx) {
        this.vertx = vertx;
        eventBus = vertx.eventBus();
        eventBus.consumer(Constants.BUS_ADDRESS_INTERNAL, message -> {
            MultiMap input = message.headers();
            String line = input.get(Constants.HEADER_LINE);
            String body = message.body().toString();
            if (line != null && line.equals(Constants.INPUT)) {
                addTexttoJSONInBox(body);
            } else {
                Core.logger.fine("ServerApp received a message: " + body);
                addTexttoTextbox(body);
                if (body.equals(Constants.BUS_ORDER_REGISTERED)) {
                    camelStarted = true;
                } else if (body.equals(Constants.BUS_ORDER_UNREGISTERED)) {
                    camelStarted = false;
                }
                if (body.contains("solved")) {
                    cal = Calendar.getInstance();
                    lastSudokuTime = sdf.format(cal.getTime());
                    lastSudokuResult = body.substring(body.indexOf("solved") + 7);
                }
            }
        });
        eventBus.consumer(Constants.BUS_ADDRESS_EXTERNAL, message -> {
            MultiMap input = message.headers();
            String line = input.get(Constants.HEADER_LINE);
            String body = message.body().toString();

            Core.logger.fine("ServerApp send a message: " + body);
            if (body.startsWith("{")) {
                addTexttoJSONOutBox(body);
            }
        });
        runHTTP();
    }

    /**
     * Fügt der Textarea einen Text hinzu.
     *
     * @param s String welcher als Text hinzugefügt wird.
     */
    private static void addTexttoTextbox(String s) {
        cal = Calendar.getInstance();
        textboxContent += sdf.format(cal.getTime()) + ": " + s + "\n";
    }

    /**
     * Fügt dem Inputfeld einen Text hinzu.
     *
     * @param s String welcher als Text hinzugefügt wird.
     */
    private static void addTexttoJSONInBox(String s) {
        cal = Calendar.getInstance();
        JSONInboxContent = sdf.format(cal.getTime()) + "\n" + "\n" + s;
    }

    /**
     * Fügt dem Outputfeld einen Text hinzu.
     *
     * @param s String welcher als Text hinzugefügt wird.
     */
    private static void addTexttoJSONOutBox(String s) {
        cal = Calendar.getInstance();
        JSONOutboxContent = sdf.format(cal.getTime()) + "\n" + "\n" + s;
    }

    /**
     * Aufbau der Seite
     */
    private void runHTTP() {
        vertx.createHttpServer().requestHandler(request -> {
            if (request.uri().equals("/")) {
                /**
                 * Serve the index page for the Sudoku Solver Command Line
                 */
                request.response().putHeader("content-type", "text/html").putHeader("refresh", "2").end(
                        "<html><head><title>Sudoku Solver GUI</title></head><body><div id='buttons'><h1>Sudoku Solver Command Line</h1>" +
                                "<form action='?' method='post'>"
                                + "  <input type='submit' name='0' value='" + (camelStarted ? "Unregister" : "Register") + "' />"
                                + "  <input type='submit' name='1' value='Ping' />"
                                + "  <input type='submit' name='2' value='Test' />"
                                + "  <input type='submit' name='3' value='Shutdown' />"
                                + "  </form></div><div>"
                                + "<textarea readonly id='textarea' rows=\"30\" cols=\"105\">" + textboxContent + "</textarea><script>var textarea = document.getElementById('textarea');textarea.scrollTop = textarea.scrollHeight;</script>"
                                + "<form>Last Sudoku solved at:<br><input type='text' value=\"" + lastSudokuTime + "\" name='lastSudokuTime' readonly><br> Last result of solve:<br><input type='text' value=\"" + lastSudokuResult + "\" name='lastSudokuResult' readonly></form>"
                                + "</div>" +
                                "<table><tr><th align='left'>Input</th><th align='left'>Output</th></tr>" +
                                "<tr><td><textarea readonly id='textarea2' rows=\"10\" cols=\"50\">" + JSONInboxContent + "</textarea></td>"
                                + "<td><textarea readonly id='textarea3' rows=\"10\" cols=\"50\">" + JSONOutboxContent + "</textarea></td></tr></div>"
                                + "</body></html>"
                );


            } else if (request.uri().startsWith("/?")) {
                request.response().setChunked(true);
                request.setExpectMultipart(true);
                request.endHandler((v) -> {
                    for (String attr : request.formAttributes().names()) {
                        int buttonpressed = Integer.parseInt(attr);
                        switch (buttonpressed) {

                            case 0:
                                publish(camelStarted ? Constants.BUS_ORDER_UNREGISTER : Constants.BUS_ORDER_REGISTER);
                                break;
                            case 1:
                                publish(Constants.BUS_ORDER_PING);
                                break;
                            case 2:
                                publish(Constants.BUS_ORDER_TEST);
                                break;
                            case 3:
                                publish(Constants.BUS_ORDER_EXIT);
                                break;
                            default:
                                break;
                        }
                    }
                    request.response().putHeader("content-type", "text/html").putHeader("refresh", "2").end(
                            "<html><head><title>Sudoku Solver GUI</title></head><body><div id='buttons'><h1>Sudoku Solver Command Line</h1>" +
                                    "<form action='?' method='post'>"
                                    + "  <input type='submit' name='0' value='" + (camelStarted ? "Unregister" : "Register") + "' />"
                                    + "  <input type='submit' name='1' value='Ping' />"
                                    + "  <input type='submit' name='2' value='Test' />"
                                    + "  <input type='submit' name='3' value='Shutdown' />"
                                    + "  </form></div><div>"
                                    + "<textarea readonly id='textarea' rows=\"30\" cols=\"105\">" + textboxContent + "</textarea><script>var textarea = document.getElementById('textarea');textarea.scrollTop = textarea.scrollHeight;</script>"
                                    + "<form>Last Sudoku solved at:<br><input type='text' value=\"" + lastSudokuTime + "\" name='lastSudokuTime' readonly><br> Last result of solve:<br><input type='text' value=\"" + lastSudokuResult + "\" name='lastSudokuResult' readonly></form>"
                                    + "</div>" +
                                    "<table><tr><th align='left'>Input</th><th align='left'>Output</th></tr>" +
                                    "<tr><td><textarea readonly id='textarea2' rows=\"10\" cols=\"50\">" + JSONInboxContent + "</textarea></td>"
                                    + "<td><textarea readonly id='textarea3' rows=\"10\" cols=\"50\">" + JSONOutboxContent + "</textarea></td></tr></div>"
                                    + "</body></html>"
                    );

                });
            } else {
                request.response().setStatusCode(404).end();
            }
        }).listen(8089);
    }

    /**
     * Sendet eine Nachricht in den eventBus.
     *
     * @param message Nachricht welche versendet wird.
     */
    private void publish(String message) {
        eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, message);
    }

}