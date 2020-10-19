#!/bin/bash
sudo apt-get -y update
sudo apt-get -y install openjdk-8-jdk
sudo git clone --single-branch --branch=feature/kinesis https://github.com/andrey-radzkov/infrastructure-as-a-code.git
cd infrastructure-as-a-code/
sudo chmod 777 gradlew
sudo ./gradlew build
sudo java -jar build/libs/infrastructure-as-a-code-0.0.1-SNAPSHOT.jar

sudo yum install -y docker
sudo service docker start
sudo docker pull nginx
COPY ./nginx/nginx.conf /etc/nginx/conf.d/default.conf
sudo docker run -d -p 80:80 --name nginx_0 nginx

