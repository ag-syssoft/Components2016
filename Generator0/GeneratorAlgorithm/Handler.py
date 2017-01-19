from Message import *
from SudokuGenerator import *
import math

class Handler():
    global reqDictionary
    reqDictionary={}
    # requestIDs, difficulty, state
    db = [
            [["r1", "r2", "r3", "r4", "r8"], "1", [
                [1,2,3,4,5,6,7,8,9],
                [2,3,4,5,6,7,8,9,1],
                [3,4,5,6,7,8,9,1,2],
                [4,5,6,7,8,9,1,2,3],
                [5,6,7,8,9,1,2,3,4],
                [6,7,8,9,1,2,3,4,5],
                [7,8,9,1,2,3,4,5,6],
                [8,9,1,2,3,4,5,6,7],
                [9,1,2,3,4,5,6,7,8]
            ]],
            [["r5", "r6", "r7"], "2", [
                [1,2,3,4,5,6,7,8,9],
                [2,3,4,5,6,7,8,9,1],
                [3,4,5,6,7,8,9,1,2],
                [4,5,6,7,8,9,1,2,3],
                [5,6,7,8,9,1,2,3,4],
                [6,7,8,9,1,2,3,4,5],
                [7,8,9,1,2,3,4,5,6],
                [8,9,1,2,3,4,5,6,7],
                [9,1,2,3,4,5,6,7,8]
            ]]
    ]

    def handle(msg):
        if(msg.instruction.startswith("generate")):
            k = int(math.sqrt(len(msg.sudoku)))
            reqDictionary[msg.requestID] = msg.instruction[-2:-1]
            #schwierigkeit später und evtl nur eine Zahl entfernen.
            sudoku = generateSudoku(generateFilledSudoku(k),k)
            #TODO apache camel message
        elif(msg.instruction == "solved: one"):
            difficulty = "hello"
            for s in db:
                if msg.requestID in s.[0]:
                    difficulty = s.[1]

            # now we need the number of 'empty' fields
            emptyCounter = 0
            for l in msg.sudoku:
                for i in l:
                    if i == 0:
                        emptyCounter++

            # and check if we are done or if need still need to 'empty' fields
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
