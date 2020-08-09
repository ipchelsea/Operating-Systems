// Jordan lawson
// If the program waits, chances are a pipe was left open

#include <iostream>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

pid_t fork(void);

// Error checks the close attempt
void closeEnd(int fd) {
	if (close(fd) != 0) {
		perror("fail pipe close");
		exit(EXIT_FAILURE);
	}
}

// Error checks the dup2 attempt
void dupe(int fd, bool io) {
	if (io) {
		if (dup2(fd, STDIN_FILENO) == -1) {
			perror("fail to dupe2 STDIN");
			exit(EXIT_FAILURE);
		}
	}
	else {
		if (dup2(fd, STDOUT_FILENO) == -1) {
			perror("fail to dupe2 STDOUT");
			exit(EXIT_FAILURE);
		}
	}
}

void piping(int fd[2]) {
	if (pipe(fd) != 0) {
		perror("fail to pipe");
		exit(EXIT_FAILURE);
	}
}

int main(int argc, char** argv)
{
	int fd[2]; // Pipe 1
	int fd2[2]; // Pipe 2
	pid_t pid; // Process ID

	//SHELL
	// If pid is less than 0 that means the process failed to fork
	// When we fork, the child process starts at the same line as the fork command
	if ((pid = fork()) < 0) {
		perror("fork fail");
	}
	// If pid is 0 then the fork succeeded
	else if (pid == 0) {
		// We are in the WC child

		// This populates the arrays the read and write ends of pipes where fd[0] is the read end and fd[1] is the write end
		piping(fd);

		// If pid is less than 0 that means the process failed to fork
		if ((pid = fork()) < 0) {
			perror("fork fail");
		}

		// If pid is 0 then the fork succeeded
		else if (pid == 0) {
			// We are in the GREP child

			// Closes the read end of fd[0] because GREP nor it's children needs the read end of fd
			// However some children need the write end of fd so we keep it
			closeEnd(fd[0]);
			// This populates the arrays the read and write ends of pipes where fd2[0] is the read end and fd2[1] is the write end
			piping(fd2);

			if ((pid = fork()) < 0) {
				perror("fork fail");
			}

			else if (pid == 0) {
				// We are in the ps child

				// STDOUT_FILENO or STIN_FILENO is where commands like GREP or PS use as inputs and outputs, dup2 overrides these to (in this case) fd2[1]
				// so any command (like grep or ps) will use what fd2[1] does rather than the default
				dupe(fd2[1], false);
				//READS FROM STDIN, NO CHANGE

				// Closes the write end of fd[1] because PS no longer needs to write to fd, fully closing fd
				closeEnd(fd[1]);
				// Closes the read end of fd[0] because PS no longer needs to read fd2
				closeEnd(fd2[0]);

				// Execute PS here
				// execlp opens the specified file: "ps" and uses the specified command: "-A"
				// PS has fd2 with the read end closed because it just needs to write to fd2 for GREP to read
				execlp("ps", "ps", "-A", (char*)NULL);
			}
			else {
				// We are in GREP
				// Closes the write end of fd2[1] pipes are technically closed as long as the write end is closed because the process will reach EOF where the write end was
				// It's just more efficient and saving resources to fully close pipes
				// GREP nor it's children need to write to fd2 so we close the write end
				closeEnd(fd2[1]);

				//WRITES TO fd[1]!
				dupe(fd[1], false);
				//READS FROM fd2[0]!
				dupe(fd2[0], true);

				// Execute GREP here at this moment we have fd2 with write closed because GREP just needs to read fd2 not write
				// However GREP has fd with read closed because it just needs to write to fd for WC
				execlp("grep", "grep", argv[1], (char*)NULL);
			}
		}
		else {
			// We are in WC

			// Closes the write end of fd[1] because WC no longer needs to write to fd
			closeEnd(fd[1]);

			//READS FROM fd[0]!
			dupe(fd[0], true);

			// Execute WC here at this moment we have fd with write closed because WC just needs to read fd not write
			execlp("wc", "wc", "-l", (char*)NULL);
		}
	}
	else {
		// We are in the SHELL
		// Wait for children to finish (wait(NULL))
		wait(NULL);
	}
}

