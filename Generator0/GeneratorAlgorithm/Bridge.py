import zmq
from Message import *

# Message(requestID, instruction, sudoku) und uebergeben
# Versendet Informationen als ZMQ Multipart.
# zmqPORT ist der Port, auf dem Camel empfaengt

class Bridge():

    def __init__(self, zmqIP="127.0.0.1", zmqPORT = "5555", seperator=';'):
        self.zmqContext = zmq.Context.instance()
        self.zmqSocket = self.zmqContext.socket(zmq.PUSH)
        self.zmqSocket.bind("tcp://" + zmqIP + ":" + zmqPORT)
        self.seperator = seperator
        self.initialized = True

    def send(self, message):
        toSend = str(message.requestID) + self.seperator + message.sender + self.seperator + message.instruction + self.seperator + str(message.sudoku)
        self.zmqSocket.send(bytes(toSend, 'utf-8'))

    def disconnect (self):
        if (self.initialized):
            self.zmqSocket.close()
            self.zmqContext.term()
            self.initialized = False

    def __del__(self):
        # Kein garbage collecting vor Nachrichtenversand
        self.disconnect()

if __name__ == "__main__":
    toSend = Message(requestID="936DA01F-9ABD-4D9D-80C7-02AF85C822A8", senderAdress=getAddress(), instruction="register:generator", sudoku=[[1,2,3],[4,5,6]])
    myBridge = Bridge()
    myBridge.send(toSend)
    myBridge.disconnect()
