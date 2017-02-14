# URL: /api/message

from Message import *
import json
import uuid
from flask import Flask, jsonify, request
from Bridge import *
from Handler import Handler

app = Flask(__name__)
myBridge = Bridge()
myHandler = Handler()

@app.route('/api/message', methods=['POST'])
def receive():
	m = Message(request.json['request-id'], request.json['instruction'], request.json['sudoku'], request.json['sender'])
	print(m.json())
	return m.json(), 201

def handleMessage(rawMsg):
	recvJson = json.loads(rawMsg)
	recvMsg = Message(requestID=recvJson["request_id"], senderAddress=recvJson["sender"], instruction=recvJson["instruction"], sudoku=self.parseSudoku(recvJson["sudoku"]))
	if recvMsg.instruction == "ping"
		recvMsg.instruction = "pong"
		myBridge.send(recvMsg)
	elif recvMsg.instruction.startswith("solved")
		handle(recvMsg)
	elif recvMsg.instruction.startswith("generate")
		handle(recvMsg)	
	
def parseSudoku(self, sudoku):
	# Nimmt Sudoku-Flat-Array entgegen und gibt verschachteltes Array aus
	# Fehlt: Excetion Handling bei falscher Sudko länge
	parts = int(math.sqrt(len(sudoku)))
	toReturn = [[]] * parts
	for iA in range (parts):
		for iB in range (parts):
			toReturn[iA] += [sudoku[parts*iA+iB]]
	return toReturn
	
if __name__ == '__main__':
	toSend = Message(requestID=uuid.uuid4(), senderAddress=getAddress(), instruction="register:generator", sudoku=[0])
    myBridge.send(toSend)
	app.run(debug=True,port=80,host='0.0.0.0')
	toSend = Message(requestID=uuid.uuid4(), senderAddress=getAddress(), instruction="unregister:generator", sudoku=[0])
    myBridge.send(toSend)
    myBridge.disconnect()
