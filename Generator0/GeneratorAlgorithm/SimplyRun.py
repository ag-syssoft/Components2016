from SudokuGenerator import *
from Message import *
from Handler import *

sudoku = generateInitialSudoku(k=3)
sudoku, list = emptyField(sudoku,17)
print(formattedString(sudoku))

print(sudoku)

senderAddress = "127.0.0.1"
hdl = Handler(senderAddress)

senderAddress = "lustigeAdd"
msg1 = Message(requestID=Message.createGUID(), senderAddress=senderAddress, instruction="generate:[difficulty:1]", sudoku=sudoku)
hdl.handle(msg1)

msg2 = Message(requestID=Message.createGUID(), senderAddress=senderAddress, instruction="generate:[difficulty:1]", sudoku=sudoku)
