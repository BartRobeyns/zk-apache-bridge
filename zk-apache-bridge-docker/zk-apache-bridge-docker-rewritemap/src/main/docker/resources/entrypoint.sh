#!/bin/sh

apachectl start
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar app.jar --spring.profiles.active=docker --zkapachebridge.rewritemap.path=/etc/apache2/maps/apimap.txt

