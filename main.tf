terraform {
  required_providers {
    google = {
      source = "hashicorp/google"
      version = "3.5.0"
    }
  }
}

provider "google" {
  credentials = file("./temporal-genius-216510-d1067f7fe71f.json")

  project = "temporal-genius-216510"
  region  = "us-central1"
  zone    = "us-central1-c"
}

resource "google_compute_instance" "default" {
  name         = "test"
  machine_type = "e2-micro"
  zone         = "us-central1-a"

  tags = ["foo", "bar"]
  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-9"
    }
  }

  // Local SSD disk
#  scratch_disk {
#    interface = "SCSI"
#  }

  network_interface {
    network = "default"

    access_config {
      // Ephemeral public IP
#      nat_ip = google_compute_address.static.address
    }
  }

  metadata = {
    foo = "bar"
  }

  metadata_startup_script = file("./start.sh")

#  service_account {
#    # Google recommends custom service accounts that have cloud-platform scope and permissions granted via IAM Roles.
#    email  = google_service_account.default.email
#    scopes = ["cloud-platform"]
#  }
}