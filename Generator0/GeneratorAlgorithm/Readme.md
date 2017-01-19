pip install flask  
pip install pyzmq

==========================

##Notizen fuer Do 19.01

###Handler.py
Eine Liste zum Speichern der "DB"

DB-STRUCTURE  
* sudokuID
    - requestIDs [list]  
    - difficulty [string]  
    - lastState  [sudoku]  

Ablauf im Handler:  
####generate:  
* gelöstes Sudoku erstellen
* Element in die DB schreiben
* Camel-Nachicht verschicken

####solved-one:  
* check difficulty from DB
* falls difficulty erreicht ~> done
* update sudoku state to DB (+ add requestID to db.requestIDs list)
* remove number from sudoku
* Camel-Nachicht verschicken

####solved-many:  
* get lastState from DB
* ~> gleiches Prozedere wie bei solved-one (jedoch stellt sich hier noch eine Frage ~> falls noch weitere Felder geleert werden sollen, ist unser Ablauf nicht schön, da wir random Felder entfernen)

+++ welche Form haben die requestIDs? Nach meinen Verständnis werden die IDs von den einzelnen Komponenten bestimmt, jedoch filtert der Broker nach Einzigartigkeit von requestID+instruction


####SudokuGenerator.py
remove removeNumbers() ?  
remove difficulty logic out of generateSudoku() ? ~> Handler.py
