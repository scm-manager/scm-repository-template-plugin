---
title: Usage
---
Um ein Repository als Vorlage nutzen zu können, muss innerhalb des Repository eine `template.yml` 
im Root Verzeichnis des Main Branches angelegt werden. Diese `template.yml` definiert welche Dateien 
aus dem Vorlage-Repository kopiert werden sollen und welche Engine zum Verarbeiten des Dateiinhalts verwendet werden soll. 
Die Engine ersetzt Platzhalter beim Kopieren durch den entsprechenden Wert aus dem Kontext. 

Die Standard Engine ist [mustache](https://mustache.github.io/) und das Standard Encoding ist `UTF-8`, 
falls keine andere Werte gesetzt wurde.

Die Template Yaml muss ein Feld `files` enthalten, wo Dateien als Array angegeben werden.
Eine einzelne Datei muss die Felder `path` (Pfad innerhalb des Vorlagen-Repository) 
und `filtered` (soll der Inhalt der Datei templated werden). Wird bei `path` ein Verzeichnis angegeben, 
werden alle Datei innerhalb dieses Verzeichnisses kopiert und getemplated.
Optional können noch eine Template Engine und ein Encoding angegeben werden.

Beispiel:
```yaml 
engine: mustache
encoding: UTF-8
files: 
    -   path: README.md
        filtered: true
    -   path: src/main
        filtered: false
```

![Template](assets/template.png)

Nachdem ein Repository als Vorlage definiert wurde, kann man es für die Initialisierung bei der Repository Erstellung auswählen.

![Create repository](assets/create-repo.png)
