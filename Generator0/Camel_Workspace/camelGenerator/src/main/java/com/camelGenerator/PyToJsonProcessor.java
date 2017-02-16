package com.camelGenerator;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

/*
 * Für dieses import waren zwei neue maven-pom dependencies notwendig:
 * 
 * - javax.json
 *   http://stackoverflow.com/questions/18346609/how-can-i-import-javax-json-in-eclipse)
 * - org.glassfish
 *   http://stackoverflow.com/questions/18282872/why-json-test-program-doesnt-work
 */
import javax.json.Json;
import javax.json.JsonArrayBuilder;

public class PyToJsonProcessor implements Processor
{
	
	/*
	 * Das können wir später auch extern, zb mit JavaScript, erledigen.
	 * Aber für den Anfang...
	 * 
	 * JS-Processor Beispiel: https://www.youtube.com/watch?v=N9LmTHY6Ppc
	 */
	
	/*
	 * Funktionsweise:
	 *  - Konvertierung durch Manipulation des Exchange Objektes in der process-Methode.
	 *    "An Exchange is the message container holding the information during the entire routing of a Message received by a Consumer."
	 *  - Das Exchange Objekt enthält ein Message Objekt welches als "Input" für den Prozessor fungiert (Exchange.getIn())
	 *  - ... und ein weitere Message Objekt welches als "Output" fungiert (Exchange.setOut().
	 *  - Lt. Doku empfiehlt es sich, wg der im Message-Objekt gespeicherten Meta Informationen kein neues Message Objekt anzulegen sondern lediglich das bestehende zu modifizieren,
	 * 
	 * http://camel.apache.org/processor.html
	 * https://camel.apache.org/maven/current/camel-core/apidocs/org/apache/camel/Exchange.html
	 * https://camel.apache.org/maven/current/camel-core/apidocs/org/apache/camel/Message.html
	 */

	@Override
	public void process(Exchange exchange) throws Exception
	{
		Message toProcess = exchange.getIn().copy();
		String zmqString = toProcess.getBody(String.class); //https://camel.apache.org/maven/current/camel-core/apidocs/org/apache/camel/Message.html#getBody(java.lang.Class)
		String [] msgArray = zmqString.split(";");
		
		String json = Json.createObjectBuilder()
				.add("request_id", msgArray[0])
				.add("sender", msgArray[1])
				.add("instruction", msgArray[2])
				.add("sudoku", sudokuConverter(msgArray[3]))
				.build()
				.toString();
		
		toProcess.setBody(json);
		exchange.setOut(toProcess);
	}
	
	private JsonArrayBuilder sudokuConverter(String pyString)
	{
		JsonArrayBuilder sudokuJSON =  Json.createArrayBuilder();
		pyString = pyString.replace("[", "").replace("]", "").replace(" ", "");
		String[] pyArray = pyString.split(",");
		for (int indexerA = 0; indexerA < pyArray.length; indexerA++)
		{
			sudokuJSON.add(Integer.valueOf(pyArray[indexerA]));
		}
		return sudokuJSON;
	}

}
