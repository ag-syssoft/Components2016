package gui0;
import com.google.gson.Gson;

public class MyMessage {

	private String request_id;
	@SuppressWarnings("unused")
	private String sender;
	private int sudoku[];
	private String instruction;

	public MyMessage() {
		this.request_id = java.util.UUID.randomUUID().toString();

	}

	public String getRequestId() {
		return this.request_id;
	}

	public void setSender(String uri) {
		this.sender = uri;
	}
	
	public String getSender() {
		return this.sender;
	}

	public void setSudoku(int[][] sudoku) {
		this.sudoku = new int[sudoku.length * sudoku[0].length];
		for (int i = 0; i < sudoku.length; i++) {
			for (int j = 0; j < sudoku[0].length; j++) {
				this.sudoku[(i * sudoku.length) + j] = j;
			}
		}
	}
	
	public void setSudoku(int[] sudoku) {
		this.sudoku = sudoku;
	}
	
	public int[] getSudoku() {
		return this.sudoku;		
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}
	
	public String getInstruction() {
		return this.instruction;
	}

	public void generateDummy(int n) {
		this.setSender("myUri");
		this.setInstruction("register:gui");
		
		int[][] sudoku = new int[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				sudoku[i][j] = i + j;
			}
		}

		this.setSudoku(sudoku);

	}

	public String toJSON() {

		Gson gson = new Gson();
		String json = gson.toJson(this);

		return json;
	}

}
