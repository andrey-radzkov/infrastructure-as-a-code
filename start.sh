#!/bin/bash
sudo apt-get -y update
sudo apt-get -y install openjdk-8-jdk
sudo apt-get -y install apt-transport-https ca-certificates curl gnupg-agent software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get -y update
sudo apt-get -y install docker-ce docker-ce-cli containerd.io
sudo git clone --single-branch --branch=feature/ecr https://github.com/andrey-radzkov/infrastructure-as-a-code.git
cd infrastructure-as-a-code/
sudo chmod 777 gradlew
sudo ./gradlew build
sudo docker build -t infrastructure-as-a-code .
sudo docker run -p 8080:8080 infrastructure-as-a-code
