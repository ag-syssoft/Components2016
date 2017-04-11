package test.java;

import java.io.*;
import java.net.*;

class TCPServer {
	public static void main(String argv[]) throws Exception {
		String clientSentence;
		String capitalizedSentence;

		System.out.println("MY IP address is " + InetAddress.getLocalHost().getHostAddress());

		// Socket erstellen
		ServerSocket welcomeSocket = new ServerSocket(8888);

		while (true) {
			// Auf Verbindung warten
			Socket connectionSocket = welcomeSocket.accept();

			// In- und Out- Channel erzeugen
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			// Empfangen und Versenden
			clientSentence = inFromClient.readLine();
			System.out.println("Received: " + clientSentence);
			capitalizedSentence = clientSentence.toUpperCase() + '\n';
			outToClient.writeBytes(capitalizedSentence);
		}
	}
}