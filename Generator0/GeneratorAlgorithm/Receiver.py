from Message import *
from flask import Flask, jsonify, request, json

app = Flask(__name__)

@app.route('/api/message', methods=['POST'])
def receive():
    m = Message(request.json['request_id'], request.json['instruction'],
                request.json['sudoku'], request.json['sender'])
    m.print()
    return m.json(), 201

if __name__ == '__main__':
    app.run(debug=False,port=80,host='0.0.0.0')
