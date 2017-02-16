import threading
import zmq
import math
from Message import *

# sender String für diese API:
# zeromq:tcp://127.0.0.1:5554?socketType=PUSH
# Erfordert Freigabe von 5554 beim Broker


# RecvAPI startet bei Anlegen des Objektes automatisch.
# RecvAPI terminiert bei Aufruf von stop() und kann nicht erneut gestartet werden (neues Objekt notwendig)
class RecvAPI():

    def __init__ (self, callBackMethod, adress="127.0.0.1:5554"):
        self.callBackMethod = callBackMethod
        self.stopped = False

        self.zmqContext = zmq.Context.instance()
        self.zmqSocket = self.zmqContext.socket(zmq.PULL)
        self.zmqSocket.connect("tcp://" + adress)

        self.recvThread = threading.Thread(target=self.__recv__)
        self.recvThread.start()

    def stop(self):
        self.stopped = True

    def parseSudoku(self, sudoku):
        # Nimmt Sudoku-Flat-Array entgegen und gibt verschachteltes Array aus
        # Fehlt: Excetion Handling bei falscher Sudko länge
        parts = int(math.sqrt(len(sudoku)))
        toReturn = [[]] * parts
        for iA in range (parts):
            for iB in range (parts):
                toReturn[iA] += [sudoku[parts*iA+iB]]
        return toReturn

    def __recv__(self):
        while not self.stopped: # Auf Threadstop prüfen
            recvJson = self.zmqSocket.recv_json()
            recvMsg = Message(requestID=recvJson["request_id"], senderAddress=recvJson["sender"], instruction=recvJson["instruction"], sudoku=self.parseSudoku(recvJson["sudoku"]))
            self.callBackMethod(recvMsg)

        self.zmqSocket.close()
        self.zmqContext.term()

################################################################################

def msgPrinter (message):
    print(message.requestID)
    print(message.sender)
    print(message.instruction)
    print(message.sudoku)
    print()

if __name__ == "__main__":
    receiver = RecvAPI(msgPrinter)
