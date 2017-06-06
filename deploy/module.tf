variable "build_id" {}

terraform {
  backend "s3" {
    bucket  = "cv-overkill-tf-state"
    key     = "aws-infrastructure-skills"
    region  = "eu-west-1"
  }
}

variable "region" {
  default = "eu-west-1"
}

provider "aws" {
  region = "eu-west-1"
}

module "skills" {
  source = "github.com/mtranter/cv-overkill-terraform?ref=v1.0//modules/tf-cv-overkill-aurelia-module"
  website_files = ["app-bundle.js"]
  relative_source_path = "/../ui/dist/"
  region = "${var.region}"
  module_name = "skills"
}

data "template_file" "task_definition" {
    template = "${file("${path.module}/skills-service.json")}"

    vars {
      image_tag = "${var.build_id}"
    }
}

module "skills_backend" {
  source                        = "github.com/mtranter/cv-overkill-terraform?ref=v1.5//modules/ecs-service"
  alb_listener_rule_priority    = 88
  alb_condition_field           = "path-pattern"
  alb_condition_values          = "/skills*"
  service_name                  = "skills-service"
  alb_container_name            = "skills-service"
  service_port                  = "8080"
  task_definition               = "${data.template_file.task_definition.rendered}"
  desired_count                 = 1
  health_check_path = "/skills"

}
