import math
from Message import *
from SudokuGenerator import *
from Bridge import *

## Note
## Zur Zeit akzeptieren wir nur Sudokus die mindestes 9 * 9 Felder besitzen.
## Kleiner macht aus vielerlei Gründen keinen Sinn, hier ist uns aber nur wichtig,
## dass wir zu beginn eine fixe Zahl an Werten aus dem Sudoku löschen können.

class Handler():
    global reqDictionary

    # TODO sender Address **hier oder in __init__**
    senderAddress = ""
    bridge= None

    """
    [key]   - reqID             - current request GUID
    [0]     - difficulty        - difficulty of sudoku
    [1]     - finishedState     - filled sudoku without empty fields
    [2]     - cleanedNumbers    - all index numbers of empty fields (in order of remove)
    [3]     - memorySet         - all index numbers of empty fields (for solved:many)
    [4]     - firstID           - GUID of the generate instruction (from gui)
    """
    reqDictionary={
        #'reqID123': (difficulty, finishedState, cleanedNumbers, memorySet, firstID)
    }


    def __init__(self, senderAddress, bridge):
        self.senderAddress = senderAddress
        self.bridge = bridge


    def getDictionary():
        return reqDictionary

    def handleGenerate(self, msg):
        print("start handleGenerate..")
        k = int(math.sqrt(len(msg.sudoku)))
        difficulty = msg.instruction[-1:]
        sudoku = generateFilledSudoku(k)

        # initial cleanup (remove 8 numbers)
        print("initial cleanup (remove 8 numbers)..")
        sudoku, cleanedNumbers = emptyField(sudoku,8)
        rID = Message.createGUID()
        reqDictionary[rID] = (difficulty, sudoku, cleanedNumbers, (set()), msg.requestID)

        # send message to camel
        msgToSend = Message(requestID=rID, senderAddress=self.senderAddress, instruction="solve", sudoku=sudoku)
        print("Send generated stuff")
        self.bridge.send(msgToSend)


    def handleSolvedOne(self, msg):
        print("start handleSolvedOne..")
        if (not (msg.requestID in reqDictionary)):
            print("unknown requestID...\nbreak")
            return

        k = int(math.sqrt(len(msg.sudoku)))
        tmpDifficulty = reqDictionary[msg.requestID][0]

        # get number of 'empty' fields
        emptyCounter = 0
        for row in msg.sudoku:
            for elem in row:
                if elem == 0:
                    emptyCounter = emptyCounter + 1
        percentCounter = (emptyCounter * 100) / (k*k)

        # check if we are done or if need still need to 'empty' fields (difficulty)
        print("check if we are done or if need still need to 'empty' fields..")
        if (tmpDifficulty == "1" and percentCounter < 0.7) or (tmpDifficulty == "2" and percentCounter < 0.5) \
          or (tmpDifficulty == "3" and percentCounter < 0.3):
            #if achieved -> sudoku finished for GUI -> send camel-msg
            msgToSend = Message(requestID=reqDictionary[msg.requestID][4], senderAddress=self.senderAddress, instruction="display", sudoku=sudoku)
            self.bridge.send(msgToSend)
            del reqDictionary[msg.requestID]
            print("done..")
            return

        # remove numbers
        print("remove numbers..")
        sudoku = msg.sudoku
        sudoku, cleanedNumbers = emptyField(sudoku,1)
        (difficulty, finishedSudoku, oldNumbers, memorySet, firstID) = reqDictionary[msg.requestID]
        del reqDictionary[msg.requestID]
        cleanedNumbers = oldNumbers + cleanedNumbers
        rID = Message.createGUID()
        reqDictionary[rID] = (difficulty, finishedSudoku, cleanedNumbers, memorySet, firstID)

        # send camel-msg to broker (request to solve)
        msgToSend = Message(requestID=rID, senderAddress=self.senderAddress, instruction="solve", sudoku=sudoku)
        print("send solvedOne stuff")
        self.bridge.send(msgToSend)


    def handleSolvedMany(self, msg):
        print("start handleSolvedMany..")
        if (not (msg.requestID in reqDictionary)):
            print("unknown requestID..\nbreak")
            return

        # recover previous state
        print("recover previous state..")
        lastNumber = reqDictionary[msg.requestID][2].pop()
        (difficulty, finishedSudoku, oldNumbers, memorySet, firstID) = reqDictionary[msg.requestID]
        del reqDictionary[msg.requestID]
        sudoku = msg.sudoku
        sudoku[lastNumber[0]][lastNumber[1]] = finishedSudoku[lastNumber[0]][lastNumber[1]]

        # Check whether the number has already been removed
        print("check wether the number has already been removed..")
        if (len(memorySet) == len(sudoku)):
            lastNumber = reqDictionary[msg.requestID][2].pop()
            sudoku[lastNumber[0]][lastNumber[1]] = finishedSudoku[lastNumber[0]][lastNumber[1]]
            memorySet = set()

        # remove numbers and check if already removed
        print("remove numbers and check if already removed..")
        sudoku, cleanedNumbers = emptyField(sudoku,1)
        memorySet = memorySet.add(cleanedNumbers[0])
        cleanedNumbers = oldNumbers + cleanedNumbers
        rID = Message.createGUID()
        reqDictionary[rID] = (difficulty, finishedSudoku, cleanedNumbers, memorySet, firstID)

        # send camel-msg to broker (request to solver)
        msgToSend = Message(requestID=rID, senderAddress=self.senderAddress, instruction="solve", sudoku=sudoku)
        print("send generated stuff..")
        self.bridge.send(msgToSend)


    def handle(self, msg):
        """
        Ruft je nach Instruction die entsprechende Handle-Funktion auf
        """
        if(msg.instruction.startswith("generate")):
            self.handleGenerate(msg)
        elif(msg.instruction == "solved:one"):
            self.handleSolvedOne(msg)
        elif(msg.instruction == "solved:many"):
            self.handleSolvedMany(msg)
        else:
            print("Some error occured!")
