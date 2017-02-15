from SudokuGenerator import *
from Message import *
from Handler import *

#sudoku = generateInitialSudoku(k=3)

#sudoku, list = emptyField(sudoku,17)
#print(formattedString(sudoku))

input = [1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,]

def parseSudoku(sudoku):
	# Nimmt Sudoku-Flat-Array entgegen und gibt verschachteltes Array aus
	# Fehlt: Exception Handling bei falscher Sudoku-länge
	parts = int(math.sqrt(len(sudoku)))
	toReturn = [[0]] * parts
	for iA in range (parts):
		for iB in range (parts):
			toReturn[iA] += [sudoku[parts*iA+iB]]
	return toReturn

out = parseSudoku(input)

print(out)


#print(sudoku)

#senderAddress = "127.0.0.1"
#hdl = Handler(senderAddress)

#senderAddress = "lustigeAdd"
#msg1 = Message(requestID="test-first-id1", senderAddress=senderAddress, instruction="generate:[difficulty:1]", sudoku=sudoku)
#hdl.handle(msg1)

#sudoku, list = emptyField(sudoku,5)

#msg3 = Message(requestID="test-solvedMany-id1", senderAddress=senderAddress, instruction="solved:many", sudoku=sudoku)
#hdl.handle(msg3)

#dic = hdl.getDictionary
#print(dic["test-solved1-id1"][0])

#msg2 = Message(requestID="test-solved1-id1", senderAddress=senderAddress, instruction="solved:one", sudoku=sudoku)
#hdl.handle(msg2)
