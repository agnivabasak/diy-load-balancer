#!/bin/bash

# Ensure Maven is installed and available in the PATH
if ! command -v mvn &> /dev/null
then
    echo "Maven is not installed or not in PATH. Please install Maven first."
    exit 1
fi

#Running Clean and Install commands for the backend server
echo "Building the backend-server..."
mvn -f ./backend-server/pom.xml clean install 1>/dev/null #only display error
if [ $? -ne 0 ]; then
    echo "Maven build failed. Exiting."
    exit 1
fi
echo "Success"
echo ""

#Running Clean and Install commands for the backend server
echo "Building the load-balancer..."
mvn -f ./load-balancer/pom.xml clean install 1>/dev/null #only display error
if [ $? -ne 0 ]; then
    echo "Maven build failed. Exiting."
    exit 1
fi
echo "Success"
echo ""
echo "Please proceed to run the start.sh script"