# Infrastructure as a code: simple deploy
## Prerequisite
1. AWS account
2. Installed [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html)
3. Installed Git Bash
4. Installed [Terraform](https://www.terraform.io/downloads.html)
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
4. To cleanup your environment execute in git bash
```
terraform destroy -auto-approve
```