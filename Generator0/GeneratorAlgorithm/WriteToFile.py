import time
from SudokuGenerator import *

sudoku  = generateInitialSudoku(k=3)
sudoku  = generateSudoku(sudoku,k=3,level=3)

filename = time.strftime("%d_%m_%Y___%H_%M_%S")

destFile = open('../transfer/input/' + filename + '.txt', 'w')
destFile.write(str(sudoku))
destFile.close()
