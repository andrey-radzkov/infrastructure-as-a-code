#!/bin/bash
sudo apt-get -y update
sudo apt-get -y install git
sudo apt-get -y install openjdk-17-jdk
sudo git clone --single-branch --branch=feature/gcp https://github.com/andrey-radzkov/infrastructure-as-a-code.git
sudo apt-get -y install nginx
sudo /etc/init.d/nginx start
cd infrastructure-as-a-code/
sudo chmod 777 gradlew
sudo ./gradlew build
sudo java -jar build/libs/infrastructure-as-a-code-0.0.1-SNAPSHOT.jar
