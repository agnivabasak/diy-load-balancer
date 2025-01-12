#!/bin/bash

echo "Please ensure both the load balancer and the backend servers are up and running"
echo "You can execute setup.sh and then start.sh to get some servers up"

#Checking configuration
echo "Configuration from user: "
if [ $# -lt 3 ]
  then
    echo "Please supply three arguments when executing the bash script!"
	echo "The arguments required (in order) are : load balancer port, number of requests to the load balancer and"
	echo "number of concurrent requests (parallel requests/requests sent at the same time) to the load balancer "
	echo "and then intervals (in seconds) for checking the health of the backend servers"
	exit 1
else 
	echo "Load balancer port: $1"
	echo "Number of reqeusts: $2"
	echo "Number of concurrent requests: $3"
fi
echo ""

#Building the config file (curl-test-urls.txt)
echo "Creating curl config file (curl-test-urls.txt) (if it doesn't exist) or emptying the contents(if it does exist)"
cat /dev/null>curl-test-urls.txt
echo "Entering the details into curl-test-urls.txt..."
urlConfig="url = \"http://localhost:$1/ping\""
for((i=1;i<=$2;i++));
do
	echo $urlConfig>>curl-test-urls.txt
done
echo "Success"

#Executing the curl command
echo "Hitting the load balancer endpoint..."
curl --parallel --parallel-immediate --parallel-max $3 --config curl-test-urls.txt
echo "Success"