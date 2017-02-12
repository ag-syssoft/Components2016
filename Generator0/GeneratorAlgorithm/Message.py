import uuid
import socket
from flask import jsonify

def getAddress(ipAddressPort=""):
	if (ipAddressPort==""):
		ipAddressPort = socket.gethostbyname(socket.gethostname())
	return "restlet:http://" + ipAddressPort + "/api/message?restletMethod=post"

class Message():

	def __init__ (self, requestID="-1", senderAddress="", instruction="solve", sudoku="[[]]"):
		self.requestID   = requestID
		self.sender      = senderAddress
		self.instruction = instruction
		self.sudoku      = sudoku

	def json (self):
		message = {
				'request-id': self.requestID,
				'sender': self.sender,
				'instruction': self.instruction,
				'sudoku': self.sudoku,
				}
		return jsonify({'message': message})

	def createGUID():
		return uuid.uuid1()
