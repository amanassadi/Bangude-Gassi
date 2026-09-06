package ui;

import javax.swing.*;
import java.awt.*;

public class SimulatorUI extends JFrame {

    // CPU state displayed by the UI
    private int pc = 0;
    private int accumulator = 0;
    private int bRegister = 0;
    private int r0 = 0;
    private int r1 = 0;
    private String flags = "00000000";
    private String currentInstruction = "None";

    // UI components
    private JLabel pcLabel;
    private JLabel accumulatorLabel;
    private JLabel bLabel;
    private JLabel r0Label;
    private JLabel r1Label;
    private JLabel flagsLabel;
    private JLabel instructionLabel;
    private JLabel statusLabel;

    private JTextArea executionTrace;
    private JTextArea programArea;

    public SimulatorUI() {
        setTitle("STC89C52 Microcontroller Simulator");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createUI();
    }

    private void createUI() {

        // ---------------- TOP BUTTON PANEL ----------------
        JPanel buttonPanel = new JPanel();

        JButton loadButton = new JButton("Load");
        JButton resetButton = new JButton("Reset");
        JButton stepButton = new JButton("Step");
        JButton runButton = new JButton("Run");

        buttonPanel.add(loadButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(runButton);

        // ---------------- PROGRAM AREA ----------------
        programArea = new JTextArea();
        programArea.setEditable(false);
        programArea.setBorder(BorderFactory.createTitledBorder("Program / Instructions"));

        // ---------------- CPU STATE PANEL ----------------
        JPanel cpuPanel = new JPanel(new GridLayout(8, 2));

        pcLabel = new JLabel("0");
        accumulatorLabel = new JLabel("0");
        bLabel = new JLabel("0");
        r0Label = new JLabel("0");
        r1Label = new JLabel("0");
        flagsLabel = new JLabel(flags);
        instructionLabel = new JLabel(currentInstruction);
        statusLabel = new JLabel("Ready");

        cpuPanel.setBorder(BorderFactory.createTitledBorder("CPU State"));

        cpuPanel.add(new JLabel("Program Counter (PC):"));
        cpuPanel.add(pcLabel);

        cpuPanel.add(new JLabel("Accumulator (A):"));
        cpuPanel.add(accumulatorLabel);

        cpuPanel.add(new JLabel("B Register:"));
        cpuPanel.add(bLabel);

        cpuPanel.add(new JLabel("R0:"));
        cpuPanel.add(r0Label);

        cpuPanel.add(new JLabel("R1:"));
        cpuPanel.add(r1Label);

        cpuPanel.add(new JLabel("Flags / PSW:"));
        cpuPanel.add(flagsLabel);

        cpuPanel.add(new JLabel("Current Instruction:"));
        cpuPanel.add(instructionLabel);

        cpuPanel.add(new JLabel("Execution Status:"));
        cpuPanel.add(statusLabel);

        // ---------------- EXECUTION TRACE ----------------
        executionTrace = new JTextArea();
        executionTrace.setEditable(false);
        executionTrace.setBorder(
                BorderFactory.createTitledBorder("Execution Trace")
        );

        // ---------------- CENTER PANEL ----------------
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(programArea), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(cpuPanel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(executionTrace), BorderLayout.CENTER);

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        // ---------------- MAIN LAYOUT ----------------
        setLayout(new BorderLayout());

        add(buttonPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // ---------------- BUTTON ACTIONS ----------------

        loadButton.addActionListener(e -> loadProgram());

        resetButton.addActionListener(e -> resetSimulator());

        stepButton.addActionListener(e -> executeStep());

        runButton.addActionListener(e -> runProgram());

        resetSimulator();
    }

    // ---------------- LOAD ----------------
    private void loadProgram() {

        programArea.setText(
                "MOV A, #05\n" +
                "ADD A, #03\n" +
                "MOV R0, A\n" +
                "END\n"
        );

        statusLabel.setText("Program Loaded");

        executionTrace.append(
                "Program loaded successfully.\n"
        );
    }

    // ---------------- RESET ----------------
    private void resetSimulator() {

        pc = 0;
        accumulator = 0;
        bRegister = 0;
        r0 = 0;
        r1 = 0;
        flags = "00000000";
        currentInstruction = "None";

        updateDisplay();

        if (executionTrace != null) {
            executionTrace.setText("Simulator reset.\n");
        }

        if (statusLabel != null) {
            statusLabel.setText("Ready");
        }
    }

    // ---------------- STEP ----------------
    private void executeStep() {

        if (pc == 0) {
            currentInstruction = "MOV A, #05";

            executionTrace.append(
                    "FETCH → MOV A, #05\n"
            );

            executionTrace.append(
                    "DECODE → Load immediate value 05 into A\n"
            );

            accumulator = 5;

            executionTrace.append(
                    "EXECUTE → A = 5\n\n"
            );
        }

        else if (pc == 1) {
            currentInstruction = "ADD A, #03";

            executionTrace.append(
                    "FETCH → ADD A, #03\n"
            );

            executionTrace.append(
                    "DECODE → Add 03 to A\n"
            );

            accumulator = accumulator + 3;

            executionTrace.append(
                    "EXECUTE → A = " + accumulator + "\n\n"
            );
        }

        else if (pc == 2) {
            currentInstruction = "MOV R0, A";

            executionTrace.append(
                    "FETCH → MOV R0, A\n"
            );

            executionTrace.append(
                    "DECODE → Copy A into R0\n"
            );

            r0 = accumulator;

            executionTrace.append(
                    "EXECUTE → R0 = " + r0 + "\n\n"
            );
        }

        else {
            currentInstruction = "END";

            executionTrace.append(
                    "Program terminated.\n"
            );

            statusLabel.setText("Program Finished");

            updateDisplay();
            return;
        }

        pc++;

        statusLabel.setText("Executing");

        updateDisplay();
    }

    // ---------------- RUN ----------------
    private void runProgram() {

        while (pc <= 2) {
            executeStep();
        }

        statusLabel.setText("Program Finished");
    }

    // ---------------- UPDATE DISPLAY ----------------
    private void updateDisplay() {

        if (pcLabel != null) {
            pcLabel.setText(String.valueOf(pc));
        }

        if (accumulatorLabel != null) {
            accumulatorLabel.setText(String.valueOf(accumulator));
        }

        if (bLabel != null) {
            bLabel.setText(String.valueOf(bRegister));
        }

        if (r0Label != null) {
            r0Label.setText(String.valueOf(r0));
        }

        if (r1Label != null) {
            r1Label.setText(String.valueOf(r1));
        }

        if (flagsLabel != null) {
            flagsLabel.setText(flags);
        }

        if (instructionLabel != null) {
            instructionLabel.setText(currentInstruction);
        }
    }

    // ---------------- MAIN ----------------
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            SimulatorUI simulator = new SimulatorUI();
            simulator.setVisible(true);
        });
    }
}
