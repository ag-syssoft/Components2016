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
		boolean restart = false;
		try {
			ProducerTemplate template = main.getCamelTemplate();
			if (pingReturned) {			
				template.sendBody(MainApp.broker0URI, Pinger.pingBody());
			} else {
				restart = true;
				if(main.isStarted()){
				MainApp.logger.info("No PingReturned!");
				template.sendBody(MainApp.broker0URI, RegisterThread.register());
				} else {
					MainApp.logger.info("No Ping, main not started!");
					main.stop();
					MainApp.logger.info("Main stopped, starting new CamelThread");
					main.addRouteBuilder(new MyRouteBuilder());
					CamelThread camelThread = new CamelThread(main, (String[])null);
					camelThread.start();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MainApp.logger.info("Pinger run " + e.toString());
			if(restart){
				MainApp.logger.info("Pinger restart");
				try {
					main.stop();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				main.addRouteBuilder(new MyRouteBuilder());
				CamelThread camelThread = new CamelThread(main, (String[])null);
				camelThread.start();
				restart = false;
			}
		}
		pingReturned = false;
	}

	static String pingBody() {
		UUID id = UUID.randomUUID();
		lastID = id.toString();
		JSONObject object = new JSONObject();
		object.put(MainApp.REQUEST_ID, id.toString());
		object.put(MainApp.SENDER, MainApp.solver0URIout);

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
