#!/bin/bash

err=1
until [ $err == 0 ]; 
do
	java -Djava.awt.headless=true $(cat "java.cfg") -jar ../libs/LoginServer.jar > /root/logs/login/log/stdout.log 2>&1
	err=$?
	sleep 10;
done
