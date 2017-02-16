package SolverGID.SolverAID;

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
		MainApp.logger.info("Register Run");
		ProducerTemplate template;
		try {
			template = main.getCamelTemplate();
			template.sendBody(MainApp.broker0URI, register ? register() : unregister());
			// template.sendBody(MainApp.broker0, unregister());
			if (!register) {
				MainApp.logger.fine("---Unregistered, exit!---");
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
		object.put(MainApp.REQUEST_ID, id.toString());
		object.put(MainApp.SENDER,
				MainApp.solver0URI);
		object.put(MainApp.INSTRUCTION, "register:solver");
		JSONArray sudokuArray = new JSONArray();
		sudokuArray.add(0);
		object.put(MainApp.SUDOKU, sudokuArray);
		String answer = object.toJSONString();
		String answer2 = answer.replace("\\", "");
		MainApp.logger.severe("Register: " + answer2);
		return answer2;

	}

	static String unregister() {
		UUID id = UUID.randomUUID();
		JSONObject object = new JSONObject();
		object.put(MainApp.REQUEST_ID, id.toString());
		object.put(MainApp.SENDER,
				MainApp.solver0URI);
		object.put(MainApp.INSTRUCTION, "unregister");
		JSONArray sudokuArray = new JSONArray();
		sudokuArray.add(0);
		object.put(MainApp.SUDOKU, sudokuArray);
		String answer = object.toJSONString();
		String answer2 = answer.replace("\\", "");
		MainApp.logger.severe("Unregister: " + answer2);
		return answer2;

	}
}
