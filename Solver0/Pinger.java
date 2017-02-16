package SolverGID.SolverAID;

import java.util.TimerTask;
import java.util.UUID;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.main.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Pinger extends TimerTask {
	public static String lastID = null;
	public static boolean pingReturned = true;
	Main main;

	public Pinger(Main main) {
		this.main = main;
	}

	@Override
	public void run() {
		MainApp.logger.info("Pinger on run");
		if (pingReturned) {
			ProducerTemplate template;
			try {
				template = main.getCamelTemplate();
				template.sendBody(MainApp.broker0URI, Pinger.pingBody());
				// template.sendBody(MainApp.broker0, unregister());
			} catch (Exception exce) {
				// TODO Auto-generated catch block
				exce.printStackTrace();
			}
		} else {
			MainApp.logger.info("No PingReturned!");
			//TODO unregister, restart
		}
		pingReturned = false;
	}

	static String pingBody() {
		UUID id = UUID.randomUUID();
		lastID = id.toString();
		JSONObject object = new JSONObject();
		object.put(MainApp.REQUEST_ID, id.toString());
		object.put(MainApp.SENDER, MainApp.solver0URI);

		object.put(MainApp.INSTRUCTION, "ping");
		JSONArray sudokuArray = new JSONArray();
		sudokuArray.add(0);
		object.put(MainApp.SUDOKU, sudokuArray);

		String answer = object.toJSONString();
		String answer2 = answer.replace("\\", "");
		MainApp.logger.severe("Ping: " + answer2);
		return answer2;

	}
}
