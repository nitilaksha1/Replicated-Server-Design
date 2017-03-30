# Replicated Server Design (Primary-Backup Model) -

This is an implementation of a Replicated Bank Server (using Apache Thrift) which is modeled using Lamport's state machine model to ensure consistency of data on all replicas when queried by the client.

<b><u>Client Process Structure:</u></b>
* When all server processes are started and initialized , the client processes are started.<br />
* The client provided here is a multithreaded client which takes number of threads and the configuration file as the <br /> parameter. The configuration file provided should contain information about the location of which servers will be used as <br /> primary and replicas. Each entry in the config file is of the form : <hostname> <serverid> <portnumber>. <br />
* A client will randomly pick one of the servers in the config file and send a request to that server and and wait for the<br /> response before sending the next request.
* Each client thread will perform the following operations 100 times and terminate:<br />
  * It will randomly pick two accounts and transfer 10 from one account to the other.<br />
  * It will write to the client logfile a record indicating the operation request and server process<br />
* After all client threads have terminated, the main thread will send a “HALT” command to the server with ID equal to 0<br />
* The HALT is communicated and executed using the state machine model in all the server replicas.<br />
