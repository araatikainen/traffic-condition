# Traffic condition checker

Our app displays driving conditions in various spots, using weather and road data, fetched from FMI and Digitraffic APIs.

## Installation

The app will need Maven to be installed. It can be installed by the command

```bash
mvn install
```

in the directory.

## Usage

The app is run in a Java environment, using the commands

```bash
mvn package
java -jar target/app-1.0.one-jar.jar
```

in the directory. 
By choosing a stations from the list in the UI-window, the app will calculate how good the driving conditions are in the given spot, and show it by displaying an emoji.

