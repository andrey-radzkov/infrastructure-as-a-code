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


resource "aws_iam_role" "ec2_role" {
  name = "ec2_role"

  # Terraform's "jsonencode" function converts a
  # Terraform expression result to valid JSON syntax.
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Sid = ""
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      },
    ]
  })
}

resource "aws_iam_policy" "server_policy" {
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "ec2:Describe*"]
        Effect = "Allow"
        Resource = "*"
      },
    ]
  })
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "ec2_profile"
  role = aws_iam_role.ec2_role.name
}

resource "aws_iam_role_policy_attachment" "ec2-attach" {
  role = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2FullAccess"
}

resource "aws_iam_role_policy_attachment" "cloud-watch-attach" {
  role = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchFullAccess"
}

resource "aws_iam_role_policy_attachment" "server_policy" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.server_policy.arn
}

resource "aws_iam_role_policy_attachment" "sqs-attach" {
  role = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSQSFullAccess"
}

resource "aws_iam_role_policy_attachment" "cloudformation-attach" {
  role = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AWSCloudFormationFullAccess"
}

resource "aws_instance" "example" {
  ami = "ami-2757f631"
  instance_type = "t2.micro"
  key_name = "test2"
  user_data = file("./start.sh")
  iam_instance_profile = aws_iam_instance_profile.ec2_profile.name
  connection {
    host = self.public_ip
    type = "ssh"
    user = "ubuntu"
    private_key = file("./test2.pem")
    agent = false
    timeout = "1m"
  }
  security_groups = [
    "terraform_security_group_http",
    "terraform_security_group_ssh"]
  associate_public_ip_address = true
}

resource "aws_sqs_queue" "terraform_queue" {
  name = "demo-queue.fifo"
  fifo_queue = true
  content_based_deduplication = true
}