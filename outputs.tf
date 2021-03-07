output "public_dns" {
  value = "http://${aws_instance.example.public_dns}:8080/set-name?name=newName"
}
