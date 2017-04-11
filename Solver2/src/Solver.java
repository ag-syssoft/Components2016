
import java.io.StringReader;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.rmi.RmiEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.restlet.data.MediaType;

public class Solver implements Receiver{
	static Solver mainSolver;
	public Sender sender;
	public JsonObject cur;
	public String uri = "localhost";
	public String broker = "file://ausgabe";
	public CamelContext context;
	
	
	public static void main(String[] args) throws Exception{
		new Solver();
		new TestBroker();
	}
	
	public Solver() throws Exception{
		Solver.mainSolver = this;
		context = new DefaultCamelContext();

		Registry registryIn = LocateRegistry.createRegistry(Registry.REGISTRY_PORT), registryOut = LocateRegistry.createRegistry(Registry.REGISTRY_PORT+1);
		
		RouteBuilder in = new RouteBuilder(){
        	@Override
        	public void configure() throws Exception{
        		RmiEndpoint endpoint = (RmiEndpoint) endpoint("rmi://" + uri + ":" + Registry.REGISTRY_PORT + "/Solver");
        		endpoint.setRemoteInterfaces(Receiver.class);
        		from(endpoint).process(new Converter(true)); //Receive, Invoke method through processor
        	}
        };
        context.addRoutes(in);
		context.start();
		register(Solver.mainSolver.broker);
		
		System.out.println("Solver started.");
	}

	@Override
	public void send(String x) throws RemoteException {}
	
	public void register(String addr) throws Exception{
        RouteBuilder out = new RouteBuilder(){
        	@Override
        	public void configure() throws Exception{
        		RmiEndpoint endpoint = (RmiEndpoint) endpoint("rmi://" + uri + ":" + (Registry.REGISTRY_PORT+1) + "/RMISend");
        		endpoint.setRemoteInterfaces(Sender.class);
        		from(endpoint).process(new Converter(false)).to(broker); //Send
        	}
        };

        context.addRoutes(out);
        Registry registryOut= LocateRegistry.getRegistry(uri, Registry.REGISTRY_PORT+1);
		sender = (Sender) registryOut.lookup("RMISend");
		
		JsonObject jsonObj = Json.createObjectBuilder()
		.add("request_id", "0")
		.add("sender", "rmi://" + Solver.mainSolver.uri + ":" + Registry.REGISTRY_PORT + "/Solver")
		.add("instruction", "register:solver")
		.build();
		sender.send(jsonObj.toString());
	}
}

class Converter implements Processor{
	private boolean receiver;
	
	public Converter(boolean receiver){
		this.receiver = receiver;	
	}
	@Override
	public void process(Exchange exchange) throws Exception {

		exchange.getIn().setBody(exchange.getIn().getBody(String.class)); //Convert Bean Invocation
		
		if(receiver){
			String rec = exchange.getIn().getBody(String.class);
			
			JsonReader jsonReader = Json.createReader(new StringReader(rec));
			JsonObject jsonObj = jsonReader.readObject();
			jsonReader.close();
			
			

			if(jsonObj.containsKey("request-id") && jsonObj.containsKey("sender") && jsonObj.containsKey("instruction") && Solver.mainSolver.cur == null)
				Solver.mainSolver.cur = jsonObj;
			else
				return;
			

			System.out.println("Solver has received:\n" + rec + "\n ---- \n");
			
			Solver.mainSolver.broker = jsonObj.getString("sender").toString().replace("\"", "");
			
			if(jsonObj.get("instruction").toString().replace("\"", "").startsWith("ping")){
				jsonObj = Json.createObjectBuilder()
						.add("request_id", jsonObj.get("request-id").toString().replace("\"", ""))
						.add("sender", "rmi://" + Solver.mainSolver.uri + ":" + Registry.REGISTRY_PORT + "/Solver")
						.add("instruction", "pong")
						.build();
				
				Solver.mainSolver.sender.send(jsonObj.toString());
			}
			
			if(jsonObj.get("instruction").toString().replace("\"", "").equals("solve"))
			{
				int[][] sudokuFeld = null;
				if (jsonObj.containsKey("sudoku")) {
					try {
						String sudoku = jsonObj.get("sudoku").toString().replace(" ", "").replace("[", "").replace("]", "").replace("\"", "");
						String[] values = sudoku.split(","); // Klammernentfernen
						int size = (int) Math.sqrt(values.length);
						sudokuFeld = new int[size][size];
						for (int i = 0; i < size; i++)
							for (int e = 0; e < size; e++)
								sudokuFeld[i][e] = Integer.parseInt(values[i * size + e]);
					} catch (Exception e) {
						System.out.println("Corrupt sudoku Field");
					}
				}
				
				
				int count = SudokuSolver.init(sudokuFeld);
				
				if (count == 1) {
					jsonObj = Json.createObjectBuilder()
							.add("request_id", jsonObj.get("request-id").toString().replace("\"", ""))
							.add("sender", "rmi://" + Solver.mainSolver.uri + ":" + Registry.REGISTRY_PORT + "/Solver")
							.add("instruction", "solved:one")
							.add("sudoku", toIntArr(Arrays.deepToString(sudokuFeld).replace("[", "").replace("]", "")))
							.build();
				}else if(count == 2){
					jsonObj = Json.createObjectBuilder()
							.add("request_id", jsonObj.get("request-id").toString().replace("\"", ""))
							.add("sender", "rmi://" + Solver.mainSolver.uri + ":" + Registry.REGISTRY_PORT + "/Solver")
							.add("instruction", "solved:many")
							.add("sudoku", toIntArr(Arrays.deepToString(sudokuFeld).replace("[", "").replace("]", "")))
							.build();
				} else {
					jsonObj = Json.createObjectBuilder()
							.add("request_id", jsonObj.get("request-id").toString().replace("\"", ""))
							.add("sender", "rmi://" + Solver.mainSolver.uri + ":" + Registry.REGISTRY_PORT + "/Solver")
							.add("instruction", "solved:impossible")
							.add("sudoku", toIntArr(Arrays.deepToString(sudokuFeld).replace("[", "").replace("]", "")))
							.build();
				}

				Solver.mainSolver.sender.send(jsonObj.toString());
			}
			Solver.mainSolver.cur = null;
		}
		else{
			Message converted = exchange.getIn().copy();
			converted.setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            String body = (String)converted.getBody();
            converted.setHeader(Exchange.CONTENT_LENGTH,body.length());
            
            exchange.setOut(converted);
			System.out.println("Solver has sent:\n" + body);
		}
	}
	
	public JsonValue toIntArr(String x){
		JsonArrayBuilder f = Json.createArrayBuilder();
		JsonValue ret;
		String[] numberStrs = x.replace(" ", "").split(",");
		for(int i = 0;i < numberStrs.length;i++)
		{
		   f.add(Integer.parseInt(numberStrs[i]));
		}
		ret = f.build();
		return ret;
	}
}

interface Receiver extends Remote {
	public void send(String x) throws RemoteException;
}
interface Sender extends Remote {
	public void send(String x) throws RemoteException;
}