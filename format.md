## Format-Spezifikation

Nachrichten weisen folgende Felder auf:
* 'request-id': string. Generierte GUID. Bezeichner für den ursprünglichen Auftrag. Auftragnehmer und weiterleitende Broker geben der Wert unverändert zurück/weiter.
* 'sender': string. URI des Auftraggebers. Bleibt beim Weiterleiten durch Broker unverändert. Andernfalls wird die eigene URI eingetragen.
* 'sudoku': int[N*N]. N*N große Integer Matrix, wobei N = K*K (etwa N=9 := K=3 Blöcke mal K=3 Zellen). Gültige Werte sind 0 (undefiniert/leer) und 1 bis N. Die Matrix ist Zeilenweise in das Array abgebildet.
* 'instruction': string:
  * 'register:*': registriere mich als * aus {'broker','gui','solver','generator'}
  * 'unregister': entferne mich aus der Registrierung. Kann auch vom Broker gesendet werden, nachdem 'pong'-Nachrichten ausbleiben.
  * 'ping': lebst du noch? Lokale Komponente antwortet mit 'pong'
  * 'pong': ich lebe noch
  * 'solve': löse das Rätsel
  * 'solved:*': Rätsel wurde gelöst mit Ergebnis * aus {'one', 'many', 'impossible'}
  * 'generate:*': generiere Rätsel mit Schwierigkeit * (Zahl). Absender muss 'soduko'-Matrix mit N*N Nullen füllen.
  
  
Idealerweise sollten Empfänger damit umgehen können, wenn in Paketen Felder fehlen oder zu viel sind.
Felder mit ungültigen/unerwarteten Namen oder Werten sollten ggf. gelogt/ausgegeben aber ignoriert werden.
Broker sollten Pakete mit allen erhaltenen Feldern weiterleiten (auch mit unerwarteten/unverständlichen).
