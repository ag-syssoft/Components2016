package gui0.tests;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import gui0.MyMessage;

public class GSONTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MyMessage m = new MyMessage();
		
		m.generateDummy(5);
		
		System.out.println(m.toJSON());
		
		Gson gson = new Gson();

		
		try {
		MyMessage xx = gson.fromJson("{request-id:0000}", MyMessage.class);
		System.out.println(xx.getRequestId());
		} catch (JsonSyntaxException ex) {
			System.out.println("Proj");
			
		}
		
		
	}

}
