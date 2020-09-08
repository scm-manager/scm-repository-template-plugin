---
title: Usage
---
Um ein Repository als Vorlage nutzen zu können, muss innerhalb des Repository eine `template.yml` 
im Root Verzeichnis des Main Branches angelegt werden. Diese `template.yml` definiert welche Dateien 
aus dem Vorlage-Repository kopiert werden sollen und welche Engine zum Verarbeiten des Dateiinhalts werden soll. Die Engine
ersetzt Platzhalter beim Kopieren durch den entsprechenden Wert aus dem Kontext. 
Die Standard Engine ist [mustache](https://mustache.github.io/) falls keine andere ausgewählt wurde.

![Template](assets/template.png)

Nachdem ein Repository als Vorlage definiert wurde, kann man für die Initialisierung bei der Repository Erstellung auswählen.

![Create repository](assets/create-repo.png)
