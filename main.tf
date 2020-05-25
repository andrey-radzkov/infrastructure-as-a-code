provider "aws" {
  profile = "default"
  region = "us-east-1"
  shared_credentials_file = "C:/Users/a/.aws/credentials"
}

resource "aws_security_group" "ssh" {

  name = "terraform_security_group_ssh"
  description = "AWS security group for terraform example"
  ingress {
    from_port = "22"
    to_port = "22"
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
}

resource "aws_security_group" "http" {

  name = "terraform_security_group_http"
  description = "AWS security group for terraform example"
  ingress {
    from_port = "80"
    to_port = "80"
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "example" {
  ami = "ami-2757f631"
  instance_type = "t2.micro"
  key_name = "test2"
  connection {
    host = self.public_ip
    type = "ssh"
    user = "ubuntu"
    private_key = file("./test22.pem")
    agent = false
    timeout = "1m"
  }
  security_groups = [
    "terraform_security_group_http",
    "terraform_security_group_ssh"]
  associate_public_ip_address = true
  provisioner "file" {
    source = "files/"
    destination = "/tmp/"
  }
  provisioner "remote-exec" {
    inline = [
      "Y | sudo apt-get install apt-transport-https ca-certificates curl gnupg-agent software-properties-common",
      "curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -",
      "sudo add-apt-repository \"deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable\"",
      "sudo apt-get update",
      "Y | sudo apt-get install docker-ce docker-ce-cli containerd.io",
      "sudo docker pull nginx",
      "sudo docker run -d -p 80:80 -v /tmp:/usr/share/nginx/html --name nginx_0 nginx",
      "sudo sed -iE \"s/{{ hostname }}/`hostname`/g\" /tmp/index.html",
      "sudo sed -iE \"s/{{ container_name }}/nginx_0/g\" /tmp/index.html"
    ]
  }
}