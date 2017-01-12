from SudokuInitializer import *
import random

def removeNumbersInRow(sudoku,row,k=3,setSize=2):
    sudokuSize=k*k
    setSize = random.randint(1,setSize)
    randoms = [0]*setSize

    for i in range(0,setSize):
        randoms[i] = random.randint(1,sudokuSize)

    for i in range(0,setSize):
        for j in range(0,sudokuSize):
            if(sudoku[row][j] == randoms[i]):
                sudoku[row][j] = 0
                break

    return sudoku

def removeNumbers(sudoku,k=3,setSize=2):
    sudokuSize = k*k
    row = random.randint(0,sudokuSize-1)

    #while sudoku solvable
    return removeNumbersInRow(sudoku,row,k,setSize)

def generateSudoku(sudoku,k=3,level=2):
    sudokuSize = k*k
    generateFilledSudoku(sudoku, k)

    # Den if-else-Block kann man noch kürzen aber bevor der Solver steht änder ich mal noch nichts dran.
    # Wir können uns dann auch leichter über das Verfahren einigen und ob der Schwierigkeitsgrad so passt!
    if(level == 1):
        # In jedem Block 1-2 Zahlen entfernen
        sudoku = removeNumbers(sudoku,k,setSize=2)
    elif(level == 2):
        # In jedem Block die hälfte aller Zahlen entfernen
        sudoku = removeNumbers(sudoku,k,setSize=3)
    else:
        # In jedem Block so viel wie möglich entfernen
        sudoku = removeNumbers(sudoku,k,setSize=4)

    return sudoku
