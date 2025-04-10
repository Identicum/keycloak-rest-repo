import os
import subprocess
import sys
from sherpa.utils.basics import Logger
from sherpa.utils.basics import Properties


def main():
	local_properties = Properties("./local.properties", "./local.properties")
	logger = Logger(os.path.basename(__file__), local_properties.get("deployment_log_level"), local_properties.get("log_file"))
	run(logger)
	logger.info("{} finished.".format(os.path.basename(__file__)))


def run(logger):
	logger.info("{} starting.".format(os.path.basename(__file__)))

	logger.info("Processing terraform")
	command = "cd ./objects && terraform init && terraform apply --auto-approve"
	output = subprocess.check_output(command, shell=True, text=True)
	logger.info("Output:\n{}", output)


if __name__ == "__main__":
	sys.exit(main())