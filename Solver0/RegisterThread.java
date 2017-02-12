package comp.solver;

import java.util.UUID;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.main.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class RegisterThread extends Thread {
    Main main;
    boolean register;

    RegisterThread(Main main) {
        this.main = main;
        this.register = true;
    }

    RegisterThread(Main main, boolean register) {
        this.main = main;
        this.register = register;
    }

    @Override
    public void run() {
        if (register) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("asd");
        ProducerTemplate template;
        try {
            template = main.getCamelTemplate();
            template.sendBody(MainApp.broker0, register ? register() : unregister());
            if (!register) {
                System.exit(0);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String register() {
        UUID id = UUID.randomUUID();
        JSONObject object = new JSONObject();
        object.put("request-id", id.toString());
        object.put("sender", "smtps://smtp.gmail.com?username=&password=&subject="
                + "Solver" + "&to=" + "gmail.com" + "&from=Broker");

        object.put("instruction", "register:solver");

        JSONArray sudokuArray = new JSONArray();
        sudokuArray.add(0);

        object.put("sudoku", sudokuArray);

        String answer = object.toJSONString();
        String answer2 = answer.replace("\\", "");
        System.out.println(answer2);
        return answer2;

    }

    static String unregister() {
        UUID id = UUID.randomUUID();
        JSONObject object = new JSONObject();
        object.put("request-id", id.toString());
        object.put("sender", "smtps://smtp.gmail.com?username=&password=&subject="
                + "Solver" + "&to=" + "gmail.com" + "&from=Broker");

        object.put("instruction", "unregister");

        JSONArray sudokuArray = new JSONArray();
        sudokuArray.add(0);

        object.put("sudoku", sudokuArray);

        String answer = object.toJSONString();
        String answer2 = answer.replace("\\", "");
        System.out.println(answer2);
        return answer2;

    }
}

