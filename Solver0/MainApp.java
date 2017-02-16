package SolverGID.SolverAID;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Timer;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.main.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A Camel Application
 */
public class MainApp {
	/**
	 * A main() so we can easily run these routing rules in our IDE
	 */

	static final String broker0URI = "rabbitmq://136.199.51.111/inExchange?username=kompo&password=kompo&skipQueueDeclare=true";
	static final String generator0URI = "restlet:http://136.199.26.133:80/api/message?restletMethod=post";
	static final String solver0URI = "smtps://smtp.gmail.com?username=DUMMYMAIL&password=DUMMYWERT&subject="
			+ "Solver" + "&to=" + "komposolver@gmail.com" + "&from=Broker0";
	static Logger logger;
	public static void main(String... args) throws Exception {
		  logger = Logger.getLogger("MyLog");  
		    FileHandler fh;  

		    try {  
		    	fh = new FileHandler("LogFile.txt");  
		        logger.addHandler(fh);
		        SimpleFormatter formatter = new SimpleFormatter();  
		        fh.setFormatter(formatter);  
		        logger.setLevel(Level.FINE);
		        logger.fine("---- Log Init ----");  
		    } catch (SecurityException e) {  
		        e.printStackTrace();  
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    }  

		
		
		
		Parser.setSender(solver0URI);
		Main main = new Main();

		main.addRouteBuilder(new MyRouteBuilder());
		CamelThread camelThread = new CamelThread(main, args);
		camelThread.start();

		RegisterThread registerThread = new RegisterThread(main);
		registerThread.start();
		RegisterThread unregisterThread = new RegisterThread(main, false);
		JPanel jpanel = new JPanel();
		JButton jButton = new JButton("Unregister");
		jButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				unregisterThread.start();
			}
		});
		
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new Pinger(main), 4*60000,4*60000);
		
		JButton jButtonTest = new JButton("Testen");

		jButtonTest.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ProducerTemplate template;
				try {
					template = main.getCamelTemplate();
					template.sendBody(MainApp.broker0URI, testBody());
					// template.sendBody(MainApp.broker0, unregister());
				} catch (Exception exce) {
					// TODO Auto-generated catch block
					exce.printStackTrace();
				}

			}
		});
		JButton jButtonPing = new JButton("Ping");

		jButtonPing.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ProducerTemplate template;
				try {
					template = main.getCamelTemplate();
					template.sendBody(MainApp.broker0URI, Pinger.pingBody());
					// template.sendBody(MainApp.broker0, unregister());
				} catch (Exception exce) {
					// TODO Auto-generated catch block
					exce.printStackTrace();
				}

			}
		});
		jpanel.add(jButton);
		jpanel.add(jButtonTest);
		jpanel.add(jButtonPing);
		JFrame jFrame = new JFrame();
		jFrame.add(jpanel);
		jFrame.pack();
		jFrame.setVisible(true);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// main.run(args);

		// send to default endpoint

	}
	static final String REQUEST_ID ="request-id";
	static final String SENDER = "sender";
	static final String INSTRUCTION ="instruction";
	static final String SUDOKU = "sudoku";
	static String testBody() {
		UUID id = UUID.randomUUID();
		JSONObject object = new JSONObject();
		object.put(REQUEST_ID, id.toString());
		object.put(SENDER,
				solver0URI);

		object.put(INSTRUCTION, "solve");

		JSONArray sudokuArray = new JSONArray();
		String sudoko = "0,0,0,25,1,2,0,5,0,4,25,7,0,9,8,7,6,17,19,24,21,16,11,0,0";
		String soduko = "0,0,0,0,1,0,7,5,0,7,4,8,6,0,0,3,0,1,3,0,0,0,9,4,0,0,0,1,0,4,0,0,6,5,0,7,5,9,0,0,7,0,0,3,0,0,0,0,0,4,8,0,0,0,4,3,0,1,0,0,0,0,2,9,0,2,0,0,0,6,4,0,8,0,7,0,5,0,9,1,0";
		String[] tokens = sudoko.split(",");
		for(int j = 0; j<25;j++){
		for (int i = 0; i < tokens.length; i++) {
			try {
				sudokuArray.add(Integer.parseInt(tokens[i]));
			} catch (Exception e) {
				sudokuArray.add(0);
			}
		}
		}

		object.put(SUDOKU, sudokuArray);

		String answer = object.toJSONString();
		String answer2 = answer.replace("\\", "");
		logger.severe("Test: " +answer2);
		return answer2;

	}

	

}
