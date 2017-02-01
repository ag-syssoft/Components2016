from SudokuGenerator import *
from Message import *
from Handler import *

sudoku = generateInitialSudoku(k=3)

#sudoku, list = emptyField(sudoku,17)
#print(formattedString(sudoku))
#print(sudoku)

a = set()
a.add(1)
print(a)


senderAddress = "127.0.0.1"
hdl = Handler(senderAddress)

senderAddress = "lustigeAdd"
msg1 = Message(requestID="test-first-id1", senderAddress=senderAddress, instruction="generate:[difficulty:1]", sudoku=sudoku)
hdl.handle(msg1)

sudoku, list = emptyField(sudoku,5)

msg3 = Message(requestID="test-solvedMany-id1", senderAddress=senderAddress, instruction="solved:many", sudoku=sudoku)
hdl.handle(msg3)

msg2 = Message(requestID="test-solved1-id1", senderAddress=senderAddress, instruction="solved:one", sudoku=sudoku)
hdl.handle(msg2)
