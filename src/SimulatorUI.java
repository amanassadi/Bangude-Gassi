import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/** Premium dark-blue Swing dashboard for the existing STC89C52 engine. */
public class SimulatorUI extends JFrame {
    private static final Color BG=new Color(5,13,31), CARD=new Color(13,29,57), INK=new Color(229,240,255), BLUE=new Color(86,169,255), LIGHT=new Color(39,70,114), MUTED=new Color(139,168,207);
    private static final Color EDITOR_BG=new Color(8,21,44), ACCENT=new Color(42,112,235), CYAN=new Color(99,209,242);
    private static final Font FONT=new Font("Segoe UI",Font.PLAIN,13), MONO=new Font("Consolas",Font.PLAIN,13);
    private RemoteCPU cpu=new RemoteCPU();
    private FifoQueue queue=FifoQueue.attach(cpu.getMemory());
    private List<Instruction> program=new ArrayList<>();
    private final JTextArea editor=new JTextArea(8,50), stack=new JTextArea(), fifo=new JTextArea(), trace=new JTextArea(), result=new JTextArea();
    private final DefaultTableModel instructions=model("", "ADDRESS","INSTRUCTION","CATEGORY"), memory=model("ADDRESS","VALUE");
    private final JTable instructionTable=table(instructions), memoryTable=table(memory);
    private final JLabel status=new JLabel("●  READY"), current=new JLabel("No instruction loaded"), addr=new JLabel("Address: ----"), sp=new JLabel(), queueSize=new JLabel();
    private final JLabel[] reg=new JLabel[12], flags=new JLabel[4];
    private javax.swing.Timer timer;
    private int lastA,lastPC;
    private String lastInstruction="";
    private final String[] names={"PC","SP","A","B","R0","R1","R2","R3","R4","R5","R6","R7"};

    public SimulatorUI(){
        super("STC89C52  |  Blue Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(1500,920);setMinimumSize(new Dimension(1250,760));setLocationRelativeTo(null);
        editor.setText("; Write assembly here — stack and FIFO commands are supported\nMOV A,#10\nMOV 30H,#25\nPUSH 30H\nPOP 31H\nENQUEUE #12\nENQUEUE A\nDEQUEUE R0\nHALT");
        build();refresh();
    }
    private static DefaultTableModel model(String... cols){return new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};}
    private static JTable table(DefaultTableModel m){
        JTable t=new JTable(m);t.setFont(MONO);t.setRowHeight(30);t.setForeground(INK);t.setBackground(EDITOR_BG);
        t.setGridColor(new Color(25,49,82));t.setShowVerticalLines(false);t.setSelectionBackground(new Color(29,75,131));
        t.setSelectionForeground(Color.WHITE);t.setIntercellSpacing(new Dimension(0,1));t.setFillsViewportHeight(true);
        JTableHeader h=t.getTableHeader();h.setBackground(new Color(20,43,79));h.setForeground(CYAN);h.setFont(new Font("Segoe UI",Font.BOLD,11));h.setPreferredSize(new Dimension(0,32));
        return t;
    }
    private static class RoundPanel extends JPanel {
        RoundPanel(LayoutManager layout){super(layout);setOpaque(false);}
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(CARD);g.fillRoundRect(1,1,getWidth()-2,getHeight()-2,18,18);
            g.setColor(LIGHT);g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,18,18);g.dispose();super.paintComponent(graphics);
        }
    }
    private JPanel card(String heading, JComponent child){
        JPanel p=new RoundPanel(new BorderLayout(0,12));p.setBorder(new EmptyBorder(15,17,15,17));
        JLabel title=new JLabel(heading);title.setForeground(new Color(137,198,255));title.setFont(new Font("Segoe UI",Font.BOLD,12));
        p.add(title,BorderLayout.NORTH);p.add(child,BorderLayout.CENTER);return p;
    }
    private static JTextArea output(JTextArea t){t.setEditable(false);t.setFont(MONO);t.setForeground(INK);t.setBackground(EDITOR_BG);t.setCaretColor(CYAN);t.setMargin(new Insets(10,12,10,12));t.setLineWrap(true);t.setWrapStyleWord(true);return t;}
    private static JScrollPane scroll(JComponent c){JScrollPane s=new JScrollPane(c);s.setBorder(new LineBorder(new Color(27,53,91)));s.getViewport().setBackground(EDITOR_BG);s.getVerticalScrollBar().setUnitIncrement(15);return s;}
    private JButton button(String title,Runnable action){
        JButton b=new JButton(title){@Override protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(getModel().isRollover()?new Color(55,133,247):ACCENT);g.fillRoundRect(0,0,getWidth(),getHeight(),12,12);g.dispose();super.paintComponent(graphics);}};
        b.setContentAreaFilled(false);b.setOpaque(false);b.setFont(new Font("Segoe UI",Font.BOLD,12));b.setForeground(Color.WHITE);b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(11,18,11,18));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.addActionListener(e->action.run());return b;
    }
    private JPanel line(JComponent... items){JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,9,3));p.setOpaque(false);for(JComponent item:items)p.add(item);return p;}
    private JPanel textCard(String title,JTextArea area){return card(title,scroll(output(area)));}
    private static JPanel transparent(LayoutManager layout){JPanel p=new JPanel(layout);p.setOpaque(false);return p;}
    private static void tint(JLabel l,Color c,int size,boolean bold){l.setForeground(c);l.setFont(new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,size));}
    private static JSplitPane split(int direction,Component a,Component b,double proportion){
        JSplitPane s=new JSplitPane(direction,a,b);s.setBorder(null);s.setOpaque(false);s.setDividerSize(10);s.setResizeWeight(proportion);s.setContinuousLayout(true);return s;
    }
    private void build(){
        JPanel root=transparent(new BorderLayout(0,11));
        root.setBackground(BG);root.setOpaque(true);
        root.setBorder(new EmptyBorder(15,19,16,19));setContentPane(root);

        JPanel top=transparent(new BorderLayout(0,9));
        JPanel titleArea=transparent(new GridLayout(2,1,0,3));
        JLabel heading=new JLabel("STC89C52  MICROCONTROLLER SIMULATOR",SwingConstants.CENTER);
        heading.setForeground(new Color(163,209,255));
        heading.setFont(new Font("Consolas",Font.BOLD,23));
        JLabel subtitle=new JLabel("8051 ARCHITECTURE  /  INTERACTIVE EXECUTION WORKSPACE",SwingConstants.CENTER);
        tint(subtitle,MUTED,11,true);
        titleArea.add(heading);titleArea.add(subtitle);
        top.add(titleArea,BorderLayout.NORTH);
        JPanel toolbar=new RoundPanel(new BorderLayout(6,0));
        toolbar.setBorder(new EmptyBorder(6,10,6,10));
        toolbar.add(line(button("LOAD",this::loadEditor),button("OPEN ASM",this::openFile),
            button("RESET",this::reset),button("STEP",this::step),button("RUN",this::run),button("STOP",this::stop)),BorderLayout.WEST);
        JPanel state=transparent(new FlowLayout(FlowLayout.RIGHT,8,7));
        tint(status,CYAN,12,true);state.add(status);toolbar.add(state,BorderLayout.EAST);
        top.add(toolbar,BorderLayout.CENTER);
        root.add(top,BorderLayout.NORTH);

        editor.setFont(new Font("Consolas",Font.PLAIN,14));editor.setForeground(INK);
        editor.setBackground(EDITOR_BG);editor.setCaretColor(CYAN);
        editor.setSelectionColor(new Color(42,91,158));editor.setMargin(new Insets(12,14,12,14));
        editor.setTabSize(4);
        JPanel editorWrap=transparent(new BorderLayout(0,6));
        editorWrap.add(scroll(editor),BorderLayout.CENTER);
        JLabel hint=new JLabel("One instruction per line  •  MOV A,#10  •  PUSH 30H  •  POP 31H  •  ENQUEUE #12  •  DEQUEUE R0");
        tint(hint,MUTED,11,false);editorWrap.add(hint,BorderLayout.SOUTH);
        JPanel editorCard=card("PROGRAM INPUT  /  ASSEMBLY EDITOR",editorWrap);
        editorCard.setPreferredSize(new Dimension(0,220));

        JPanel instructionColumn=transparent(new BorderLayout(0,10));
        instructionColumn.add(card("PROGRAM  /  INSTRUCTIONS",scroll(instructionTable)),BorderLayout.CENTER);
        JPanel now=transparent(new BorderLayout(0,7));
        tint(current,INK,14,true);tint(addr,MUTED,12,false);
        now.add(current,BorderLayout.NORTH);now.add(addr,BorderLayout.CENTER);
        JPanel nowCard=card("CURRENT INSTRUCTION",now);
        nowCard.setPreferredSize(new Dimension(0,110));
        instructionColumn.add(nowCard,BorderLayout.SOUTH);

        JPanel registerGrid=transparent(new GridLayout(12,2,4,2));
        for(int i=0;i<names.length;i++){
            JLabel n=new JLabel(names[i]);tint(n,MUTED,12,true);registerGrid.add(n);
            reg[i]=new JLabel("00H",SwingConstants.RIGHT);
            reg[i].setForeground(INK);reg[i].setFont(new Font("Consolas",Font.BOLD,13));
            registerGrid.add(reg[i]);
        }
        JPanel registersCard=card("REGISTERS",registerGrid);
        JPanel memoryCard=card("DATA MEMORY  /  256 ADDRESSES",scroll(memoryTable));
        JPanel stackCard=textCard("STACK  /  LIFO",stack);
        JPanel queueCard=textCard("FIFO QUEUE",fifo);

        // Distinctive asymmetric workspace: instruction stream on the left,
        // CPU and the full RAM inspector in the center, stack/queue on the right.
        JPanel middle=transparent(new GridBagLayout());
        JPanel cpuMemory=transparent(new BorderLayout(0,10));
        registersCard.setPreferredSize(new Dimension(0,295));
        cpuMemory.add(registersCard,BorderLayout.NORTH);
        cpuMemory.add(memoryCard,BorderLayout.CENTER);
        JPanel dataStructures=transparent(new GridLayout(2,1,0,10));
        dataStructures.add(stackCard);
        dataStructures.add(queueCard);
        JComponent[] panels={instructionColumn,cpuMemory,dataStructures};
        double[] weights={1.45,1.35,1.0};
        for(int i=0;i<panels.length;i++){
            GridBagConstraints c=new GridBagConstraints();c.gridx=i;c.gridy=0;
            c.weightx=weights[i];c.weighty=1;c.fill=GridBagConstraints.BOTH;
            c.insets=new Insets(0,i==0?0:6,0,i==2?0:6);
            middle.add(panels[i],c);
        }

        JPanel flagsGrid=transparent(new GridLayout(1,4,8,0));
        String[] fn={"CY","AC","OV","P"};
        for(int i=0;i<4;i++){
            JPanel f=transparent(new GridLayout(2,1,0,5));
            JLabel name=new JLabel(fn[i],SwingConstants.CENTER);tint(name,MUTED,11,true);
            flags[i]=new JLabel("0",SwingConstants.CENTER);
            flags[i].setFont(new Font("Consolas",Font.BOLD,17));flags[i].setForeground(CYAN);
            f.add(name);f.add(flags[i]);flagsGrid.add(f);
        }
        JPanel bottom=transparent(new GridBagLayout());
        JComponent[] bottomPanels={textCard("EXECUTION TRACE  /  FETCH → DECODE → EXECUTE",trace),
                textCard("RESULT  /  CHANGES",result),card("FLAGS  /  STATUS",flagsGrid)};
        double[] bottomWeights={1.4,1.2,.8};
        for(int i=0;i<bottomPanels.length;i++){
            GridBagConstraints c=new GridBagConstraints();c.gridx=i;c.gridy=0;
            c.weightx=bottomWeights[i];c.weighty=1;c.fill=GridBagConstraints.BOTH;
            c.insets=new Insets(0,i==0?0:5,0,i==2?0:5);
            bottom.add(bottomPanels[i],c);
        }
        bottom.setPreferredSize(new Dimension(0,172));
        JPanel content=transparent(new BorderLayout(0,10));
        content.add(editorCard,BorderLayout.NORTH);
        content.add(middle,BorderLayout.CENTER);
        content.add(bottom,BorderLayout.SOUTH);
        root.add(content,BorderLayout.CENTER);
    }
    private void loadEditor(){stop();try{List<Instruction> parsed=Assembler.assemble(editor.getText());program=parsed;cpu.reset();cpu.loadSource(editor.getText());cpu.setTrace(false);queue=FifoQueue.attach(cpu.getMemory());trace.setText("FETCH     ○  Waiting\nDECODE    ○  Waiting\nEXECUTE   ○  Waiting\n\nProgram loaded: "+program.size()+" instructions\n");result.setText("Ready to execute.\n");status.setText("LOADED");refresh();}catch(RuntimeException ex){error(ex);}}
    private void openFile(){JFileChooser fc=new JFileChooser();if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){try{editor.setText(new String(Files.readAllBytes(fc.getSelectedFile().toPath())));loadEditor();}catch(IOException ex){error(ex);}}}
    private void step(){if(cpu.isHalted()){stop();status.setText("HALTED");return;}if(cpu.getPC()<0||cpu.getPC()>=program.size()){stop();status.setText("END OF PROGRAM");return;}Instruction ins=program.get(cpu.getPC());lastPC=cpu.getPC();lastA=cpu.getA();lastInstruction=ins.toString();try{cpu.step();trace.setText(String.format("FETCH     ●  PC=%04XH  %s\nDECODE    ●  %s\nEXECUTE   ●  Completed\n",lastPC,lastInstruction,lastInstruction));result.setText(String.format("Executed: %s\nA: %02XH -> %02XH\nPC: %04XH -> %04XH\nCycles: %d",lastInstruction,lastA,cpu.getA(),lastPC,cpu.getPC(),cpu.getCycles()));status.setText(cpu.isHalted()?"HALTED":"EXECUTED");refresh();}catch(RuntimeException ex){stop();error(ex);}}
    private void run(){if(timer!=null&&timer.isRunning())return;timer=new javax.swing.Timer(140,e->{if(cpu.isHalted()||cpu.getPC()<0||cpu.getPC()>=program.size()){stop();status.setText("FINISHED");return;}step();});timer.start();status.setText("RUNNING");}
    private void stop(){if(timer!=null)timer.stop();if("RUNNING".equals(status.getText()))status.setText("STOPPED");}
    private void reset(){stop();cpu.reset();trace.setText("FETCH     ○  Waiting\nDECODE    ○  Waiting\nEXECUTE   ○  Waiting\n\nCPU reset.\n");result.setText("Ready.\n");status.setText("READY");refresh();}
    private void error(Exception ex){status.setText("ERROR");result.setText("ERROR: "+ex.getMessage());JOptionPane.showMessageDialog(this,ex.getMessage(),"Simulator error",JOptionPane.ERROR_MESSAGE);refresh();}
    private void refresh(){int[] vals={cpu.getPC(),cpu.getSP(),cpu.getA(),cpu.getB(),cpu.getRegisterValue("R0"),cpu.getRegisterValue("R1"),cpu.getRegisterValue("R2"),cpu.getRegisterValue("R3"),cpu.getRegisterValue("R4"),cpu.getRegisterValue("R5"),cpu.getRegisterValue("R6"),cpu.getRegisterValue("R7")};for(int i=0;i<reg.length;i++)reg[i].setText(String.format(i==0?"%04XH":"%02XH",vals[i]));boolean[] f={cpu.getCY(),cpu.getAC(),cpu.getOV(),cpu.getP()};for(int i=0;i<4;i++)flags[i].setText((f[i]?"1":"0"));
        instructions.setRowCount(0);for(int i=0;i<program.size();i++){Instruction in=program.get(i);instructions.addRow(new Object[]{i==cpu.getPC()?"▶":"",String.format("%04XH",i),in.toString(),in.getOpcode()});}if(cpu.getPC()<program.size()){current.setText(program.get(cpu.getPC()).toString());addr.setText(String.format("Address: %04XH",cpu.getPC()));}else{current.setText(cpu.isHalted()?"HALTED":"No current instruction");addr.setText("Address: ----");}
        memory.setRowCount(0);for(int a=0;a<256;a++){int v=cpu.getMemory().peek(a);memory.addRow(new Object[]{String.format("%02XH",a),String.format("%02XH",v)});}int[] st=cpu.getStackContents();StringBuilder sb=new StringBuilder(String.format("SP: %02XH   DEPTH: %d\n",cpu.getSP(),st.length));if(st.length==0)sb.append("Stack empty\n");for(int i=st.length-1;i>=0;i--)sb.append(String.format("%02XH  →  %02XH%s\n",CPU.STACK_BASE+1+i,st[i],i==st.length-1?"  TOP":""));stack.setText(sb.toString());
        List<Integer> queueValues=(cpu instanceof RemoteCPU)?((RemoteCPU)cpu).getQueueValues():queue.toList();int qStatus=(cpu instanceof RemoteCPU)?((RemoteCPU)cpu).getQueueStatus():queue.getStatus();StringBuilder qb=new StringBuilder(String.format("SIZE: %d/%d  STATUS: %s\nFront → Rear\n",queueValues.size(),FifoQueue.CAPACITY,FifoQueue.statusName(qStatus)));if(queueValues.isEmpty())qb.append("Queue empty\n");for(int v:queueValues)qb.append(String.format("%02XH   ",v));fifo.setText(qb.toString());}
    public static void main(String[] args){SwingUtilities.invokeLater(()->new SimulatorUI().setVisible(true));}
}
