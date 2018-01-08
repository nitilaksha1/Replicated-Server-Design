# Replicated Server Design (Primary-Backup Model)

This is an implementation of a Replicated Bank Server (using Apache Thrift) which is modeled using Lamport's state machine model to ensure consistency of data on all replicas when queried by the client.

## Client Process Structure:
* When all server processes are started and initialized , the client processes are started.<br />
* The client provided here is a multithreaded client which takes number of threads and the configuration file as the <br /> parameter. The configuration file provided should contain information about the location of which servers will be used as <br /> primary and replicas. Each entry in the config file is of the form : <hostname> <serverid> <portnumber>. <br />
* A client will randomly pick one of the servers in the config file and send a request to that server and and wait for the<br /> response before sending the next request.
* Each client thread will perform the following operations 100 times and terminate:<br />
  * It will randomly pick two accounts and transfer 10 from one account to the other.<br />
  * It will write to the client logfile a record indicating the operation request and server process<br />
* After all client threads have terminated, the main thread will send a “HALT” command to the server with ID equal to 0<br />
* The HALT is communicated and executed using the state machine model in all the server replicas.<br />

## Server Process Structure:
* When a server process is started, it will first create 10 accounts. <br />
* It will also initialize the balance of each to 1000. <br />
* The server process will take two command line arguments namely: id of the server (id that represents this server) and <br /> configuration file which will be the same file as used by the client.<br />

## Assumptions:
* We have assumed that the config file's first line will be a dummy line stating the format of the every entry in the file.
* Thus the first line of any custom config file will need to specify the actual hostname and portnumber from line 2 else parsing will fail

## Compiling:
* Extract the .tar or gz folder
* Change directory to the extracted folder directory
* Execute compile.sh

## Test Cases:
### Test Case 1: Single host with 5 server processes and 8 client threads:
* Open 6 terminal windows
* In the each of the five terminal windows, type:
    * Compile using instructions given above
    * Change directory to gen-java/
    * Type java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankServer <server-id> config.txt
    * This will start the server with id <number from 0-4>
* In the sixth terminal window :
    * Compile using instructions given above
    * Change directory to gen-java/
    * Type java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankClient 8 config.txt
    * This will start a client with 8 threads

### Test Case 2: Multi host with 5 server processes and 8 client threads:
* Copy the entire .tar file to each of these hosts.
* Open 6 terminals (one each in a seperate host).
* Compile the files using the procedure stated above in each of the hosts
* Start each of the 5 server in a new terminal on a different host
* Change the directory to gen-java/
* Type in each of the servers the command : java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankServer <id of server> config.txt
* In a different host run a client and follow the same process except the running command will be:
    * Type java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankClient 8 config.txt


## Logging Mechanism:
* The client logs can be found in a file called clientLog.txt
* The server logs can be found in the filename of the form: serverLog_<id> ; where id of the server is appended to the filename representing log for a
particular server.

## Performance Data:
* 1 SERVER CONFIGURATION WITH 24 CLIENT THREADS AND 100 ITERATIONS PER THREAD:
    * Total number of Client Requests = 2400
    * Average Response time = 2ms

* 3 SERVER CONFIGURATION WITH 24 CLIENT THREADS AND 100 ITERATIONS PER THREAD:
    * SERVER1:
        * Total number of Client Requests = 700
        * Average Response time = 19ms
    * SERVER2:
        * Total number of Client Requests = 600
        * Average Response time = 19ms
    * SERVER3:
        * Total number of Client Requests = 1100
        * Average Response time = 19ms
* 5 SERVER CONFIGURATION WITH 24 CLIENT THREADS AND 100 ITERATIONS PER THREAD:
    * SERVER1:
        * Total number of Client Requests = 300
        * Average Response time = 31ms
    * SERVER2:
        * Total number of Client Requests = 400
        * Average Response time = 31ms
    * SERVER3:
        * Total number of Client Requests = 900
        * Average Response time = 31ms
    * SERVER4:
        * Total number of Client Requests = 600
        * Average Response time = 31ms
    * SERVER5:
        * Total number of Client Requests = 200
        * Average Response time = 31ms
