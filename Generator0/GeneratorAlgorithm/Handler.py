from Message import *
from SudokuGenerator import *
import math

## Note
## Zur Zeit akzeptieren wir nur Sudokus die mindestes 9 * 9 Felder besitzen.
## Kleiner macht aus vielerlei Gründen keinen Sinn, hier ist uns aber nur wichtig,
## dass wir zu beginn eine fixe Zahl an Werten aus dem Sudoku löschen können.

class Handler():
    global reqDictionary
    reqDictionary={
        #'reqID123': (difficulty, finishedState, cleanedNumbers)
    }


    def handle(msg):
        # instruction: generate
        if(msg.instruction.startswith("generate")):
            k = int(math.sqrt(len(msg.sudoku)))
            difficulty = msg.instruction[-2:-1]
            sudoku = generateSudoku(generateFilledSudoku(k),k)
            
            # initial cleanup (remove 8 numbers)
            sudoku, cleanedNumbers = emptyFields(sudoku,8)
            reqDictionary[msg.requestID] = (difficulty, sudoku, cleanedNumbers) 

            # TODO Camel-Nachicht schicken

        # instruction: solved one
        elif(msg.instruction == "solved: one"):
            k = int(math.sqrt(len(msg.sudoku)))
            tmpDifficulty = reqDictionary[msg.requestID][0]
            
            # now we need the number of 'empty' fields
            emptyCounter = 0
            for row in msg.sudoku:
                for elem in row:
                    if elem == 0:
                        emptyCounter++

            percentCounter = (emptyCounter * 100) / (k*k)
                        
            # and check if we are done or if need still need to 'empty' fields
            if (tmpDifficulty == "1" and percentCounter < 0.7) or (tmpDifficulty == "2" and percentCounter < 0.5)
                or (tmpDifficulty == "3" and percentCounter < 0.3):
                # TODO send sudoku to broker (finished for GUI)
                
            # remove numbers
            sudoku, cleanedNumbers = emptyField(sudoku,1)
            (difficulty, finishedSudoku, oldNumbers) = reqDictionary[msg.requestID]
            cleanedNumbers = oldNumbers.extend(cleanedNumbers)
            reqDictionary[msg.requestID] = (difficutly, finishedSudoku, cleanedNumbers)
            
            # TODO send to broker (request to solver)

        # instruction: solved many
        elif(msg.instruction == "solved: many"):
            # recover previous state
            lastNumber = reqDictionary[msg.requestID][2].pop()
            (difficulty, finishedSudoku, oldNumbers) = reqDictionary[msg.requestID]
            sudoku = msg.sudoku
            sudoku[lastNumber[0]][lastNumber[1]] = finishedSudoku[lastNumber[0]][lastNumber[1]]

            # remove numbers
            sudoku, cleanedNumbers = emptyField(sudoku,1)
            cleanedNumbers = oldNumbers.extend(cleanedNumbers)
            reqDictionary[msg.requestID] = (difficutly, finishedSudoku, cleanedNumbers)

            # TODO send to broker (request to solver)
        else:
            print("Some error occured!")

    if __name__ == "__main__":

        #Dump-Message
        requestID   = "12345"
        sender      = "sender"
        instruction = "generate[difficulty:1]"
        sudoku      = [[0]*9]*9
        message = Message(requestID,sender,instruction,sudoku)

        handle(message)
