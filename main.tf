provider "aws" {
  profile = "default"
  region = "us-east-1"
  shared_credentials_file = "C:/Users/Andrey_Radzkov/.aws/credentials"
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
    from_port = "8080"
    to_port = "8080"
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
}

resource "aws_instance" "example" {
  ami = "ami-2757f631"
  instance_type = "t2.micro"
  key_name = "test2"
  user_data = file("./start.sh")
  connection {
    host = self.public_ip
    type = "ssh"
    user = "ubuntu"
    private_key = file("./test2.pem")
    agent = false
    timeout = "1m"
  }
  security_groups = [
    "terraform_security_group_http"
  ]
  associate_public_ip_address = true
}
