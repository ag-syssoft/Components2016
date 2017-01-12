class Message():

    # Beim Versenden wird der Sender von Camel gesetzt.
    def __init__ (requestID, instruction, sudoku, sender=""):
        self.requestID   = requestID
        self.sender      = sender
        self.instruction = instruction
        self.sudoku      = sudoku
