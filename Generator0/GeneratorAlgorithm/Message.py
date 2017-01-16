from flask import jsonify

class Message():

	def __init__ (self, requestID="-1", sender="Generator0", instruction="solve", sudoku="[[]]"):
		self.requestID   = requestID
		self.sender      = sender
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
