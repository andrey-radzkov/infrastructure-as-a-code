provider "aws" {
  profile = "default"
  region = "us-east-1"
  shared_credentials_file = "C:/Users/a/.aws/credentials"
}

resource "aws_security_group" "default" {
  count = length(var.security_groups)

  name = var.security_groups[count.index].name
  description = var.security_groups[count.index].description
  ingress {
    from_port = var.security_groups[count.index].ingress.from_port
    protocol = var.security_groups[count.index].ingress.protocol
    to_port = var.security_groups[count.index].ingress.to_port
    cidr_blocks = var.security_groups[count.index].ingress.cidr_blocks
  }
  egress {
    from_port = var.security_groups[count.index].egress.from_port
    protocol = var.security_groups[count.index].egress.protocol
    to_port = var.security_groups[count.index].egress.to_port
    cidr_blocks = var.security_groups[count.index].egress.cidr_blocks
  }
}

resource "aws_instance" "example" {
//  count = 3
  ami = var.ami_id
  instance_type = var.instance_type
  key_name = var.key_name
  user_data = file("./start.sh")

  connection {
    host = self.public_ip
    type = "ssh"
    user = var.user
    private_key = file(var.private_key)
    agent = false
    timeout = "1m"
  }
  security_groups = [
    "terraform_security_group_http",
    "terraform_security_group_ssh"]
  associate_public_ip_address = true
}