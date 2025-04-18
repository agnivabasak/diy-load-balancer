#!/bin/bash

echo "Checking if load-balancer JAR exists"
if [ ! -f ./load-balancer/target/lb-server-1.0.jar ]; then
    echo "Load Balancer JAR not present in /load-balancer/target folder"
	echo "Please make sure you have run the setup.sh script before running the start.sh script"
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

echo "Configuration from user: "
if [ $# -lt 5 ]
  then
    echo "Please supply five arguments when executing the bash script!"
	echo "The arguments required (in order) are : load balancer port, first backend server port, number of backend servers, Load balancing algorithm(refer to README.md)"
	echo "and then intervals (in seconds) for checking the health of the backend servers"
	echo "If first backend server port is 8081, and number of backend servers are 3, then the ports of the be-servers would be 8081, 8082, 8083"
	exit 1
else 
	echo "Load balancer port: $1"
	echo "First backend server port: $2"
	echo "Number of backend servers: $3"
	echo "Load balancing algorithm: $4"
	echo "Seconds of interval for Health Checks of the backend servers: $5"
fi
echo ""

echo "Creating backendServerUrls.txt (if it doesn't exist) or emptying the contents(if it does exist)"
cat /dev/null>backendServerUrls.txt

echo "Entering the localhost adresses of the be-servers into the backendServerUrls.txt"
startBackendServer=$2
endBackendServer=$(($2+$3-1))
for ((i=$startBackendServer;i<=$endBackendServer;i++));
do
	echo "http://localhost:$i">>backendServerUrls.txt
done

current_dir=$(pwd)
file_path="${current_dir}/backendServerUrls.txt"

# Convert to Windows-style backslashes if running on Windows
if [[ "$os_type" == *"mingw"* || "$os_type" == *"cygwin"* ]]; then
    file_path=$(echo "$file_path" | sed 's:/:\\:g')
fi
echo "Backend Server urls for the load balancer stored in: $file_path"
echo "Success"
echo ""

declare -a pids
for ((i=$startBackendServer;i<=$endBackendServer;i++));
do
	echo "Starting backend server in port $i"
	java -jar ./backend-server/target/be-server-1.0.jar $i 1>/dev/null &
	pids+=($!)
	if [ $? -ne 0 ]; then
		echo "Server startup failed"
	else 
		echo "Success"
	fi
done
echo "Started servers with PIDs: ${pids[@]}"
echo ""

echo "Saving the PIDs in a file (backendServerPids.txt)"
echo ""
echo "Creating backendServerPids.txt (if it doesn't exist) or emptying the contents(if it does exist)"
cat /dev/null>backendServerPids.txt
for (( i=0; i<$3; i++ ));
do
  echo "$(($2+$i)),${pids[$i]}">>backendServerPids.txt
done
echo "Success"
echo ""

echo "Starting the load balancer in port $1"
java -jar ./load-balancer/target/lb-server-1.0.jar $1 $4 "$file_path" $5

