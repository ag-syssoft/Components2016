import random
import numpy
import math

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
def generateFilledSudoku(k=3):

    sudoku = generateInitialSudoku(k=k)

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

def emptyField(sudoku,numbersToRemove):
    k = int(math.sqrt(len(sudoku)))
    sudokuSize = k*k
    cleanedNumbers = []
    
    for i in range(0,numbersToRemove):
        row , index, init = 0,0,1
        while sudoku[row][index] == 0 or init == 1:
            row = random.randint(0,sudokuSize-1)
            index =  random.randint(0,sudokuSize-1)
            init = 0

        sudoku[row][index] = 0
        cleanedNumbers.extend([(row,index)])
    
    return sudoku, cleanedNumbers
