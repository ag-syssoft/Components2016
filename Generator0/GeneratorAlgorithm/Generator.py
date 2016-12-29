import random
import numpy

def formattedString(sudoku,k=3):
    sudokuSize = k*k
    row = ""
    for i in range(0,sudokuSize):
        if(i > 0):
            row += "\n"
        if((i % k) == 0):
            row += "\n\n"

        for j in range(0,sudokuSize):
            if(sudoku[i][j] < 10):
                value = "0"+str(sudoku[i][j])
            else:
                value = str(sudoku[i][j])
            row += value+"  "

            if(((j+1) % k) == 0):
                row += "    "
    return(row)

def swapNumbers(sudoku, k=3):
    sudokuSize = k*k
    first  = random.randint(1,sudokuSize)
    second = random.randint(1,sudokuSize)

    while(first == second):
        second = random.randint(1,sudokuSize)

    for i in range(0,sudokuSize):
        ifst = sudoku[i].index(first)
        iscd = sudoku[i].index(second)
        sudoku[i][ifst], sudoku[i][iscd] = sudoku[i][iscd], sudoku[i][ifst]

    return sudoku

def swapRows(sudoku, k=3):
    block = random.randint(0,k-1)
    chosenColumn1 = block*k
    chosenColumn2 = chosenColumn1+random.randint(1,k-1)

    sudoku[chosenColumn1], sudoku[chosenColumn2] = sudoku[chosenColumn2], sudoku[chosenColumn1]

    return sudoku

def swapBlocks(sudoku, k=3):
    block1 = random.randint(0,k-1)
    block2 = random.randint(0,k-1)

    while(block1 == block2):
        block2 = random.randint(0,k-1)

    for i in range(0,k-1):
        sudoku[block1*k+i], sudoku[block2*k+i] = sudoku[block2*k+i], sudoku[block1*k+i]

    return sudoku

def rotateSudoku(sudoku, k=3):
    sudokuSize = k*k
    tmp = numpy.array(sudoku, int)
    rotated = numpy.rot90(tmp).tolist()

    for i in range(0,sudokuSize):
        sudoku[i] = rotated[i]

    return sudoku

# Standardfeld erzeugen
def generateInitialSudoku(k=3):
    sudokuSize = k*k
    offset = 0
    array = [[0]]*sudokuSize

    for i in range(0,sudokuSize):
        tmp = [0]*sudokuSize

        if(i % k == 0 and i > 0):
            offset += 1

        for j in range(0,sudokuSize):
            tmp[j] = ((j+offset) % (sudokuSize)) + 1

        array[i] = tmp
        offset = (offset+k) % sudokuSize

    return array

# Standardfeld modifizieren
def generateFilledSudoku(sudoku, k=3):
    for i in range(1,5):
        for j in range(0,random.randint(k,k*2)):
            if(i == 1):
                swapNumbers(sudoku, k)
            elif(i == 2):
                swapRows(sudoku, k)
            elif(i == 3):
                swapBlocks(sudoku, k)
            else:
                rotateSudoku(sudoku, k)
    return sudoku

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
