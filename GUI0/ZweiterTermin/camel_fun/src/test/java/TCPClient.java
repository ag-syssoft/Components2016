package test.java;

import java.io.*;
import java.net.*;

class TCPClient {
	public static void main(String argv[]) throws Exception {
		String sentence;
		String modifiedSentence;

		// Socket erstellen
		Socket clientSocket = new Socket("192.168.56.101", 80);
		
		// In- und Out- Channel erzeugen
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		// Versenden und Empfangen
		sentence = inFromUser.readLine();
		outToServer.writeBytes(sentence + '\n');
		
		modifiedSentence = inFromServer.readLine();
		System.out.println("FROM SERVER: " + modifiedSentence);
		clientSocket.close();
	}
}