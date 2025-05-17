# diy-load-balancer
A simple load balancer made for learning the internals of how a layer 7 lb works
<br><br>This project is developed in Java using **Jetty servers** as backend instances. 
<br><br>The load balancer distributes traffic in a **ROUND_ROBIN** fashion, supports periodic health checks, and includes shell scripts to manage setup, testing, teardown, and chaos testing.

## Steps to test

Shell scripts are  provided for ease of setting up the servers for testing. Alternatively, they can be manually spun up as well as you would a Java Jetty Server (The shell scripts can be used as reference to figure out the exact commands)
<br><br> Please execute the commands listed in the root directory of the project

### Prerequisites:

- Java 
- Maven
  
### Step 1: Build the JARs

execute `./setup.sh`<br>
<br>This will create the Load Balancer JAR in location : ./load-balancer/target/lb-server-1.0.jar
<br>and create the Backend Server JAR in location : ./backend-server/target/be-server-1.0.jar

### Step 2: Run the LBServer and BEServer

execute the start.sh script <br>
<br>Arguments to be passed:
<br>
1. The port in which the load balancer server should spin up
2. The port in which the first backend server should spin up
3. The number of backend servers to spin up
4. The load balancing algorithm to use (Currently only "ROUND_ROBIN" is supported)
5. The interval in which the health checks for the backends servers should be performed by the load balancer (in seconds)

Example : `./start.sh 8080 8081 10 ROUND_ROBIN 10`
<br><br> This will start the load balancer as well as the specified number of backend servers in chronological order.
<br>If first backend server port is 8081, and number of backend servers are 3, then the ports of the be-servers would be 8081, 8082, 8083
<br>The logs of the load balancer will appear in the current terminal and the backend servers run in the background as a detached process
<br>
<br>This will also store the \<port\>,\<pid\> of the be-servers in backendServerPids.txt and the list of be-server urls in backendServerUrls.txt 

### Step 3: Test the Load Balancing using curl

execute the curl-test.sh <br>
<br>Arguments to be passed:
<br>
1. The port in which the load balancer server is running
2. The number of requests to send to the load balancer
3. The number of requests that should be concurrent (sent at the same time)

Example : `./curl-test.sh 8080 10 10`<br>
<br>This will create and fill a curl config file (curl-test-urls.txt) to use for the curl request internally.
<br>It will then send specified number of requests to the load balancer and respond back with the response from each request to the load balancer.
```
Response from BEServer (8085)
Response from BEServer (8083)
Response from BEServer (8086)
Response from BEServer (8084)
Response from BEServer (8090)
Response from BEServer (8089)
Response from BEServer (8088)
Response from BEServer (8087)
Response from BEServer (8082)
Response from BEServer (8081)
```
<br>**Note**: The concurrency parameter along with a file input doesn't seem to work properly. It's best to send all the requests concurrently.  

### Step 4: Introduce Chaos

execute chaos.sh script <br>
<br>Arguments to be passed:
<br>
1. No. of backend servers to terminate (must be lesser than or equal to the no. of backend servers up and running)
2. Time interval after which these backend servers will recover (in seconds)

Example : `./chaos.sh 2 60`<br>
<br>This will randomly select the specified number of servers and terminate them. 
It will wait for the specified number of seconds and then get those servers back up.
<br><br>It uses the backendServerPids.txt file to fetch the pid and the port of the be-servers.
At the end, it also updates the file to hold the current pids of the be-servers.
<br>Once the servers are terminated, we can see health checks failing for them and  any requests to the load balancer not using them to serve requests

### Step 5: Stop the Load Balancer and the Backend Servers

To stop the load balancer just press `CTRL-C` in the terminal that we used to execute the start.sh script.
<br>To stop the backend servers execute `./stop.sh`. This will use the pids stored in backendServerPids.txt and stop those processes, in case they already aren't.
