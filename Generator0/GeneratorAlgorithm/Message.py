from flask import jsonify

class Message():

	def __init__ (self, requestID="-1", senderAdress="", instruction="solve", sudoku="[[]]"):



		self.requestID   = requestID
		self.sender      = "rest:post:" + sender + "/api/message"
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
