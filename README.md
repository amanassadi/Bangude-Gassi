Week 4 – Multi-Process Simulator & IPC

Project Title

Educational Microcontroller Simulator with Process Scheduling (STC89C52)

1. Introduction

Week 4 enhances the existing microcontroller simulator by introducing a multi-process architecture using Inter-Process Communication (IPC).

The simulator is divided into three separate processes: UI Process, Core Process, and Logging Process. These processes communicate using Java TCP sockets to perform execution, display results, and maintain logs.

2. Objectives

* Implement a multi-process simulator architecture.
* Establish communication between processes using Java Socket Programming.
* Separate UI, Core, and Logging functionalities.
* Integrate the existing CPU, Memory, Stack, and FIFO Queue components.
* Enable execution commands and exchange results between processes.
* Maintain execution and error logs.

3. System Architecture

The simulator consists of three main processes:

UI Process

* Provides the graphical user interface.
* Accepts user input and assembly programs.
* Sends commands to the Core Process.
* Displays execution results and simulator status.

Core Process

* Responsible for the actual execution of the simulator.
* Contains the CPU, Memory, Stack, and FIFO Queue.
* Receives commands from the UI Process through a TCP socket.
* Uses the Assembler to convert assembly instructions into executable instructions.
* Executes programs using the CPU.
* Sends execution results and status information back to the UI.

Core Process Server Port: 5000

Logging Process

* Records execution activities.
* Maintains error and event logs.
* Receives logging information through socket communication.

Logging Process Server Port: 5001

4. Inter-Process Communication (IPC)

Java TCP Socket Programming is used to establish communication between the three processes.

The Core Process listens on port 5000 for incoming commands, while the Logging Process listens on port 5001 for logging information.

The processes exchange messages to coordinate program execution, status updates, and logging operations.

5. Core Process Components

The Core Process reuses the components developed in the previous weeks:

* CPU: Fetches, decodes, and executes instructions.
* Memory: Provides 256 bytes of internal RAM.
* Stack: Supports PUSH and POP operations using the Stack Pointer (SP).
* FIFO Queue: Maintains First-In-First-Out data processing.
* Assembler: Converts assembly source code into executable Instruction objects.

6. Program Execution

1. The user enters an assembly program through the UI.
2. The UI sends the LOAD command to the Core Process.
3. The Core Process passes the source code to the Assembler.
4. The Assembler converts the source code into Instruction objects.
5. The RUN command starts CPU execution.
6. The CPU executes instructions until the HALT instruction is reached.
7. The execution results are returned to the UI.
8. Execution activities and errors are recorded by the Logging Process.

7. Testing

The following test files are provided:

* test_week4.bat – Runs Week 4 IPC tests.
* run_week4.bat – Starts the Week 4 simulator.

Example assembly program:

MOV A,#05
INC A
HALT

Expected result:

* Accumulator (A): 06
* Program Counter (PC): 2
* Stack Pointer (SP): 7
* Execution Cycles: 3

8. Technologies Used

* Java Programming Language
* Java Socket Programming
* TCP/IP Communication
* Java Swing (GUI)
* Microprocessor Architecture
* Inter-Process Communication (IPC)

9. Conclusion

Week 4 successfully extends the existing microcontroller simulator into a multi-process system. By separating the UI, Core, and Logging functionalities and establishing communication through Java TCP sockets, the simulator achieves better modularity and process separation while retaining the CPU and hardware simulation features developed in previous weeks.
