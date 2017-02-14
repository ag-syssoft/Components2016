# URL: /api/message

from Message import *
import json
import uuid
from flask import Flask, jsonify, request
from Bridge import *
from Handler import Handler
import urllib.parse


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
myBridge = None
myHandler = None

@app.route('/api/message', methods=['POST'])
def receive():
    #print("====================")
    #print("HEADERS:")
    #print(request.headers)
    #print("====================")
    #print("RAW:")
    recvRaw = request.stream.read().decode("utf-8")
    recvDecoded = urllib.parse.unquote(recvRaw)
    #print(recvDecoded)
    #print(json.loads(request.stream.read())
    jsonString = recvDecoded.split('}')[0] + "}"
    #print("====================")
    #print(recvRaw)
    #print("====================")
    #print(jsonString)
    #print("====================")
    #print(jsonString)
    handleMessage(jsonString)
    """
    print("====================")
    print("DATA:")
    print(request.form)
    recvList= list(request.form.keys())
    print(recvList)
    recvString = ""
    counter = 0
    for i in range(len(recvList)):
        if recvList[i].startswith('{'):
            recvString = recvList[counter]
            break
    print("====================")
    print("JSON:")
    print(recvString)
    print("====================")
    """
    #handleMessage(recvString)
    return "", 200

def handleMessage(rawMsg):
    recvJson = json.loads(rawMsg)
    recvMsg = Message(requestID=recvJson["request-id"], senderAddress=recvJson["sender"], instruction=recvJson["instruction"], sudoku=recvJson["sudoku"])
    if recvMsg.instruction == "ping":
        print("PING")
        recvMsg.instruction = "pong"
        myBridge.send(recvMsg)
        print("PONG")
    elif recvMsg.instruction.startswith("solved"):
        myHandler.handle(recvMsg)
        print("SOLVED")
    elif recvMsg.instruction.startswith("generate"):
        myHandler.handle(recvMsg)
        print("GENERATE")
    else:
        print(recvMsg.instruction)

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
    myBridge = Bridge()
    myHandler = Handler(getAddress(), myBridge)
    toSend = Message(requestID=uuid.uuid4(), senderAddress=getAddress(), instruction="register:generator", sudoku=[0])
    myBridge.send(toSend)
    app.run(debug=False,port=80,host='0.0.0.0')
    toSend = Message(requestID=uuid.uuid4(), senderAddress=getAddress(), instruction="unregister:generator", sudoku=[0])
    myBridge.send(toSend)
    myBridge.disconnect()
