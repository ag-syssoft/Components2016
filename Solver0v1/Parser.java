import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class Parser {

	static String sender = "";
static final String[] solvedResponses = {"solved:impossible","solved:one","solved:many","solved:illegal"};
	public static String parse(String input) {

		/* Test fuer korrekten Empfang */
		MainApp.logger.warning("Input:\n" + input);

		/* Dokument ueberpruefen. Erst einfache Ueberpruefung */
		String request = "";
		// String sender = "";
		String instruction = "";
		int[] sudoku = {};
		String modInput = input.replace("|", "").replace(" ", "").replace("\r", "").replace("\n", "").replace("\t", "");

		JSONParser parser = new JSONParser();
System.out.println("11");
		try {
			System.out.println("22");
			Object object = parser.parse(modInput);

			JSONObject myJSONObject = (JSONObject) object;
			request = (String) myJSONObject.get(MainApp.REQUEST_ID);
			// sender = (String) myJSONObject.get("sender");
			System.out.println("33");
			instruction = (String) myJSONObject.get(MainApp.INSTRUCTION);
			if (myJSONObject.containsKey(MainApp.SUDOKU)) {
				JSONArray sudokuArray = (JSONArray) myJSONObject.get(MainApp.SUDOKU);
				sudoku = new int[sudokuArray.size()];
				for (int i = 0; i < sudokuArray.size(); i++) {
					sudoku[i] = Integer.valueOf(String.valueOf((sudokuArray.get(i))));
				}
			} else {
				MainApp.logger.info("noSudoku!");
				return answerJSON(request, "solved:illegal", sudoku);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Debug
		// MainApp.logger.info("Input:");
		// MainApp.logger.info("request=" + request);
		// MainApp.logger.info("sender=" + sender);
		// MainApp.logger.info("instruction=" + instruction);
		// MainApp.logger.info("sudoku=" + Arrays.toString(sudoku));

		switch (instruction) {
		case "ping":
			// Antworte mit pong
			return answerJSON(request, "pong", sudoku);

		case "pong":
			MainApp.logger.fine("Message Ignored: " + instruction);
			if (request.equals(Pinger.lastID.toString())) {
				Pinger.pingReturned = true;
				Pinger.lastID = "";
			}
			return "ERROR";
		case "solve":
			try {
				SudokuSolver sodokuSolver = new SudokuSolver(sudoku);
				int solution = sodokuSolver.search();
				MainApp.logger.fine("Solution:  " + solvedResponses[solution]);
				return answerJSON(request, solvedResponses[solution], sudoku);
			} catch (Exception e) {
				MainApp.logger.info(e.toString());
				e.printStackTrace();
				return answerJSON(request, solvedResponses[3], sudoku);
			}
		default:
			MainApp.logger.fine("Message Ignored: " + instruction);
			// Rest interessiert uns nicht
			return "ERROR";
		}
	}

	public static String answerJSON(String request, String instruction, int[] sudoku) {
		/*
		 * Sender muss geupdated werden für Antwort (URI muss noch gemachr
		 * werden..). Instruction wurde bereits geupdated. GUID (request-id)
		 * bleibt (?). Sudoku Feld ist entweder null oder bereits durch
		 * Solve-Case geupdatet.
		 */

		String answer = "";

		if (sender.isEmpty()) {
			System.err.println("Bitte zuerst die SenderURI über Setter-Methode festlegen!");

		} else {
			// MainApp.logger.info(sender);
			JSONObject object = new JSONObject();
			object.put(MainApp.REQUEST_ID, request);
			object.put(MainApp.SENDER, sender);
			object.put(MainApp.INSTRUCTION, instruction);

			if (sudoku.length != 0) {
				JSONArray sudokuArray = new JSONArray();
				for (int i = 0; i < sudoku.length; i++) {
					sudokuArray.add(sudoku[i]);
				}
				object.put(MainApp.SUDOKU, sudokuArray);
			}

			answer = object.toJSONString();

			// Debug
			// MainApp.logger.info("Answer:");
			// MainApp.logger.info(answer);
		}
		String answer2 = answer.replace("\\", "");
		MainApp.logger.severe("Output: " + answer2);
		return answer2;

	}

	public static void setSender(String senderURI) {
		if (senderURI.isEmpty()) {
			System.err.println("Der als Parameter übergebene String ist leer!");
		} else {
			sender = senderURI;
		}
	}

	public static String getSender() {
		return sender;
	}

}