variable "ami_id" {
  default = "ami-2757f631"
}
variable "aws_availability_zones" {
  default = [
    "us-east-1a",
    "us-east-1b"
  ]
}
variable "security_groups" {
  default = [
    {
      name = "terraform_security_group_ssh"
      description = "AWS ssh security group for terraform example"
      ingress = {
        from_port = "22"
        to_port = "22"
        protocol = "tcp"
        cidr_blocks = [
          "0.0.0.0/0"]
      }
      //      TODO: think how to remove duplocate
      egress = {
        from_port = 0
        to_port = 0
        protocol = "-1"
        cidr_blocks = [
          "0.0.0.0/0"]
      }
    },
    {
      name = "terraform_security_group_http"
      description = "AWS http security group for terraform example"
      ingress = {
        from_port = "80"
        to_port = "8080"
        protocol = "tcp"
        cidr_blocks = [
          "0.0.0.0/0"]
      }
      egress = {
        from_port = 0
        to_port = 0
        protocol = "-1"
        cidr_blocks = [
          "0.0.0.0/0"]
      }
    }
  ]
}
variable "instance_type" {
  default = "t2.micro"
}
variable "key_name" {
  default = "test2"
}
variable "private_key" {
  default = "./test2.pem"
}
variable "user" {
  default = "ubuntu"
}
