#!/bin/bash
sudo apt-get -y update
sudo apt-get -y install openjdk-8-jdk
sudo apt-get -y install nginx
sudo /etc/init.d/nginx start -c "/infrastructure-as-a-code/nginx/nginx.conf"

#sudo apt-get -y install apt-transport-https ca-certificates curl gnupg-agent software-properties-common
#curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
#sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
#sudo apt-get update
#sudo apt-get -y install docker-ce docker-ce-cli containerd.io
#sudo docker pull nginx
##COPY ./nginx/nginx.conf /etc/nginx/nginx.conf
#sudo docker run --name nginx_0 -p 80:8080 -d nginx

sudo git clone --single-branch --branch=feature/kinesis https://github.com/andrey-radzkov/infrastructure-as-a-code.git
cd infrastructure-as-a-code/
sudo chmod 777 gradlew
sudo ./gradlew build
sudo java -jar build/libs/infrastructure-as-a-code-0.0.1-SNAPSHOT.jar


