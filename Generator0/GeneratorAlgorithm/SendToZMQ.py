import zmq
from Message import *

# Message(requestID, instruction, sudoku) und uebergeben
# Versendet Informationen als ZMQ Multipart.
# zmqPORT ist der Port, auf dem Camel empfaengt

def send(message, zmqPORT = 5555, seperator=';'):

    # Connect
    zmqContext = zmq.Context.instance()
    zmqSocket = zmqContext.socket(zmq.PUSH)
    zmqSocket.bind("tcp://127.0.0.1:" + str(zmqPORT))

    # Als Multipart senden
    toSend = str(message.requestID) + seperator + message.sender + seperator + message.instruction + seperator + str(message.sudoku)
    zmqSocket.send(bytes(toSend, 'utf-8'))

    # Disconnect
    zmqSocket.close()
    zmqContext.term()


if __name__ == "__main__":
    toSend = Message(requestID="123", sudoku=[[1,2,3],[4,5,6]])
    send(toSend)
