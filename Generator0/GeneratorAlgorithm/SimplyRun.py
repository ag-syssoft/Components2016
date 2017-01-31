from SudokuGenerator import *

sudoku = generateInitialSudoku(k=3)
sudoku, list = emptyField(sudoku,17)
print(formattedString(sudoku))

reqDictionary={
    'ID1': ('diff', 'finishedSudoku', [(88,88),(99,99)])
}


sudoku, cleanedNumbers = emptyField(sudoku,1)
(difficulty, finishedSudoku, oldNumbers) = reqDictionary['ID1']
oldNumbers.extend(cleanedNumbers)
reqDictionary['ID1'] = (difficulty, finishedSudoku, oldNumbers)
print(oldNumbers)
sudoku, cleanedNumbers = emptyField(sudoku,1)
(difficulty, finishedSudoku, oldNumbers) = reqDictionary['ID1']
oldNumbers.extend(cleanedNumbers)
reqDictionary['ID1'] = (difficulty, finishedSudoku, oldNumbers)
print(oldNumbers)
sudoku, cleanedNumbers = emptyField(sudoku,1)
(difficulty, finishedSudoku, oldNumbers) = reqDictionary['ID1']
oldNumbers.extend(cleanedNumbers)
reqDictionary['ID1'] = (difficulty, finishedSudoku, oldNumbers)
print(oldNumbers)
sudoku, cleanedNumbers = emptyField(sudoku,1)
(difficulty, finishedSudoku, oldNumbers) = reqDictionary['ID1']
oldNumbers.extend(cleanedNumbers)
reqDictionary['ID1'] = (difficulty, finishedSudoku, oldNumbers)
print(oldNumbers)
sudoku, cleanedNumbers = emptyField(sudoku,1)
(difficulty, finishedSudoku, oldNumbers) = reqDictionary['ID1']
oldNumbers.extend(cleanedNumbers)
reqDictionary['ID1'] = (difficulty, finishedSudoku, oldNumbers)
print(oldNumbers)
print(reqDictionary['ID1'])
print(reqDictionary['ID1'][2].pop())

