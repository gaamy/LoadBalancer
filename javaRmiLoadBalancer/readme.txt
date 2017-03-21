
                    INF4410 TP2

=== Compilation ===
- Execute “ant” from the project folder.
-Exemple:
$ ant

=== RMI registry ===
- Lunch rmiregistry from the bin folder ./bin.
-Exemple:
$ cd bin/
$ rmiregistry 5010 &
$ cd ..

=== Computing servers ===
- Lunch the servers from the project folder ./computingServer <CAPACITY> <MALICIOUS_LEVEL> <SERVER_ID> <PORT>:
- Parameters:
	<CAPACITY> - maximum quantity of operations that can be executed on this server
	<MALICIOUS_LEVEL> - A number between 0 and 100 that represents the chance that the server is untrustworthy. 
			    An untrustworthy server will send a phoney(randomly generated) as answer.
	<SERVER_ID> - name of the server (this is the id used on the RMI registry)
	<PORT> - port where the RMI registry is located
-Exemple:
$ ./computingServer 5 0 server2 5010

=== LoadBalancer ===
==== Configure ====
- Set the name of the servers that you started on this file ./LoadBalancerConfig/serverSocket.config:
-Exemple:
	server1 5010
	server2 5010

==== Lunch ====
- Lunch the load balancer in  “non safe mode”. This mode doesn’t verify the operations.
$ ./loadBalancer 

- From the project folder,lunch the load balancer in “safe mode”. This mode double check every operation in case a server is returning phoney results.
$ ./loadBalancer -S 



==== Commands ====
Once the load balancer is lunched, he will wait for the following commands: 
	compute <FILENAME> - load the file FILENAME and start computing the operations contained on it
	-exemple:
	$ compute TestOperations/operations-588

	exit - stop the program
	$ exit
