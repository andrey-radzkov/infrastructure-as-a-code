#!/bin/bash
sudo apt-get -y update
sudo apt-get -y install openjdk-8-jdk
sudo git clone --single-branch --branch=feature/async-demo https://github.com/andrey-radzkov/infrastructure-as-a-code.git
cd infrastructure-as-a-code/
sudo chmod 777 gradlew
sudo ./gradlew build
sudo java -jar build/libs/infrastructure-as-a-code-0.0.1-SNAPSHOT.jar
