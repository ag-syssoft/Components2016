(1)
Laufen die beiden Broker parallel?
> Ja
Registriert man sich bei beieden?
> Ja

(2)
Wäre eine Kommunikations-ID sinnvoll?
Diese könnte sinnvoll sein, wenn mehrere Generatoren/Solver parallel arbeiten.
zB damit klar ist, auf welches Sudoku sich die Antwort des Solvers bezieht
> request-id

(3)
Wir implmentieren Schwierigkeitsgrade.
Schwierigkeitsgrade definieren sich über die Anzahl der freien Felder
(wenn man so lange Felder rausnimmt bis nichts mehr geht hat man immer die schwerste Lösung)
> OK

(4)
Vorschlag:
Tupel als JSON, statt String assoziatives Array
> Siehe Design

(5)
Kodierung leerer / vom solver zu füllender Bereiche im Sudoku-Feld
> Leere Felder werden als Null kodiert

(6)
Wird der Broker mehrere Camel Instanzen besitzen, sodass sich der Broker beim Versenden an die Spezifikation des jeweiligen Clients hält?
(das ließe auch mit einer Camel-Instanz realisieren, die Frage ist eher ob sich der Broker beim Versenden an die Spezifikation den Empfängers anpasst).
So wie das aktuell beschrieben ist lautet die Antwort eigentlich ja, aber eine Nachfrage wäre hier sicher nicht verkehrt.
> Ja

(7)
Beide Broker verwenden das selbe Kommunikationsformat?
> Ja

(8)
Wird der Broker konsequent Broadcasten.
> Ja

Weitere Design Entscheidungen
> Gemeinsames GIT-Repo, jede Gruppe 1 Ordner
> Im Soduku wird immer die gesamte Matrix geflattet, nicht 3x3-Weise
> Im Zweifelsfall wird auf JSON zurückgegriffen um das Tupel zu kommunizieren.
