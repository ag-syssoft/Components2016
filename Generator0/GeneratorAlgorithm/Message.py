from flask import jsonify

class Message():
	# Beim Versenden wird der Sender von Camel gesetzt.
	def __init__ (self, requestID, instruction, sudoku, sender=""):
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
