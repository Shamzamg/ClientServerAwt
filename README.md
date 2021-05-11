# ClientServerAwt

This project is a graphic Client/Server implemented in Java using Awt and Swing. 

The server creates a new communication thread and allocates a new port for each Client.

## Setup

The project was made using Maven and developed with IntelliJ Ultimate.

## Usage

To launch the server

```
cd Server/src/main/java

javac ./*.java # compiles everything in the folder
java Server p maxClients # loads the server on port p, with at most maxClients clients

```

You can then connect to the server by starting the main function under the Client folder
