import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonObject;

public class TestBroker {
	
	public TestBroker() throws RemoteException, NotBoundException{
		Registry registryIn = LocateRegistry.getRegistry(Solver.mainSolver.uri, Registry.REGISTRY_PORT);
		Receiver receiver = (Receiver) registryIn.lookup("Solver");
		
		JsonObject auftrag = Json.createObjectBuilder()
				.add("request-id", "ABCDTEST")
				.add("sender", "file://ausgabe")
				.add("instruction", "solve")
				.add("sudoku", "[" + Arrays.deepToString(new int[][]{
					{0,1,2,7,0,0,0,0,0},
					{0,7,0,6,0,8,0,0,0},
					{0,0,0,0,0,0,8,3,0},
					{0,0,8,0,0,7,0,0,0},
					{0,0,0,0,0,0,3,5,0},
					{0,0,6,0,0,4,0,0,0},
					{2,4,0,0,0,0,0,0,0},
					{0,0,0,0,5,0,0,1,6},
					{1,0,0,0,8,0,9,0,0}}).replace("[","").replace("]",  "") + "]")
				.build();
		
		receiver.send(auftrag.toString()); //JSON String
	}
}