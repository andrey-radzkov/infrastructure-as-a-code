output "public_dns" {
  value = join(", ", aws_instance.example.*.public_dns)
}

output "aws_lb_dns" {
  value = "${aws_lb.web.dns_name}/hello"
}
