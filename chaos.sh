#!/bin/bash

# This script will randomly choose a few of the backend servers and terminate them.
# It will restart them after the given time interval. This will only work properly if
# start.sh was used to start the servers and the backendServerPids.txt file still exists in the root directory

echo "Introducing Chaos..."
echo ""

echo "Checking if backendServerPids.txt exists"
if [ ! -f backendServerPids.txt ]; then
    echo "backendServerPids.txt not present!"
	exit 1
fi
echo "Success"
echo ""

echo "Checking if backend-server JAR exists"
if [ ! -f ./backend-server/target/be-server-1.0.jar ]; then
    echo "Backend Server JAR not present in /backend-server/target folder"
	echo "Please make sure you have run the setup.sh script before running the start.sh script"
	exit 1
fi
echo "Success"
echo ""

#Checking configuration
echo "Configuration from user: "
if [ $# -lt 2 ]
  then
    echo "Please supply two arguments when executing the bash script!"
	echo "The arguments required (in order) are : no. of be-servers to terminate, time (in seconds) after which they are restarted"
	exit 1
else 
	echo "No. of be-servers to terminate : $1"
	echo "Time interval for recovery : $2"
fi
echo ""

declare -a pids
file="backendServerPids.txt"

echo "Fetching details of backend servers..."
while IFS= read -r line; do
    IFS=',' read -ra linearr <<< "$line"  # Split line into array using ',' as delimiter

    port="${linearr[0]}"
    pid="${linearr[1]}"

    pids["$port"]="$pid"
done < "$file"

noOfBeServers="${#pids[@]}"
echo "No. of backend servers up and running : $noOfBeServers"
if [ $noOfBeServers -lt $1 ]
  then
    echo "Number of backend servers actually running is less than the provided no. of servers to terminate"
	exit 1
fi
echo "Success"
echo ""


all_ports=("${!pids[@]}")

# Shuffle the keys and pick the first $1
random_ports=($(printf "%s\n" "${all_ports[@]}" | shuf -n "$1"))

echo "Terminating the be-servers..."
for key in "${random_ports[@]}"
do
	echo "Terminating be-server with PORT: $key, PID: ${pids[$key]}"
	kill -9 "${pids[$key]}" 1>/dev/null &
	if [ $? -ne 0 ]; then
		echo "Failed to terminate process"
	else 
		echo "Success"
	fi
done

echo ""
echo "Waiting for time interval ($2 seconds)..."
sleep $2

for key in "${random_ports[@]}"
do
	echo "Starting backend server in port $key"
	java -jar ./backend-server/target/be-server-1.0.jar $key 1>/dev/null &
	pids["$key"]=$!
	if [ $? -ne 0 ]; then
		echo "Server startup failed"
	else 
		echo "Success"
	fi
done
echo "Updated PIDs of servers: ${pids[@]}"
echo ""

echo "Updating the PIDs in the file (backendServerPids.txt)"
echo ""
cat /dev/null>backendServerPids.txt
for key in "${!pids[@]}"
do
  echo "${key},${pids[$key]}">>backendServerPids.txt
done
echo "Success"
echo ""



