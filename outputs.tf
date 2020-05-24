//output "security_group" {
//  value = "${join(", ", aws_security_group.default.*.id)}"
//}
//
output "web_ip" {
  value = "${aws_instance.example.public_ip}"
}
//
//output "elb_address" {
//  value = "${aws_elb.web.dns_name}"
//}