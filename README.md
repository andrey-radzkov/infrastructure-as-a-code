# Infrastructure as a code: simple deploy
## Prerequisite
1. AWS account
2. Installed aws CLI (link here)
3. Installed Git Bash
4. Installed Terraform (link here)
5. Generated private key for EC2 with name test2.pem and placed to the root

## How to run
1. Execute the following commands in git bash:
```
terraform init
terraform plan
terraform apply -auto-approve
```
2. Copy public_dns output value, paste to browser 
3. Enjoy)