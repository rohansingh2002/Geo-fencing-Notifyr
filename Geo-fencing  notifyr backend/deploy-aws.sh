#!/bin/bash

echo "start building application"

mvn clean install

echo "build completed"

chmod 400 PNNewInstanceBackEnd.pem

scp -i PNNewInstanceBackEnd.pem target/api-0.0.1-SNAPSHOT.jar  ubuntu@ec2-65-0-179-254.ap-south-1.compute.amazonaws.com:~/app/ftp/

echo "transfer completed"



