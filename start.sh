#!/bin/bash
sudo apt-get -y update
sudo apt -y install wget curl
wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.deb
sudo apt-get -y install git
sudo apt -y  install ./jdk-17_linux-x64_bin.deb
sudo git clone --single-branch --branch=feature/gcp https://github.com/andrey-radzkov/infrastructure-as-a-code.git

sudo apt-get -y install nginx
sudo cp /infrastructure-as-a-code/nginx/nginx.conf /etc/nginx/nginx.conf
sudo cp /infrastructure-as-a-code/nginx/nginx.conf /etc/init/nginx.conf
sudo /etc/init.d/nginx start

cd infrastructure-as-a-code/
sudo chmod 777 gradlew
sudo ./gradlew build
sudo java -jar build/libs/infrastructure-as-a-code-0.0.1-SNAPSHOT.jar
