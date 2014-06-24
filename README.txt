Note: This requires this java development kit and jmf version 2.1.1e specifically to be installed on all machines involved.

on the server machine
run javac proxy_server.java
run java proxy_server
follow the setup on the program


on the machine that you want to stream from
run javac SenderTest.java
run java SenderTest
follow the setup on the program


on the machine you want to receive the video from
run javac SessionJoiner.java
run java SessionJoiner
follow the setup on the program


Known Problems,
This system cannot stream video and receive video on the same machine
-seems to be a problem with the receive player