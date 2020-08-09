//processes.cpp
//teach u the different in the command shells
import java.util.*;
import java.sql.*;
import java.nio.file.*;
import static java.lang.System.*;

// Java program to illustrate reading data from file
// using nio.File
import java.nio.charset.StandardCharsets;
import java.io.*;

public class Shell extends Thread {
    public void run() {
    int lineCount = 1;
    // WHILE-SHELL -  This while loop represents the overall Shell Implemented.
    while(true) {
        StringBuffer getCommand = new StringBuffer();
        SysLib.cout("Shell [" + lineCount + "]: ");
        SysLib.cin(getCommand); // Gets what is in the file line.
        int elementCount = 0;
        String[] userInput = SysLib.stringToArgs(getCommand.toString()); // Create array delimited by spaces

        // If the command typed by the user is only "Exit" we will not check for further commands
        // and we will immediately leave exit the shell
        if(userInput[0].equalsIgnoreCase( "Exit")) {
            SysLib.exit();
            break;
        }
        // WHILE-COMMAND - This while loop captures the user's command.
        while( elementCount < userInput.length) {
            String[] argumentsOfCommand = new String[3]; // hold arguments for command
            int command_count = 0;
            // WHILE - ARGUMENTS - This while loop captures the arguments of each command;

            while( (elementCount < userInput.length) && !(userInput[elementCount].equals(";")) &&  !(userInput[elementCount].equals("&")) /*&& command_count < argumentsOfCommand.length*/) {
                argumentsOfCommand[command_count] = userInput[elementCount];
                command_count++; // increment command count to adjust where arguments are set
                elementCount++; //increment elementCount to adjust what element of the userinput is being examined
            }

            //Passes the following elements of string array & starts it as child thread
            int child_pid = SysLib.exec(argumentsOfCommand);

            //If the delimiter typed by the user is ';' or the end of a line,
            //then it waits for the termination. Returns -1, if failed.
            //for one of the child threads before returning the ID
            //& means concurrent execution
            if( elementCount >= userInput.length || userInput[elementCount].equals(";")) {
                boolean wait = true;
                while(wait) {
                    int check = SysLib.join();

                    if(check == -1) {
                        SysLib.cout("error with returning child");
                        break;
                    }

                    if(check == child_pid) {
                        wait = false;

                    }

                }

            }

            elementCount++; // increase elementCount to get past the delimiter element
        } // repeat until out of commands from shell input
        // This will capture the arguments of a command given by the user into a second
        // array called  "argumentsOfCommand"
        lineCount++; // Shell[lineCount+1]:
    } //exit from shell input
}

}
