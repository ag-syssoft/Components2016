# URL: /api/message

from Message import *
from solve import solve
from flask import Flask, jsonify, request

app = Flask(__name__)
myBridge = Bridge()

@app.route('/api/message', methods=['POST'])
def receive():
	if not request.json or not 'request_id' in request.json or not 'sender' in request.json or not 'instruction' in request.json or not 'sudoku' in request.json:
		abort(400)
	m = Message(request.json['request_id'], request.json['sender'], request.json['instruction'], request.json['sudoku'] )
	if len(m.sudoku) == (9*9):
		abort(400)
	print(m.json().get_data(as_text=True))
	solutions=solve(m.sudoku)
	print("found {} solutions".format(len(solutions)))
	if len(solutions)==0:
		instr="solved:impossible"
	elif len(solutions)==1:
		instr="solved:one"
	else:
		instr="solved:many"
	toSend = Message(m.requestID, getAddress(), instr, m.sudoku)
	myBridge.send(toSend)
	return m.json(), 201

if __name__ == '__main__':
	app.run(debug=True,port=80,host='0.0.0.0')
