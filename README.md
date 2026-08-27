# STC89C52
# Microcontroller Simulator

## 1.Project Objective:
**Education Microcontroller Simulator with Process Scheduling**

## 2.Problem Statement:
**Design and implement a simplified educational simulator for the STC89C52 8-bit microcontroller.**
**The simulator should model its essential processor components, execute a selected subset of instructions, manage program/data memory and stack operations, and provide simplified GPIO, timer, and interrupt functionality.**
**The simulator should also represent multiple programs as processes using Process Control Blocks (PCBs) and appropriate data structures.**
**It should provide FCFS, Round Robin, and Priority CPU scheduling, including context switching and performance analysis.**

## 3.Project Scope:
**1.Simulate the basic architecture of the STC89C52 8-bit microcontroller.**

**2.Implement essential registers, program counter, stack pointer, flags, memory, and stack.**

**3.Execute a selected subset of STC89C52 instructions.**

**4.Simulate basic GPIO, timer, and interrupt operations.**

**5.Represent multiple programs as processes using PCBs.**

**6.Implement process states, ready queue, and context switching.**

**7.Implement FCFS, Round Robin, and Priority scheduling algorithms.**

**8.Use data structures such as stack, queue, circular queue, and instruction lookup table.**

**9.Provide interactive features such as program loading, run, reset, and single-step execution.**

**10.Analyze performance using waiting time, turnaround time, response time, context switches, and CPU utilization.**

## 4.Microcontroller Being Simulated
**The microcontroller selected for simulation is the STC89C52, an 8-bit microcontroller based on the 8051 architecture. The simulator will represent its major components such as the accumulator, B register, general-purpose registers, program counter, stack pointer, PSW, memory, I/O ports, timers, and interrupts.**

## 5.Team Members:
1.**Muhammad Aman Assadi**

2.**Neil Saldanha**

3.**Thrupthi Anchan**

4.**Shahala Shahzeen**

## 6.Team Responsibilities:
  ## Team Responsibilities

| Member | Primary Responsibility | Supporting Responsibility |
|---|---|---|
| **1.Muhammed Aman Assadi — Team Leader** | CPU & Instruction Execution | Integration & GitHub |
| **2.Shahalla Shazeen** | Memory & Stack | CPU Support |
| **3.Trupthi Anchan** | Data Structures & Process Management | Testing |
| **4.Neil Saldanha** | OS Scheduling & Context Switching | UI & Integration |

## 7. Selected programming language:
**We have selected Java as the programming language for this project.**
**Java provides object-oriented features that are suitable for representing components such as the CPU, memory, processes, PCB, scheduler, and peripherals.**
**It also provides suitable libraries for developing the simulator's graphical user interface.**

## 8.Initial System Architecture:


                    ┌─────────────────────────────┐
                    │   STC89C52 MICROCONTROLLER  │
                    │          SIMULATOR          │
                    └──────────────┬──────────────┘
                                   │
             ┌─────────────────────┼─────────────────────┐
             ↓                     ↓                     ↓
      ┌─────────────┐       ┌─────────────┐       ┌──────────────┐
      │     CPU     │       │   MEMORY    │       │ PERIPHERALS  │
      └─────────────┘       └──────┬──────┘       └──────┬───────┘
                                   │                     │
                                   │              ┌──────┴───────┐
                                   │              ↓              ↓
                                   │        ┌────────────┐ ┌────────────┐
                                   │        │ GPIO/TIMER │ │ INTERRUPTS │
                                   │        └────────────┘ └────────────┘
                                   ↓
                         ┌────────────────────┐
                         │  PROCESS MANAGER   │
                         │  PCB & Ready Queue │
                         └─────────┬──────────┘
                                   ↓
                         ┌────────────────────┐
                         │     SCHEDULER      │
                         │ FCFS | RR | Priority│
                         └─────────┬──────────┘
                                   ↓
                         ┌────────────────────┐
                         │   USER INTERFACE   │
                         │ Load | Run | Step  │
                         │ Reset & Visualize  │
                         └────────────────────┘


## 9.Initial development plan:
1. **Study STC89C52 Architecture** – Understand its registers, memory organization, instruction set, stack, GPIO, timers, and interrupts.

2. **Design the Simulator** – Define the overall architecture and decide how the CPU, memory, peripherals, processes, and scheduler will interact.

3. **Implement CPU Simulation** – Develop the registers, PC, SP, flags, instruction decoder, and execution of the selected instructions.

4. **Implement Memory and Stack** – Create program memory, data memory, SFRs, and stack operations.

5. **Implement Peripherals** – Add simplified GPIO, timer, and interrupt functionality.

6. **Implement Process Management** – Create PCB structures, process states, ready queue, and context switching.

7. **Implement Scheduling** – Develop FCFS, Round Robin, and Priority scheduling algorithms.

8. **Develop User Interface** – Add options for program loading, run, reset, and single-step execution, along with processor and scheduling visualization.

9. **Testing and Integration** – Test each module individually and then integrate all components into the complete simulator.

10. **Performance Analysis** – Calculate waiting time, turnaround time, response time, CPU utilization, and context switches.













