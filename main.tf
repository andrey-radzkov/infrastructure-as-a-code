provider "aws" {
  profile    = "default"
  region     = "us-east-1"
  shared_credentials_file = "C:/Users/a/.aws/credentials"
}

resource "aws_instance" "example" {
  ami           = "ami-2757f631"
  instance_type = "t2.micro"
}