from Message import *
from SudokuGenerator import *
import math

class Handler():
    global reqDictionary
    reqDictionary={
        #'reqID123': (difficulty, prevState)
    }


    def handle(msg):
        if(msg.instruction.startswith("generate")):
            k = int(math.sqrt(len(msg.sudoku)))
            difficulty = msg.instruction[-2:-1]
            sudoku = generateSudoku(generateFilledSudoku(k),k)
            reqDictionary[msg.requestID] = (difficulty, sudoku)
            #TODO Felder leeren

            #TODO Camel-Nachicht schicken

        elif(msg.instruction == "solved: one"):
            tmpDifficulty = reqDictionary[msg.requestID][0]
            # now we need the number of 'empty' fields
            emptyCounter = 0
            for l in msg.sudoku:
                for i in l:
                    if i == 0:
                        emptyCounter++
            # and check if we are done or if need still need to 'empty' fields
            if tmpDifficulty == "1" and emptyCounter > 7:

            if tmpDifficulty == "2" and emptyCounter > 10:

            if tmpDifficulty == "3" and emptyCounter > 13:

            print("Test")
        elif(msg.instruction == "solved: many"):
            print("Test")
        else:
            print("Error")

    if __name__ == "__main__":

        #Dump-Message
        requestID   = "12345"
        sender      = "sender"
        instruction = "generate[difficulty:1]"
        sudoku      = [[0]*9]*9
        message = Message(requestID,sender,instruction,sudoku)

        handle(message)
