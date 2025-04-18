#!/bin/bash

echo "This stops all the backend servers running using the pids stored in the backendServerPids.txt file"

echo "Checking if backendServerPids.txt exists"
if [ ! -f backendServerPids.txt ]; then
    echo "backendServerPids.txt not present!"
	exit 1
fi
echo "Success"
echo ""

declare -a pids
file="backendServerPids.txt"

echo "Backend Servers : "
while IFS= read -r line; do
    IFS=',' read -ra linearr <<< "$line"  # Split line into array using ',' as delimiter

    port="${linearr[0]}"
    pid="${linearr[1]}"

    echo "PORT: $port, PID: $pid"

    pids["$port"]="$pid"
done < "$file"

echo ""
echo "Terminating the be-servers..."
for key in "${!pids[@]}"
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
echo "Clearing backendServerPids.txt..."
cat /dev/null>backendServerPids.txt
echo "Success"

