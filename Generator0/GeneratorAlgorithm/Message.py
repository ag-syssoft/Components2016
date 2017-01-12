class Message():
    requestID   = ""
    sender      = ""
    instruction = ""
    sudoku      = []

    # Beim Versenden wird der Sender von Camel gesetzt.
    def __init__(self, requestID, sender, instruction, sudoku):
        self.requestID   = requestID
        self.sender      = sender
        self.instruction = instruction
        self.sudoku      = sudoku
