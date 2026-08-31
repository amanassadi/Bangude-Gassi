## Initial System Architecture Design

### 1. Decisions
- Use **Java** for developing the simulator.
- Simulate the basic working of the **STC89C52 8-bit microcontroller**.
- Divide the simulator into separate modules.
- Focus on instruction execution, memory, hardware simulation, and process management.

### 2. User Interface & Controls
- Simple interface for interacting with the simulator.
- Load programs and control execution.
- Start, pause, and reset the simulation.
- Display registers, memory, PC, flags, and current instruction.

### 3. OS & Process Management
- Treat each loaded program as a separate process.
- Maintain a **Process Control Block (PCB)**.
- Use a ready queue for process management.
- Support **FCFS, Round Robin, and Priority Scheduling**.
- Demonstrate basic context switching.

### 4. Microcontroller Hardware Simulation
- Simulate important STC89C52 components:
  - CPU and Registers
  - Program Counter (PC)
  - Stack Pointer (SP)
  - Flags
  - GPIO Ports
  - Timers
  - Interrupts
- Execute instructions based on the simulated architecture.

### 5. Memory Space
- Simulate program/code memory and data memory.
- Implement memory read and write operations.
- Simulate stack and stack operations.
- Display memory contents in the user interface.

