# URL: /api/message

from Message import *
from flask import Flask, jsonify, request
import json

def msgPrinter (message):
    print(message.requestID)
    print(message.sender)
    print(message.instruction)
    print(message.sudoku)
    print()

def parseSudoku(self, sudoku):
	# Nimmt Sudoku-Flat-Array entgegen und gibt verschachteltes Array aus
	# Fehlt: Excetion Handling bei falscher Sudko länge
	parts = int(math.sqrt(len(sudoku)))
	toReturn = [[]] * parts
	for iA in range (parts):
		for iB in range (parts):
			toReturn[iA] += [sudoku[parts*iA+iB]]
	return toReturn

app = Flask(__name__)

@app.route('/api/message', methods=['POST'])
def receive():
	print("====================")
	print("HEADERS:")
	print(request.headers)
	print("====================")
	print("DATA:")
	print(request.form)
	#recvJson = request.get_json(force=True)
	recvString = list(request.form.keys())[2]
	print("====================")
	print("JSON:")
	print(recvString)
	print("====================")
	handleMessage(recvString)
	return "", 200

if __name__ == '__main__':
	app.run(debug=True,port=80,host='0.0.0.0')
