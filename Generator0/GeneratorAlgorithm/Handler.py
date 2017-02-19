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

    senderAddress = ""
    bridge = None

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


    def checkValid(self, sudoku):
        return ((math.sqrt(len(sudoku))) % 1) == 0


    def handleGenerate(self, msg):
        print("start handleGenerate..")

        if (not(self.checkValid(msg.sudoku))):
            print("length of sudoku not valid ~> stopping handleGenerate..")
            return

        if (len(msg.sudoku) == 1):
            msgToSend = Message(requestID=msg.requestID, senderAddress=self.senderAddress, instruction="display", sudoku=[[0]])
            self.bridge.send(msgToSend)
            print("done.. sudokuSize = 1..")
            return

        k = int(math.sqrt(len(msg.sudoku)))
        numberOfFields = k*k*k*k
        difficulty = msg.instruction[-1:]
        sudoku = generateFilledSudoku(k)

        # initial cleanup (remove 8 numbers)
        print("initial cleanup (remove numberOfFields/10 numbers)..")
        sudoku, cleanedNumbers = emptyField(sudoku,int(numberOfFields/10))
        rID = Message.createGUID().urn[9:]
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

        k = int(len(msg.sudoku))
        tmpDifficulty = reqDictionary[msg.requestID][0]

        # get number of 'empty' fields
        emptyCounter = 0
        for row in msg.sudoku:
            for elem in row:
                if elem == 0:
                    emptyCounter = emptyCounter + 1
        percentCounter = (emptyCounter) / (k*k)
        sudoku = msg.sudoku
        print(percentCounter)

        # check if we are done or if need still need to 'empty' fields (difficulty)
        print("check if we are done or if need still need to 'empty' fields..")
        if (tmpDifficulty == "1" and percentCounter > 0.3) or (tmpDifficulty == "2" and percentCounter > 0.5) \
          or (tmpDifficulty == "3" and percentCounter > 0.7):
            #if achieved -> sudoku finished for GUI -> send camel-msg
            msgToSend = Message(requestID=reqDictionary[msg.requestID][4], senderAddress=self.senderAddress, instruction="display", sudoku=sudoku)
            self.bridge.send(msgToSend)
            del reqDictionary[msg.requestID]
            print("done..")
            return

        # remove numbers
        print("remove numbers..")
        sudoku, cleanedNumbers = emptyField(sudoku,1)
        (difficulty, finishedSudoku, oldNumbers, memorySet, firstID) = reqDictionary[msg.requestID]
        del reqDictionary[msg.requestID]
        cleanedNumbers = oldNumbers + cleanedNumbers
        rID = Message.createGUID().urn[9:]
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

        # get number of fields with numbers
        filledCounter = 0
        for row in sudoku:
            for elem in row:
                if elem != 0:
                    filledCounter = filledCounter + 1

        # Check if we tried all fields on this 'level'
        print("check if we tried all fields on this 'level'..")
        if (len(memorySet) == filledCounter):
            print("we tried all fields on this level, we need to recover the previous 'level'..")
            lastNumber = reqDictionary[msg.requestID][2].pop()
            sudoku[lastNumber[0]][lastNumber[1]] = finishedSudoku[lastNumber[0]][lastNumber[1]]
            memorySet = set()

        # remove a field
        print("remove a field..")
        sudoku, cleanedNumbers = emptyField(sudoku,1)
        memorySet.add(cleanedNumbers[0])
        cleanedNumbers = oldNumbers + cleanedNumbers
        rID = Message.createGUID().urn[9:]
        reqDictionary[rID] = (difficulty, finishedSudoku, cleanedNumbers, memorySet, firstID)

        # send camel-msg to broker (request to solver)
        msgToSend = Message(requestID=rID, senderAddress=self.senderAddress, instruction="solve", sudoku=sudoku)
        print("send solvedMany stuff..")
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
        elif(msg.instruction == "solved:impossible"):
            print("Impossible Sudoku")
        else:
            print("Some error occured!")
