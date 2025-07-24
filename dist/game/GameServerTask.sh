#!/bin/bash

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt

while :; do
	java -Djava.awt.headless=true $(cat "java.cfg") -jar ../libs/GameServer.jar > /root/logs/game/log/stdout.log 2>&1
	[ $? -ne 2 ] && break
#	/etc/init.d/mysql restart
	sleep 10
done
