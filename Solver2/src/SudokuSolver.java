import java.util.Arrays;


public class SudokuSolver {
	
	
	static boolean solved = false;
	static boolean solvedOnce = false;
	
	static int[][] firstSolution;
	
	static boolean[][] zeilen;
	static boolean[][] spalten;
	static boolean[][] felder;

	
	static int n;
	static int k;
	
	static int I = 0;
	static int J = 0;

	static int init(int[][] sudoku){
		n = sudoku.length;
		k = (int)Math.sqrt(n);
		
		zeilen = new boolean[n][n];
		spalten = new boolean[n][n];
		felder = new boolean[n][n];
	
		firstSolution = new int[n][n];
		
		for(int i = 0; i < n; i++){	
			for(int j = 0; j < n; j++){
				
				if(sudoku[i][j] == 0) continue;
				
				zeilen[i][sudoku[i][j]-1] = true;
				spalten[j][sudoku[i][j]-1] = true;
				felder[j/k + i/k * k][sudoku[i][j]-1] = true;
			
			}
		}
		
		if(solve(sudoku)){
			return 2;
		}
		else if(solvedOnce){
			sudoku = firstSolution;
			return 1;
		}
		else{
			return 0;
		}
	}
	
static boolean solve(int[][] sudoku){
		
		solved = true;

		for(int i = I; i < n; i++){
			for(int j = J; j < n; j++){
				
				if(sudoku[i][j] != 0) continue;
				
				solved = false;
				I = i; J = j % (k-1);
				
				for(int l = 0; l < n; l++){
					
					
					if(spalten[j][l] || zeilen[i][l] || felder[j/k + i/k * k][l]) continue;
					
					
					sudoku[i][j] = l+1;
					zeilen[i][l] = true;
					spalten[j][l] = true;
					felder[j/k + i/k * k][l] = true;

					solve(sudoku);

					I = i; J = j;
					
					if(!solved){ 
						zeilen[i][l] = false;
						spalten[j][l] = false;
						felder[j/k + i/k * k][l] = false;
						sudoku[i][j] = 0;
					}
					
					else if(solvedOnce){
						return true;
					}
					else if(!solvedOnce){
						solvedOnce = true;
						
						for(int p = 0; p < n; p++){
							firstSolution[p] = sudoku[p].clone();
						}
						
						zeilen[i][l] = false;
						spalten[j][l] = false;
						felder[j/k + i/k * k][l] = false;
						sudoku[i][j] = 0;
						
						solved = false;
						return false;

					}
				}
				return false;
			}
		}
		return true;
	}
}