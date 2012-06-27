Changelog
=========

Version 2.5 *(2012-03-08)*
--------------------------------

* Datenbank-Upgrade für bessere Zeitrepräsentation. Es dauert ein paar Sekunden bis die Datenbank aktualisiert wurde.
* Verstecken einzelner Serien.
* 'Nur Favoriten'-Einstellung in Demnächst/Kürzlich, wird von Widgets respektiert.
* 'Alle bisher gesendeten gesehen'-Funktion bei Lang-Tippen auf eine Folge.
* Einheitliche Oberfläche auf allen Geräten dank ActionBarSherlock 4.0.
* Mittleres und großes Widget auf Android 3.0+ Geräten entfernt. Listen-Widget benutzen!

Version 2.4.2 *(2012-01-11)*
-------------------------------

* Fix für einen Absturz beim Aktualisieren, falls trakt Aktivität geholt wird
* Fix für falsch angezeigtes Serienposter im Listen-Widget
* Aktuelle Übersetzungen von crowdin.

Version 2.4.1 *(2012-01-07)*
-------------------------------

* Neues Listen-Widget für Android 3.0+.
* Überarbeitung aller Widgets, sie zeigen jetzt 1 (klein), 3 (mittel) oder 7 (groß) Folgen an.
* Aktuelle Übersetzungen von crowdin.

Version 2.4 *(2011-12-25)*
--------------------------------

* Check In einer Serie bei trakt. Nach dem Check In zeigt trakt die Folge als 'watching' an und markiert diese nach Ablauf der Laufzeit als gesehen.
* Bei einem Update werden kürzl. gesehene Folgen von trakt geladen, dies ermöglicht eine einfache Synchronisation zw. Geräten. Nach dem Hinzufügen einer Serie sollte initial aber ein Sync über `Einstellungen` >> `Sync mit trakt` >> `Sync nach SeriesGuide` vorgenommen werden.
* `Demnächst` in `Aktivität` umbenannt.

Version 2.3 *(2011-11-26)*
--------------------------------

* Erneuerter Updater: Serien nach 7 Tagen als veraltet markiert und dann im nächsten Update eingeschlossen
* Auto-Update: betätigt den Aktual.-Knopf automatisch beim Starten der App
* Aktualisieren funktioniert nur über Wi-Fi, kann in den Einstellungen deaktiviert werden
* Hinzufügen von Serien überarbeitet (bessere trakt Integration)
* Folgen-Swiper/Pager auf Telefonen und kleinen Bildschirmen
* TVDb Buttons

Version 2.2.2 *(2011-11-01)*
--------------------------------

* Behebe Crash bei Hinzufügen von Serien auf einzelnen HTC Geräten (Desire HD, Mytouch 4G, ...)

Version 2.2.1 *(2011-10-31)*
--------------------------------

* Layout-Fixes für kleine Tablets (large screens)
* Validierung von trakt.tv Zugangsdaten
* Noch besserer Schutz des trakt.tv Passwortes (muss daher leider erneut eingegeben werden)
* Suchindex wird nur bei Änderungen neu erstellt
* Aktuelle Übersetzungen. Neu in Ungarisch dank devilinside und uw11!

Version 2.2 *(2011-10-18)*
--------------------------------

* Desire HD Nutzer: bitte versuchen Sie Ihr Gerät auf die neueste Firmware zu aktualisieren
* Filter für Serienliste
* Weitreichende Verbesserungen am Layout, v.a. auf Tablet/Google TV (large+) Geräten
* Zwei-Spalten-Ansicht für 'Demnächst' auf large+ Geräten
* Zeige gerade aktualisierte Serie an
* Neues Nummernformat realisiert von dqdb via GitHub
* Diverse Fehlerbehebungen und Bugfixes
* Dank an alle die Fehlerberichte und Vorschläge eingesendet haben oder bei der Übersetzung helfen!

Version 2.1.2 *(2011-10-01)*
--------------------------------

* Bessere Fehlerbehandlung in trakt API
* Neue Serien-Sortierung: Favoriten, dann nach nächster Folge
* Beim Löschen von Serien werden deren Bilder entfernt
* Zeige erste Folge in Staffel-Doppelspaltenansicht automatisch
* Bugfixes, Verbesserungen

Version 2.1.1 (04.09.2011)
------------------------

* Bessere Benachrichtigungen wenn delta oder komplette Aktualisierung ('Alle aktualisieren') verwendet wird
* Verwendung der TVDB id anstatt der IMDb id um Serien auf trakt gesehen zu markieren
* Setze keinen Touchscreen mehr voraus (für neues Google TV)
* Benutze immer ein GridLayout für die Serien-Liste.
* Einstellungen aufgeräumt.
* URLs zeigen zur neuen Website.
* Verbesserungen am DeltaUpdate.
* Aktuelle Übersetzungen.

Version 2.1 (13.08.2011)
------------------------

* Design updates (incl. usage of ActionBarSherlock)
* Delta-Updates (Full update option remains available)
* Experimental syncing with trakt.tv
* 'Hide watched shows' and 'No special episodes' options brought to you by Jake Wharton (big thanks!)
* Progress bars in season list
* Show trending or your shows on trakt.tv when adding shows
* New 'Favorites first' show sorting, ability to add shows to favorites
* Many other small improvements
* Thanks to all the beta channel users!


Version 2.0.6 (24.07.2011)
--------------------------------

* Fix calculation of air time and day when using time offset (and in general it is now done right)
* Fixes for some crashes reported by Android Market
* If your device is set to GMT-5:00 (US Central) SeriesGuide automagically corrects airtimes by 1 hour


Version 2.0.5 (08.07.2011)
--------------------------------

* using trakt does not require an API key anymore
* Removed languages with little translation (due to user requests)
* Reworded some strings (English only)


Version 2.0.4 (17.06.2011)
--------------------------------

* Fix: Various rare crashes
* Now with two variants of Portuguese


Version 2.0.3 (16.06.2011)
--------------------------------

* Fix: Crash when updating a show failed.
* trakt.tv rating via the share button.
* Latest translations from crowdin.
* Some other rare crash fixes.


Version 2.0.2 (15.06.2011)
--------------------------------

* Fix: Crashes while database ops were running in parallel
* Latest translations from crowdin.


Version 2.0.1 (14.06.2011)
--------------------------------

* Fix: German whats new was missing.


Version 2.0 (14.06.2011)
--------------------------------

* Redesign of UI and backend (try using it in landscape, or on Android 3.0+!).
* New time offset setting.
* Remove single episodes.
* Long-clicking on Back takes you to the show list.
* Dropped support for Android 1.6
* Much more... (see the source changes)
