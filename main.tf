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
  count = 3
  ami = var.ami_id
  instance_type = var.instance_type
  key_name = var.key_name
  user_data = file("./start.sh")
  availability_zone = var.aws_availability_zones[count.index % length(var.aws_availability_zones)]

  connection {
    host = self.public_ip
    type = "ssh"
    user = var.user
    private_key = file(var.private_key)
    agent = false
    timeout = "1m"
  }
  security_groups = aws_security_group.default.*.name
  associate_public_ip_address = true
}

resource "aws_lb" "web" {
  name = "albweb"
  internal = false
  load_balancer_type = "application"
  subnets = aws_instance.example.*.subnet_id
  security_groups = aws_security_group.default.*.id
}