import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Properties;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * SolverApp liest als überwacht in run() als extra Thread permanent den MailDir Ordner des Postfix E-Mailfaches.
 * Bei einer neuen Datei wird die MIME-Datei konvertiert und ein ggf vorhandes JSON ausgelesen und weiterverarbeitet.
 */
class SolverApp extends Thread {
    private static final String name = "SolverApp: ";
    private static final String[] solvedResponses = {"solved:impossible", "solved:one", "solved:many", "solved:illegal"};
    private final EventBus eventBus;
    private DeliveryOptions optionsInput = new DeliveryOptions();

    public SolverApp(Vertx vertx) {
        eventBus = vertx.eventBus();
        eventBus.consumer(Constants.BUS_ADDRESS_INTERNAL, message -> {
            String body = message.body().toString();
            Core.logger.fine("SolverApp received a message: " + body);
        });
        optionsInput.addHeader(Constants.HEADER_LINE, Constants.INPUT);
    }

    /**
     * @param request     Die Request-ID aus dem Parser wird übergeben
     * @param instruction Die Instruction aus dem Parser wird übergeben
     * @param sudoku      Das Sudoku aus dem Parser wird übergeben
     * @return Baut den fertigen JSON-String auf und gibt ihn an die Methode parse() zurück
     * @see SolverApp#parse(String)
     */
    @SuppressWarnings("unchecked")
    private static String answerJSON(String request, String instruction, int[] sudoku) {
        JSONObject object = new JSONObject();
        object.put(Constants.REQUEST_ID, request);
        object.put(Constants.SENDER, Constants.solver0URIout);
        object.put(Constants.INSTRUCTION, instruction);
        if (sudoku.length != 0) {
            JSONArray sudokuArray = new JSONArray();
            for (int aSudoku : sudoku) {
                sudokuArray.add(aSudoku);
            }
            object.put(Constants.SUDOKU, sudokuArray);
        }
        String answer = object.toJSONString();

        String answer2 = answer.replace("\\", "");
        Core.logger.info("Output: " + answer2);
        return answer2;
    }

    /**
     * Überwacht den MailDir Ordner des Postfix Servers in einer Endlosschleife.
     */
    @Override
    public void run() {
        String dirPath = "/home/kombo/Maildir/new/";
        Path path = Paths.get(dirPath);
        try {
            Boolean isFolder = (Boolean) Files.getAttribute(path,
                    "basic:isDirectory", NOFOLLOW_LINKS);
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path + " is not a folder");
            }
        } catch (IOException ioe) {
            // Ordner existiert nicht.
            ioe.printStackTrace();
        }
        Core.logger.fine("Watching path: " + path);
        FileSystem fs = path.getFileSystem();
        try (WatchService watchService = fs.newWatchService()) {
            path.register(watchService, ENTRY_CREATE);
            WatchKey key = null;
            while (true) {
                key = watchService.take();
                WatchEvent.Kind<?> kind = null;
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    kind = watchEvent.kind();
                    if (OVERFLOW != kind && ENTRY_CREATE == kind) {
                        Path newPath = ((WatchEvent<Path>) watchEvent).context();
                        System.out.println("New path created: " + newPath);
                        Path p = Paths.get(dirPath + newPath);
                        Session session = Session.getDefaultInstance(new Properties());
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(p));
                        MimeMessage mimeMessage = new MimeMessage(session, inputStream);
                        String message = null;
                        try {
                            message = getTextFromMessage(mimeMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println("M: " + message);
                        String sender = mimeMessage.getFrom()[0].toString();
                        System.out.println("S: " + sender);
                        newMessage(message, sender);
                        Files.deleteIfExists(p);
                    }
                }
                if (!key.reset()) {
                    break; //loop
                }
            }
        } catch (IOException | InterruptedException | MessagingException e) {
            Core.logger.warning("Filewatch: " + e.toString());
        }
    }

    /**
     * @param message MIME E-Mail
     * @return den Inhalt einer E-Mail, funktioniert nicht bei HTML Nachrichten.
     */
    private String getTextFromMessage(Message message) throws Exception {
        String result = "";
        if (message.isMimeType("application/json")) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            InputStream inputStream = message.getInputStream();
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            result = byteArrayOutputStream.toString("UTF-8");
        } else if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else {
            Core.logger.warning("Message neither text/plain nor application/json!");
        }
        return result;
    }


    /**
     * Parsed
     *
     * @param input  Inhalt der E-Mail
     * @param sender Absender der E-Mail aus MIME
     */
    private void newMessage(String input, String sender) {
        System.out.println("newMessage: " + sender);
        int first = sender.indexOf("<");
        if (first > 0) {
            int second = sender.indexOf(">", first);
            sender = sender.substring(first + 1, second);
        }
        if (sender.equals("Broker0")) {
            eventBus.publish(Constants.BUS_ADDRESS_EXTERNAL, parse(input));
        } else {
            Properties properties = System.getProperties();
            properties.setProperty("mail.smtp.host", "localhost");
            Session session = Session.getDefaultInstance(properties);
            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress("kombo@v22017021402245169.hotsrv.de"));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(sender));
                message.setSubject("Sudoko Result");
                message.setText(parse(input));
                Transport.send(message);
                System.out.println("Sent message successfully....");
            } catch (MessagingException e) {
                Core.logger.warning("EMail: " + e.toString());
            }
        }
    }

    /**
     * @param input Der Inhalt der empfangenen E-Mail wird dem Parser zur weiteren Verarbeitung übergeben
     * @return Gibt die fertige JSON mit Hilfe der Methode answerJSON() zurück oder ein entsprechender Fehler wird in MyRouteBuilder geloggt.
     * @see SolverApp#answerJSON(String, String, int[])
     * @see MyRouteBuilder
     * @see ServerApp#publish(String)
     */
    private String parse(String input) {
        eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, input, optionsInput);
        Core.logger.info("Input:\n" + input);
        String request = "";
        String instruction = "";
        int[] sudoku = {};
        String modInput = input.replace("|", "").replace(" ", "").replace("\r", "").replace("\n", "").replace("\t", "");
        JSONParser parser = new JSONParser();
        try {
            Object object = parser.parse(modInput);
            JSONObject myJSONObject = (JSONObject) object;
            request = (String) myJSONObject.get(Constants.REQUEST_ID);
            instruction = (String) myJSONObject.get(Constants.INSTRUCTION);
            /**
             * Jede empfangene JSON muss ein Sudoku beinhalten, welches in einem "normalen" Array gespeichert wird
             */
            if (myJSONObject.containsKey(Constants.SUDOKU)) {
                JSONArray sudokuArray = (JSONArray) myJSONObject.get(Constants.SUDOKU);
                sudoku = new int[sudokuArray.size()];
                for (int i = 0; i < sudokuArray.size(); i++) {
                    sudoku[i] = Integer.valueOf(String.valueOf((sudokuArray.get(i))));
                }
            } else {
                Core.logger.info("noSudoku!");
                eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "answering: " + solvedResponses[3]);
                return answerJSON(request, solvedResponses[3], sudoku);
            }
        } catch (ParseException e) {
            Core.logger.warning("Parser: " + e.toString());
        }

        /**
         * Welche Instruktion wurde per E-Mail übergeben und welche JSON soll als Rückantwort gegeben werden
         */
        switch (instruction) {
            case "ping":
                eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "answering: pong");
                return answerJSON(request, "pong", sudoku);
            case "pong":
                Core.logger.fine("Message Ignored: " + instruction);
                eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, Constants.PONG + request);
                return "ERROR pong received";
            case "solve":
                try {
                    SudokuSolver sudokuSolver = new SudokuSolver(sudoku);
                    int solution = sudokuSolver.search();
                    Core.logger.fine("Solution:  " + solvedResponses[solution]);
                    eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "answering: " + solvedResponses[solution]);
                    return answerJSON(request, solvedResponses[solution], sudoku);
                } catch (Exception e) {
                    Core.logger.info(e.toString());
                    e.printStackTrace();
                    eventBus.publish(Constants.BUS_ADDRESS_INTERNAL, name + "answering: " + solvedResponses[3]);
                    return answerJSON(request, solvedResponses[3], sudoku);
                }
            default:
                Core.logger.fine("Message Ignored: " + instruction);
                return "ERROR message ignored: " + instruction;
        }
    }
}