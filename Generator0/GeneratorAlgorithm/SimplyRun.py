from SudokuGenerator import *

sudoku = generateInitialSudoku(k=3)
sudoku = generateSudoku(sudoku,k=3,level=3)
print(formattedString(sudoku,k=3))
