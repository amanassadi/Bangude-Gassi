package ui;

import simulator.CPU;
import simulator.Instruction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Swing UI for the existing STC89C52 simulator classes.
 * The UI controls and displays the real CPU; it does not re-implement the CPU.
 */
public class SimulatorUI extends JFrame {

    private final CPU cpu = new CPU();
    private List<Instruction> loadedProgram = new ArrayList<>();

    private final DefaultTableModel programModel = new DefaultTableModel(
            new Object[]{"Address", "Instruction"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable programTable = new JTable(programModel);
    private final JTextArea traceArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("READY");
    private final JLabel currentInstructionLabel = new JLabel("None");

    private final JLabel pcLabel = new JLabel();
    private final JLabel spLabel = new JLabel();
    private final JLabel aLabel = new JLabel();
    private final JLabel bLabel = new JLabel();
    private final JLabel[] rLabels = new JLabel[8];
    private final JLabel flagsLabel = new JLabel();

    private final DefaultTableModel memoryModel = new DefaultTableModel(
            new Object[]{"Address", "Value"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public SimulatorUI() {
        setTitle("STC89C52 Microcontroller Simulator");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildUI();
        updateCpuDisplay();
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadButton = new JButton("Load");
        JButton resetButton = new JButton("Reset");
        JButton stepButton = new JButton("Step");
        JButton runButton = new JButton("Run");

        top.add(loadButton);
        top.add(resetButton);
        top.add(stepButton);
        top.add(runButton);
        top.add(Box.createHorizontalStrut(20));
        top.add(new JLabel("Status:"));
        top.add(statusLabel);
        add(top, BorderLayout.NORTH);

        programTable.setFillsViewportHeight(true);
        programTable.setRowHeight(24);

        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(BorderFactory.createTitledBorder("Program / Instructions"));
        left.add(new JScrollPane(programTable), BorderLayout.CENTER);

        JPanel currentPanel = new JPanel(new BorderLayout());
        currentPanel.setBorder(BorderFactory.createTitledBorder("Current Instruction"));
        currentPanel.add(currentInstructionLabel, BorderLayout.CENTER);
        left.add(currentPanel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.setBorder(BorderFactory.createTitledBorder("Registers"));
        center.add(createRegisterPanel(), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.setBorder(BorderFactory.createTitledBorder("Memory (Data Memory)"));
        right.add(new JScrollPane(new JTable(memoryModel)), BorderLayout.CENTER);

        JPanel upper = new JPanel(new GridLayout(1, 3, 8, 8));
        upper.add(left);
        upper.add(center);
        upper.add(right);

        traceArea.setEditable(false);
        traceArea.setLineWrap(true);
        traceArea.setWrapStyleWord(true);
        traceArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel tracePanel = new JPanel(new BorderLayout());
        tracePanel.setBorder(BorderFactory.createTitledBorder("Execution Trace"));
        tracePanel.add(new JScrollPane(traceArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(tracePanel, BorderLayout.CENTER);
        bottom.add(createFlagsPanel(), BorderLayout.EAST);

        add(upper, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        loadButton.addActionListener(e -> loadProgram());
        resetButton.addActionListener(e -> resetSimulator());
        stepButton.addActionListener(e -> stepProgram());
        runButton.addActionListener(e -> runProgram());
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 4, 4));
        panel.add(new JLabel("PC"));
        panel.add(pcLabel);
        panel.add(new JLabel("SP"));
        panel.add(spLabel);
        panel.add(new JLabel("A"));
        panel.add(aLabel);
        panel.add(new JLabel("B"));
        panel.add(bLabel);

        for (int i = 0; i < 8; i++) {
            rLabels[i] = new JLabel();
            panel.add(new JLabel("R" + i));
            panel.add(rLabels[i]);
        }
        return panel;
    }

    private JPanel createFlagsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Flags / Status"));
        flagsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(flagsLabel);
        return panel;
    }

    /** Load a small demo program using the project's real Instruction and CPU classes. */
    private void loadProgram() {
        loadedProgram = new ArrayList<>();
        loadedProgram.add(new Instruction("MOV", "R0", "#10"));
        loadedProgram.add(new Instruction("MOV", "A", "#20"));
        loadedProgram.add(new Instruction("ADD", "A", "R0"));
        loadedProgram.add(new Instruction("INC", "R0", ""));
        loadedProgram.add(new Instruction("DEC", "A", ""));
        loadedProgram.add(new Instruction("ANL", "A", "#15"));
        loadedProgram.add(new Instruction("ORL", "A", "#2"));

        cpu.loadProgram(loadedProgram);

        programModel.setRowCount(0);
        for (int i = 0; i < loadedProgram.size(); i++) {
            programModel.addRow(new Object[]{String.format("%04X", i), loadedProgram.get(i)});
        }

        currentInstructionLabel.setText("None");
        traceArea.setText("Program loaded successfully.\n");
        statusLabel.setText("LOADED");
        updateCpuDisplay();
    }

    private void resetSimulator() {
        cpu.reset();
        currentInstructionLabel.setText("None");
        traceArea.setText("Simulator reset.\n");
        statusLabel.setText("READY");
        updateCpuDisplay();
    }

    private void stepProgram() {
        Instruction instruction = cpu.fetch();

        if (instruction == null) {
            statusLabel.setText("FINISHED");
            currentInstructionLabel.setText("None");
            traceArea.append("Program finished.\n");
            updateCpuDisplay();
            return;
        }

        int oldPC = cpu.getPC();
        int oldA = cpu.getA();
        int oldR0 = cpu.getRegisterValue("R0");

        currentInstructionLabel.setText(String.format("%04X : %s", oldPC, instruction));
        traceArea.append(String.format("FETCH   : %s%n", instruction));
        traceArea.append(String.format("DECODE  : %s%n", instruction));

        cpu.step();

        traceArea.append(String.format(
                "EXECUTE : PC %04X -> %04X | A %02X -> %02X | R0 %02X -> %02X%n%n",
                oldPC, cpu.getPC(), oldA & 0xFF, cpu.getA() & 0xFF,
                oldR0 & 0xFF, cpu.getRegisterValue("R0") & 0xFF));

        statusLabel.setText(cpu.fetch() == null ? "FINISHED" : "EXECUTING");
        updateCpuDisplay();
        selectCurrentInstruction();
    }

    private void runProgram() {
        if (cpu.fetch() == null) {
            statusLabel.setText("FINISHED");
            return;
        }

        while (!cpu.isHalted() && cpu.fetch() != null) {
            stepProgram();
        }

        statusLabel.setText("FINISHED");
        updateCpuDisplay();
    }

    private void selectCurrentInstruction() {
        int row = cpu.getPC();
        if (row >= 0 && row < programModel.getRowCount()) {
            programTable.setRowSelectionInterval(row, row);
            programTable.scrollRectToVisible(programTable.getCellRect(row, 0, true));
        } else {
            programTable.clearSelection();
        }
    }

    private void updateCpuDisplay() {
        pcLabel.setText(String.format("%04X", cpu.getPC()));
        spLabel.setText(String.format("%02X", cpu.getSP()));
        aLabel.setText(String.format("%02X", cpu.getA() & 0xFF));
        bLabel.setText(String.format("%02X", cpu.getRegisterValue("B") & 0xFF));

        for (int i = 0; i < 8; i++) {
            rLabels[i].setText(String.format("%02X", cpu.getRegisterValue("R" + i) & 0xFF));
        }

        flagsLabel.setText(String.format(
                "CY=%d   AC=%d   OV=%d   P=%d",
                cpu.getCY() ? 1 : 0,
                cpu.getAC() ? 1 : 0,
                cpu.getOV() ? 1 : 0,
                cpu.getP() ? 1 : 0));

        updateMemoryDisplay();
    }

    private void updateMemoryDisplay() {
        memoryModel.setRowCount(0);
        int[] addresses = {0, 1, 2, 3, 4, 5, 6, 7, 0x20};
        for (int address : addresses) {
            memoryModel.addRow(new Object[]{
                    String.format("%02X", address),
                    String.format("%02X", cpu.readDataMemory(address))
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulatorUI().setVisible(true));
    }
}
