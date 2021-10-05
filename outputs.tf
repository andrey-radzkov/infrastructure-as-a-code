output "public_dns" {
  value = "${google_compute_instance.default.network_interface[0].access_config[0].nat_ip}:8080/hello"
}
