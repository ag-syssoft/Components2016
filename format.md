## Format-Spezifikation

Nachrichten weisen folgende Felder auf:
* 'request-id': string. Generierte GUID. Bezeichner für den ursprünglichen Auftrag. Auftragnehmer und weiterleitende Broker geben der Wert unverändert zurück/weiter.
* 'sender': string. URI des Auftraggebers. Bleibt beim Weiterleiten durch Broker unverändert. Andernfalls wird die eigene URI eingetragen.
* 'sudoku': int[NxN]. NxN große Integer Matrix, wobei N = KxK (etwa N=9 := K=3 Blöcke mal K=3 Zellen). Gültige Werte sind 0 (undefiniert/leer) und 1 bis N. Die Matrix ist Zeilenweise in das Array abgebildet.
* 'instruction': string:
  * 'register:_X_': registriere mich als _X_ aus {'broker','gui','solver','generator'}
  * 'unregister': entferne mich aus der Registrierung. Kann auch vom Broker gesendet werden, nachdem 'pong'-Nachrichten ausbleiben.
  * 'ping': lebst du noch? Lokale Komponente antwortet mit 'pong'
  * 'pong': ich lebe noch
  * 'solve': löse das Rätsel
  * 'solved:_X_': Rätsel wurde gelöst mit Ergebnis _X_ aus {'one', 'many', 'impossible'}
  * 'generate:_X_': generiere Rätsel mit Schwierigkeit _X_ (Zahl). Absender muss 'soduko'-Matrix mit NxN Nullen füllen.
  * 'display': Das enthaltene Sudoku ist Fertig generiert und kann angezeigt werden
  
  
Idealerweise sollten Empfänger damit umgehen können, wenn in Paketen Felder fehlen oder zu viel sind.
Felder mit ungültigen/unerwarteten Namen oder Werten sollten ggf. gelogt/ausgegeben aber ignoriert werden.
Broker sollten Pakete mit allen erhaltenen Feldern weiterleiten (auch mit unerwarteten/unverständlichen).
