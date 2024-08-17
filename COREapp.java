import com.sun.jdi.connect.Connector;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import jdk.jfr.Timespan;

public class COREapp extends JFrame {

    private JPanel bottomPanel; // Panel to hold agent buttons
    private JLabel worldSize;
    private JLabel perTurn;
    private JLabel maxSoldiers;
    private JLabel visRange;
    private JLabel startCLabel;
    public int sSpeed;
    public int gpturn;
    public int wSize;
    public int startcount;
    public int maxNumSoldiers;
    public int visibility_range;
    private JPanel topDisplayPanel; //Panel to hold Reload button
    private JPanel simdisplayPanel;
    public int step; //sim step
    public int displayStep; //The display step, might go back and forth
    //sim super step can be gotten from the sim at any time.
    public int displaySuperStep; //The step of the sim being displayed..corresponds to the super step
    public Simulation sim;
    public Set<Simulation.Agent_Details> agent_set;
    private Timer timer;
    public HashMap<String, Simulation.Agent_Details> agentLookup;
    public HashMap<Color, Integer> faceLookup;
    public int aCount;
    public int startingSuperStep;
    public HashMap<Integer, Integer> superStep2Step;

    public COREapp() {
        setTitle("COREwars Visualizer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //set default world values...size, fernies per round, etc
        this.wSize = 20;
        this.sSpeed = 100;
        this.gpturn = 50;
        this.visibility_range = 1;
        this.maxNumSoldiers = 1000;
        this.startcount = 75;
        this.step = 0;
        this.displayStep = 0;
        this.displaySuperStep = 0;
        this.startingSuperStep = 0;
        this.agent_set = new HashSet<>();
        this.agentLookup = new HashMap<>();
        this.faceLookup = new HashMap<>();
        this.superStep2Step = new HashMap<>();
        Random random = new Random();
        this.aCount = random.nextInt(4);
        initializeUI();
    }

    private void initializeUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        //add image
        JPanel startPanel = new JPanel(new BorderLayout());
        ImageIcon flavourImage = new ImageIcon("src/RAMfight.png","this is a caption");
        Image tmpImage = flavourImage.getImage(); // transform it 
        Image flavour = tmpImage.getScaledInstance(450, 450,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way 
        ImageIcon borderIcon = new ImageIcon("src/LoadScreenBorder.png","this is a caption"); 
        flavourImage = new ImageIcon(flavour);
        JLabel flavourPanel = new JLabel(borderIcon);
        
        
        //JLabel welcome = new JLabel("Welcome to COREwars!");
        JPanel startCenter = new JPanel();
        //startCenter.add(welcome,BorderLayout.CENTER);
        startPanel.add(startCenter,BorderLayout.NORTH);
        startPanel.add(flavourPanel,BorderLayout.CENTER);
        tabbedPane.addTab("Welcome", startPanel);

        // Menu Panel with JMenuBar
        JPanel menuPanel = new JPanel(new BorderLayout());
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Edit");
        ImageIcon settingsIcon = new ImageIcon("src/SimSettings.gif","this is a caption");
        menu.setIcon(settingsIcon);
        JMenuItem sizeItem = new JMenuItem("Set Ring Size");
        sizeItem.addActionListener(e -> {
            int worldsize = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter new world size:"));
            // Implement size change logic here
            wSize = worldsize;
            updateWorldSize(worldsize);
        });
        JMenuItem growthItem = new JMenuItem("Set Fernies per Turn");
        growthItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Fernies per Turn:"));
            // Implement size change logic here
            gpturn = tmp;
            updateGPTurn(tmp);
        });
        JMenuItem startItem = new JMenuItem("Set Starting Fernies");
        startItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Starting Fernies:"));
            // Implement size change logic here
            startcount = tmp;
            updateStartCount(tmp);
        });
        JMenuItem maxItem = new JMenuItem("Set Max per Tile");
        maxItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Maximum:"));
            // Implement size change logic here
            maxNumSoldiers = tmp;
            updateMax(tmp);
        });
        JMenuItem visItem = new JMenuItem("Set Visibility Range");
        visItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Visibility Range:"));
            // Implement size change logic here
            visibility_range = tmp;
            updateVis(tmp);
        });
        JMenuItem addAgentItem = new JMenuItem("Add Agent");
        addAgentItem.addActionListener(e -> createAddAgentPopup());
        menu.add(sizeItem);
        menu.add(growthItem);
        menu.add(startItem);
        menu.add(maxItem);
        menu.add(visItem);
        menu.add(addAgentItem);
        menuBar.add(menu);
        menuPanel.add(menuBar, BorderLayout.NORTH);
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        menuPanel.add(bottomPanel, BorderLayout.SOUTH);
        //Create settings panel
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        worldSize = new JLabel("World Size: 20");
        perTurn = new JLabel("Growth per Turn: 50");
        //simSpeed = new JLabel("Simulation Play Speed: 100 ms");
        visRange = new JLabel("Visibility Range: 1");
        maxSoldiers = new JLabel("Max Fernies per Node: 1000");
        startCLabel = new JLabel("Starting Fernies: 75");
        JLabel newLineHolder = new JLabel("\n");
        settingsPanel.setLayout(new GridLayout(0, 1));
        //,BorderLayout.CENTER
        JPanel settingsHolder = new JPanel(new FlowLayout(FlowLayout.CENTER));
        settingsPanel.add(newLineHolder);
        settingsPanel.add(new JLabel("\n"));
        settingsPanel.add(new JLabel("\n"));
        settingsPanel.add(worldSize);
        settingsPanel.add(new JLabel("\n"));
        settingsPanel.add(perTurn);
        settingsPanel.add(new JLabel("\n"));
        settingsPanel.add(visRange);
        settingsPanel.add(new JLabel("\n"));
        settingsPanel.add(maxSoldiers);
        settingsPanel.add(new JLabel("\n"));
        settingsPanel.add(startCLabel);
        settingsPanel.add(new JLabel("\n"));
        //settingsPanel.add(simSpeed);
        
        settingsHolder.add(settingsPanel);
        menuPanel.add(settingsHolder);
        tabbedPane.addTab("Settings", menuPanel);
        

        // Display Panel
        JPanel displayPanel = new JPanel(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false); // Prevents the toolbar from being moved
        ImageIcon bIcon = new ImageIcon("src/back.gif","this is a caption");
        JButton backButton = new JButton(bIcon);
        ImageIcon playIcon = new ImageIcon("src/play.gif","this is a caption");
        JButton startButton = new JButton(playIcon);
        ImageIcon pauseIcon = new ImageIcon("src/pause.gif","this is a caption");
        JButton stopButton = new JButton(pauseIcon);
        ImageIcon fIcon = new ImageIcon("src/forward.gif","this is a caption");
        JButton forwardButton = new JButton(fIcon);
        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepForward();
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSimulation();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopSimulation();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepBackward();
            }
        });


        toolBar.add(backButton);
        toolBar.add(stopButton);
        toolBar.add(startButton);
        toolBar.add(forwardButton);
        
        ImageIcon reloadIcon = new ImageIcon("src/reload_button.gif","this is a caption");
        //Image image = reloadIcon.getImage(); // transform it 
        //Image newimg = image.getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        //reloadIcon = new ImageIcon(image);
        JButton reloadButton = new JButton(reloadIcon);
        reloadButton.setToolTipText("RELOAD");
        reloadButton.addActionListener(e -> reloadSim());

        reloadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED)); // Remove borders
        reloadButton.setFocusPainted(false);  // Disable focus painting
        reloadButton.setContentAreaFilled(false);  // Remove background painting
        //reloadButton.setOpaque(true);  // Enable opaque to use the background color
        
        
        //reloadButton.setForeground(UIManager.getColor("Menu.foreground"));
        //reloadButton.setFont(UIManager.getFont("Menu.font"));

        ImageIcon pSettingsIcon = new ImageIcon("src/playSettings.gif","this is a caption");
        JPanel simSetButton = new JPanel();
        
        
        simSetButton.setToolTipText("Simulation Control Buttons");
        JMenuBar simMenuBar = new JMenuBar();
        
        JMenu simButtonMenu = new JMenu();
        simButtonMenu.setToolTipText("Speed and Step Navigation");
        simButtonMenu.setFocusPainted(true);
        simButtonMenu.setContentAreaFilled(false);
        simButtonMenu.setIcon(pSettingsIcon);
        simButtonMenu.setOpaque(true);
        simButtonMenu.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        JMenuItem preCalcItem = new JMenuItem("PreCalc Next 10 Steps");
        preCalcItem.addActionListener(e -> {
            preCalcSim(10);
        });
        JMenuItem preCalcNItem = new JMenuItem("PreCalc Next N Steps");
        preCalcNItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Steps to Precompute:"));
            preCalcSim(tmp);
        });
        JMenuItem returnItem = new JMenuItem("Return to Step 0");
        returnItem.addActionListener(e -> {
            returnToStep(0);
        });
        JMenuItem returnNItem = new JMenuItem("Return to Step N");
        returnNItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Return to Step:"));
            returnToStep(tmp);
        });
        JMenuItem setSpeedItem = new JMenuItem("Set Playback Speed");
        setSpeedItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "New Sim Speed (ms):"));
            this.sSpeed = tmp;
        });
        simButtonMenu.add(preCalcItem);
        simButtonMenu.add(preCalcNItem);
        simButtonMenu.add(returnItem);
        simButtonMenu.add(returnNItem);
        simButtonMenu.add(setSpeedItem);
        simMenuBar.add(simButtonMenu);
        simSetButton.add(simMenuBar);

        topDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,10,0));
        topDisplayPanel.add(reloadButton);
        topDisplayPanel.add(simSetButton);
        displayPanel.add(topDisplayPanel,BorderLayout.NORTH);
        simdisplayPanel = new DisplayPanel();
        simdisplayPanel.setBackground(Color.WHITE);
        displayPanel.add(simdisplayPanel,FlowLayout.CENTER);

        // Centering ToolBar
        JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        toolBarPanel.add(toolBar);
        displayPanel.add(toolBarPanel, BorderLayout.SOUTH); // Add centered toolbar to the bottom
        
        tabbedPane.addTab("Display", displayPanel);

        // Add the tabbed pane to the frame
        add(tabbedPane);
    }
    private void reloadSim() {
        //HashMap<String, Simulation.Agent_Details> agentLookup = new HashMap<>();
        ArrayList<String> ag_names = new ArrayList<>();
        for (Simulation.Agent_Details adetails : agent_set) {
            //agentLookup.put(adetails.locname, adetails);
            ag_names.add(adetails.locname);
            File theDir = new File(adetails.locname);
            if (!theDir.exists()){
                theDir.mkdirs();
            }
        }
        String[] agArray = new String[ ag_names.size() ];
        ag_names.toArray( agArray );
        this.sim = new Simulation(this.wSize,agArray,agentLookup,gpturn,maxNumSoldiers,startcount,visibility_range);
        this.sim.setup();
        this.step = 1;
        this.displayStep = this.step;
        this.displaySuperStep = sim.superStep;
        this.startingSuperStep = sim.superStep;
        this.superStep2Step.put(displaySuperStep,displayStep);
        System.out.println("Starting superstep: " + this.startingSuperStep);
        simdisplayPanel.repaint();
    }

    private void returnToStep(int n) {
        this.displayStep = n;
        if (n == 1 | n == 0) {
            this.displaySuperStep = this.startingSuperStep;
        } else {
            this.displaySuperStep = this.startingSuperStep + n*agent_set.size();
        }
        simdisplayPanel.repaint();
    }

    private void preCalcSim(int n) {
        int currentDisplayStep = this.displayStep;
        int currentDisplaySuperStep = this.displaySuperStep;
        int ac = agent_set.size();
        for (int i = 0; i < n*ac; i++) {
            this.updateSimulationNoDisplay();
        }
        this.displayStep = currentDisplayStep;
        this.displaySuperStep = currentDisplaySuperStep;
        simdisplayPanel.repaint();   
    }

    private void startSimulation() {
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(sSpeed, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (displaySuperStep < sim.superStep) {
                    displaySuperStep++;
                    displayStep = superStep2Step.get(displaySuperStep);
                    simdisplayPanel.repaint();
                    return;
                }
                updateSimulation();
            }
        });
        timer.start();
    }

    private void stopSimulation() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void stepForward() {
        //needs to check if display step < step in which case updateSim isn't called but display step moves up
        if (displaySuperStep < sim.superStep) {
            displaySuperStep++;
            displayStep = this.superStep2Step.get(displaySuperStep);
            simdisplayPanel.repaint();
            return;
        }
        updateSimulation(); // Placeholder
    }
    private void stepBackward() {
        // Add code to step backward in the simulation
        //move back a step
        displaySuperStep--;
        displayStep = this.superStep2Step.get(displaySuperStep);
        if (displaySuperStep < 0) {
            displaySuperStep = 0;
            displayStep = 1;
        }
        simdisplayPanel.repaint();
    }

    private void updateWorldSize(int size) {
        worldSize.setText("World Size: "+String.valueOf(size));
        Component temp = worldSize.getParent();
        temp.revalidate();
        temp.repaint();
    }

    private void updateGPTurn(int size) {
        perTurn.setText("Growth per Turn: "+String.valueOf(size));
        Component temp = perTurn.getParent();
        temp.revalidate();
        temp.repaint();
    }

    private void updateStartCount(int count) {
        startCLabel.setText("Starting Fernies: "+String.valueOf(count));
        Component temp = startCLabel.getParent();
        temp.revalidate();
        temp.repaint();
    }
    private void updateMax(int maxx) {
        maxSoldiers.setText("Max Fernies per Node: "+String.valueOf(maxx));
        Component temp = maxSoldiers.getParent();
        temp.revalidate();
        temp.repaint();
    }
    private void updateVis(int maxx) {
        visRange.setText("Visibility Range: "+String.valueOf(maxx));
        Component temp = visRange.getParent();
        temp.revalidate();
        temp.repaint();
    }

    private void createAddAgentPopup() {
        JDialog addAgentDialog = new JDialog(this, "Add Agent", true);
        addAgentDialog.setLayout(new BorderLayout());
        addAgentDialog.setSize(500, 400);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField nameField = new JTextField(10);
        JTextField locationField = new JTextField(10);

        inputPanel.add(new JLabel("Name of Agent Class File:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Where to Store State Files:"));
        inputPanel.add(locationField);
        inputPanel.add(new JLabel("Color:"));

        JColorChooser colorChooser = new JColorChooser();
        colorChooser.setPreviewPanel(new JPanel()); // Remove the preview panel to save space

        addAgentDialog.add(inputPanel, BorderLayout.NORTH);
        addAgentDialog.add(colorChooser, BorderLayout.CENTER);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String location = locationField.getText();
            Color color = colorChooser.getColor();
            addAgent(name, location, color);
            addAgentButton(name, location, color);
            
            addAgentDialog.dispose(); // Close the dialog after adding the agent
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        addAgentDialog.add(buttonPanel, BorderLayout.SOUTH);

        addAgentDialog.setVisible(true);
    }

    private void addAgent(String name, String loc, Color color) {
        //Adds the agent to the list of agents.
        //agent_set;
        
        Simulation.Agent_Details a_d = new Simulation.Agent_Details(name,loc,color);
        agentLookup.put(a_d.locname, a_d);
        agent_set.add(a_d);
        faceLookup.put(color,aCount%5);
        aCount++;
    }

    private void addAgentButton(String name, String loc, Color color) {
        JMenuBar agentMenuBar = new JMenuBar();
        JPanel agentButton = new JPanel();
        JMenu agentButtonMenu = new JMenu(name);
        agentButton.setBackground(color);
        //agentButton.setBackground(Color.RED);
        JMenuItem removeAgentItem = new JMenuItem("Delete");
        removeAgentItem.addActionListener(e -> {
            Simulation.Agent_Details a_d = agentLookup.get(loc);
            agent_set.remove(a_d);
            agentLookup.remove(loc);
            //Removes the button from the app.
            JPanel grandparent = bottomPanel;
            grandparent.remove(agentButton);
            grandparent.revalidate();
            grandparent.repaint();
        });
        agentButtonMenu.add(removeAgentItem);
        agentMenuBar.add(agentButtonMenu);
        agentButton.add(agentMenuBar);
        bottomPanel.add(agentButton);
        bottomPanel.revalidate(); // Refresh panel to show new button
    }
    class DisplayPanel extends JPanel {
        private Image gifImage;
        private Image shrunkFace;
        private Image smallFace;
        //private Image midRedFace;
        private Image mildFace;
        private Image redFace;
        private Image[] redFaces;
        private Image[] blueFaces;
        private Image[] greenFaces;
        private Image[] yellowFaces;
        private Image[] otherFaces;
        private HashMap<Integer, Image[]> whichFace;
    
        // Constructor or initialization block to load the GIF
        public DisplayPanel() {
            redFaces = new Image[3];
            blueFaces = new Image[3];
            greenFaces = new Image[3];
            yellowFaces = new Image[3];
            otherFaces = new Image[3];
            whichFace = new HashMap<>();
            redFace = new ImageIcon("src/cartoonFace.gif").getImage(); // Load your GIF image here
            mildFace = new ImageIcon("src/mildFace.gif").getImage();
            redFaces[0] = new ImageIcon("src/cartoonFace.gif").getImage();
            redFaces[1] = redFaces[0].getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
            redFaces[2] = redFaces[0].getScaledInstance(24, 24,  java.awt.Image.SCALE_SMOOTH);
            blueFaces[0] = new ImageIcon("src/angryEyebrowFace.gif").getImage();
            blueFaces[1] = blueFaces[0].getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
            blueFaces[2] = blueFaces[0].getScaledInstance(24, 24,  java.awt.Image.SCALE_SMOOTH);
            greenFaces[0] = new ImageIcon("src/angryTongueFace.gif").getImage();
            greenFaces[1] = greenFaces[0].getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
            greenFaces[2] = greenFaces[0].getScaledInstance(24, 24,  java.awt.Image.SCALE_SMOOTH);
            yellowFaces[0] = new ImageIcon("src/dullFace.gif").getImage();
            yellowFaces[1] = yellowFaces[0].getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
            yellowFaces[2] = yellowFaces[0].getScaledInstance(24, 24,  java.awt.Image.SCALE_SMOOTH);
            otherFaces[0] = new ImageIcon("src/crosseyedFace.gif").getImage();
            otherFaces[1] = otherFaces[0].getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
            otherFaces[2] = otherFaces[0].getScaledInstance(24, 24,  java.awt.Image.SCALE_SMOOTH);
            shrunkFace = mildFace.getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH); 
            smallFace = mildFace.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);
            whichFace.put(0,redFaces);
            whichFace.put(1,blueFaces);
            whichFace.put(2,greenFaces);
            whichFace.put(3,yellowFaces);
            whichFace.put(4,otherFaces); //add different images later
            
        }

        private Image getFace(int myCount, Color myColor) {
            if (myCount < 100) {
                return smallFace;
            }
            int scaler = 0;
            int fint = faceLookup.get(myColor);
            Image[] faces = whichFace.get(fint);
            if (myCount < 201) {
                scaler = 2;
            } else if (myCount < 351) {
                scaler = 1;
            }
            //System.out.println("FACE STUFF fint: " + fint + " scaler: " + scaler);
            //System.out.println("Count: " + myCount + " Color: " + myColor.getRed() + ", " + myColor.getGreen() + ", " + myColor.getBlue());
            return faces[scaler];
        }
        private HashMap<Integer,Integer> findCenterIndicesAndSums(Simulation.World_State dState, int minSize) {
            ArrayList<Integer> centerIndices = new ArrayList<>();
            ArrayList<Integer> sums = new ArrayList<>();
            HashMap<Integer,Integer> indexToSum = new HashMap<>();
            
            int n = dState.owners.size();
            int start = 0;
    
            while (start < n) {
                int end = start;
                
                // Find the end of the sublist with the same owner
                while (end < n && dState.owners.get(end).equals(dState.owners.get(start))) {
                    end++;
                }
    
                int sublistSize = end - start;
                if (sublistSize >= minSize) {
                    // Calculate the center index
                    int centerIndex = start + (sublistSize - 1) / 2;
                    //centerIndices.add(centerIndex);
    
                    // Calculate the sum of the counts in this sublist
                    int sum = 0;
                    for (int i = start; i < end; i++) {
                        sum += dState.counts.get(i);
                    }
                    //sums.add(sum);
                    if (dState.owners.get(start) != Color.GRAY) {
                        indexToSum.put(centerIndex,sum);
                    }  
                }
    
                // Move to the next group
                start = end;
            }
    
            // Print the results
            //System.out.println("Center indices of sublists: " + centerIndices);
            //System.out.println("Sums of counts for those sublists: " + sums);
    
            return indexToSum;
        }
    
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
    
            if (step == 0) {
                g.setColor(Color.BLACK);
                g.drawString("PRESS RELOAD BUTTON", 50, 50);
                return;
            }
    
            String stepS = Integer.toString(step);
            String stepD = Integer.toString(displayStep);
            g.setColor(Color.BLACK);
            g.drawString("Last Game Step: " + stepS, 50, 50);
            g.drawString("Current Display Step: " + stepD, 50, 60);
            g.drawImage(gifImage, 50, 75, this);
    
            ArrayList<Simulation.World_State> state_history = sim.get_state_history();
            Simulation.World_State drawState = state_history.get(displaySuperStep);
    
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(getWidth(), getHeight()) / 2 - 20; // Radius of the outer circle
            int arcAngle = 360000 / drawState.counts.size(); // Each segment has equal size
    
            int startAngle = 0; // Starting angle for the first arc
            //Map counts and owners to find the indexes of nodes which are surrounded by similar color nodes.
            //Then when the size is large you can draw faces at those indexes.
            for (int i = 0; i < drawState.counts.size(); i++) {
                int count = drawState.counts.get(i);
                Color c = drawState.owners.get(i);
    
                // Fill the arc with the specified color
                g.setColor(c);
                g.fillArc(centerX - radius, centerY - radius, 2 * radius, 2 * radius, startAngle/1000, (1000+arcAngle)/1000);
    
                // Calculate the midpoint angle of the arc to place the text
                int midAngle = startAngle + arcAngle / 2;
                double angleRad = Math.toRadians(midAngle/1000);
                int textRadius = radius + 10; // Position text slightly outside the arc
    
                // Adjust text position based on the angle
                int textX = centerX + (int) (textRadius * Math.cos(angleRad)) - g.getFontMetrics().stringWidth(String.valueOf(count)) / 2;
                int textY = centerY - (int) (textRadius * Math.sin(angleRad)) + g.getFontMetrics().getHeight() / 4;
                int text2X = centerX + (int) ((textRadius-45) * Math.cos(angleRad)) - g.getFontMetrics().stringWidth(String.valueOf(i)) / 2;
                int text2Y = centerY - (int) ((textRadius-45) * Math.sin(angleRad)) + g.getFontMetrics().getHeight() / 4;
    
                // Draw the count value near the arc
                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(count), textX, textY);
                g.drawString(String.valueOf(i), text2X, text2Y);
                // If count > 0, draw the GIF in the center of the arc
                if ((count > 0) & (wSize < 36) & (c != Color.GRAY)) {
                    Image myFace = getFace(count, c);
                    int gifX = centerX + (int) (radius * 0.9 * Math.cos(angleRad)) - myFace.getWidth(this) / 2;
                    int gifY = centerY - (int) (radius * 0.9 * Math.sin(angleRad)) - myFace.getHeight(this) / 2;
                    g.drawImage(myFace, gifX, gifY, this);
                    
                }
    
                // Update start angle for the next arc
                startAngle += arcAngle;
            }
    
            // Draw the white inner circle last, smaller than the outer circle
            int innerRadius = (int) (radius * 0.8); // 80% of the outer circle's radius
            g.setColor(Color.WHITE);
            g.fillOval(centerX - innerRadius, centerY - innerRadius, 2 * innerRadius, 2 * innerRadius);

            if (wSize > 35) {
                int minNodes = 1 + wSize / 35;
                HashMap<Integer,Integer> locCount = findCenterIndicesAndSums(drawState,minNodes);
                for (int x : locCount.keySet()) {
                    Color c = drawState.owners.get(x);
                    int count = locCount.get(x);
                    Image myFace = getFace(count, c);
                    int myAngle = (x+1)*arcAngle/1000;
                    double myAngleRad = Math.toRadians(myAngle);
                    int gifX = centerX + (int) (radius * 0.9 * Math.cos(myAngleRad)) - myFace.getWidth(this) / 2;
                    int gifY = centerY - (int) (radius * 0.9 * Math.sin(myAngleRad)) - myFace.getHeight(this) / 2;
                    g.drawImage(myFace, gifX, gifY, this);
                }
                
            }
        }
    }
    private void updateSimulation() {        
        //needs to check if display step < step
        this.step = this.step + sim.run_one_agent(this.step);
        this.displayStep = this.step;
        this.displaySuperStep = sim.superStep;
        this.superStep2Step.put(displaySuperStep,displayStep);
        //System.out.println(this.step);
        simdisplayPanel.repaint(); // Repaint the display panel to show the new state
    }
    private void updateSimulationNoDisplay() {        
        //needs to check if display step < step
        this.step = this.step + sim.run_one_agent(this.step);
        this.displayStep = this.step;
        this.displaySuperStep = sim.superStep;
        this.superStep2Step.put(displaySuperStep,displayStep);
    }

    private void loadAgentsFromFile(String line) {
        String[] parts = line.split(",");
        String name = parts[0];
        String loc = parts[1];
        Color myColor = new Color(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
        addAgent(name, loc, myColor);
        addAgentButton(name, loc, myColor);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            COREapp app = new COREapp();
            app.setVisible(true);
            if (args.length > 0) {
                try (BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        app.loadAgentsFromFile(line);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading the settings file: " + e.getMessage());
                }
            }
        });
    }
}
