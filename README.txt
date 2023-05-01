# Distributed Computing Systems

## Programming Project 1: Simple FTP Client and Server

### Members
1. Jaya Simha Reddy Kurri (jrk98933@uga.edu)
2. Shanwaz Waqar Kotekanti (sk98220@uga.edu)



#### special compilation or execution instruction


	# How to run
	
	## Run Server
	
	Open The Terminal in the Server directory(src) and enter the below commands:

	```
	$ javac FTPServer.java

	$ java FTPServer 'port-number'

	ex: In 'vcf0.cs.uga.edu' VM

	    java FTPServer 5002

	```
	The server opens a socket and is ready for connection.

	### Run Client 
	
	Open another Terminal in the same/different system in the Client directory (folder called Client) and enter the below commands:

	```
	
	$ javac FTPClient.java

	$ java FTPClient <server domain/ip> <port-number(same as server)>

	ex: In 'vcf1.cs.uga.edu' VM

		java FTPClient vcf0.cs.uga.edu 5002

	```

	The client will be connected and ready to execute Simple FTP Commands.

##### Implemented Functionalities

1. get - Copy the file with the name <remote_filename> from the remote directory to the local directory.

	* get <remote_filename>

2. put - Copy file with the name <local_filename> from local directory to remote directory.

	* put <local_filename>

3. delete – Delete the file with the name <remote_filename> from the remote directory.

	* delete <remote_filename>

4. ls - List the files and subdirectories in the remote directory.

	* ls

5. cd – Change to the <remote_direcotry_name > on the remote machine or change to the parent directory of the current directory

	* cd <remote_direcotry_name> or cd ..

6. mkdir – Create a directory named <remote_direcotry_name> as the sub-directory of the current working directory on the remote machine.

	* mkdir <remote_directory_name>

7. pwd – Print the current working directory on the remote machine.

	* pwd

8. quit – End the FTP session.

	* quit


###### Honor Pledge For Project

This project was done in its entirety by **Jaya Simha Reddy Kurri** and ** Shanwaz Waqar Kotekanti **. We hereby state that we have not received unauthorized help of any form.











