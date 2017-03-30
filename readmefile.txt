INSTRUCTIONS ON RUNNING AND COMPILING THE PROGRAMS:
-------------------------------------------------------

ASSUMPTIONS:
-------------
-> We have assumed that the config file's first line will be a dummy line stating the format of the every entry in the file.
-> Thus the first line of any custom config file will need to specify the actual hostname and portnumber from line 2 else parsing will fail

COMPILATION:
-------------
1. Extract the .tar or gz folder
2. Change directory to the extracted folder directory
3. Execute compile.sh

RUNNING TESTCASES:
--------------------

TEST CASE 1: Single host with 5 server processes and 8 client threads:
-----------------------------------------------------------------------
-> Open 6 terminal windows
-> In the first terminal window, type:
    -> Compile using instructions given above
    -> Change directory to gen-java/
    -> Type java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankServer 0 config.txt
    -> This will start the server with id 0
-> In the second terminal window, type:
    -> Compile using instructions given above
    -> Change directory to gen-java/
    -> Type java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankServer 1 config.txt
    -> This will start the server with id 1
-> In the third terminal window, type:
    -> Compile using instructions given above
    -> Change directory to gen-java/
    -> Type java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankServer 2 config.txt
    -> This will start the server with id 2
-> In the fourth terminal window, type:
    -> Compile using instructions given above
    -> Change directory to gen-java/
    -> Type java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankServer 3 config.txt
    -> This will start the server with id 3
-> In the fifth terminal window, type:
    -> Compile using instructions given above
    -> Change directory to gen-java/
    -> Type java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankServer 4 config.txt
    -> This will start the server with id 4
-> In the sixth terminal window :
    -> Compile using instructions given above
    -> Change directory to gen-java/
    -> Type java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankClient 8 config.txt
    -> This will start a client with 8 threads

TEST CASE 2: Multi host with 5 server processes and 8 client threads:
-----------------------------------------------------------------------
-> Copy the entire .tar file to each of these hosts.
-> Open 6 terminals (one each in a seperate host).
-> Compile the files using the procedure stated above in each of the hosts
-> Start each of the 5 server in a new terminal on a different host
-> Change the directory to gen-java/
-> Type in each of the servers the command : java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankServer <id of server> config.txt
-> In a different host run a client and follow the same process except the running command will be:
    -> Type java -cp ".:libs/libthrift-0.9.1.jar:libs/slf4j-api-1.7.12.jar" BankClient 8 config.txt


LOGGING MECHANISMS:
--------------------
-> The client logs can be found in a file called clientLog.txt
-> The server logs can be found in the filename of the form: serverLog_<id> ; where id of the server is appended to the filename representing log for a
particular server.

PERFORMANCE DATA:
-------------------

SINGLE HOST:
-------------
                                                                                                    
1. 1 SERVER CONFIGURATION WITH 24 CLIENT THREADS AND 100 ITERATIONS PER THREAD:
------------------------------------------------------------------------------
Total number of Client Requests = 2400
Average Response time = 2

2. 3 SERVER CONFIGURATION WITH 24 CLIENT THREADS AND 100 ITERATIONS PER THREAD:
------------------------------------------------------------------------------

SERVER1:
---------
Total number of Client Requests = 700
Average Response time = 19

SERVER2:
---------
Total number of Client Requests = 600
Average Response time = 19

SERVER3:
---------
Total number of Client Requests = 1100
Average Response time = 19

3. 5 SERVER CONFIGURATION WITH 24 CLIENT THREADS AND 100 ITERATIONS PER THREAD:
------------------------------------------------------------------------------

SERVER1:
---------
Total number of Client Requests = 300
Average Response time = 31

SERVER2:
---------
Total number of Client Requests = 400
Average Response time = 31

SERVER3:
---------
Total number of Client Requests = 900
Average Response time = 31

SERVER4:
---------
Total number of Client Requests = 600
Average Response time = 31

SERVER5:
---------
Total number of Client Requests = 200
Average Response time = 31
