from Message import *
from SudokuGenerator import *
import math

class Handler():
    global reqDictionary
    reqDictionary={}

    def handle(msg):
        if(msg.instruction.startswith('generate')):
            k = int(math.sqrt(len(msg.sudoku)))
            reqDictionary[msg.requestID] = msg.instruction[-2:-1]
            #schwierigkeit später und evtl nur eine Zahl entfernen.
            sudoku = generateSudoku(generateFilledSudoku(k),k)
            #TODO apache camel message
        elif(msg.instruction == "solved: one"):
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